package com.qingmang.application;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.favorite.FavoriteFolder;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.user.User;
import com.qingmang.domain.user.UserPrivacy;
import com.qingmang.domain.user.UserStats;
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.domain.user.mapper.UserPrivacyMapper;
import com.qingmang.domain.user.mapper.UserStatsMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.UserQueryMapper;
import com.qingmang.interfaces.dto.LoginRequest;
import com.qingmang.interfaces.dto.RegisterRequest;
import com.qingmang.interfaces.dto.ProfileUpdateRequest;
import com.qingmang.interfaces.dto.PrivacyUpdateRequest;
import com.qingmang.interfaces.dto.AccountUpdateRequest;
import com.qingmang.interfaces.vo.LoginRow;
import com.qingmang.interfaces.vo.UserProfileVO;
import com.qingmang.support.AuthContext;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {
    private static final List<String> HOME_SECTIONS = List.of("magnum-video", "tribute-video",
            "collect", "follow-anime", "love-video");

    private final UserMapper userMapper;
    private final UserPrivacyMapper privacyMapper;
    private final UserStatsMapper statsMapper;
    private final FavoriteFolderMapper folderMapper;
    private final UserQueryMapper queryMapper;
    private final CounterMapper counterMapper;
    private final MentionNotificationService mentions;
    private final NotificationCenterService notifications;

    public AuthService(UserMapper userMapper, UserPrivacyMapper privacyMapper,
                       UserStatsMapper statsMapper, FavoriteFolderMapper folderMapper,
                       UserQueryMapper queryMapper, CounterMapper counterMapper,
                       MentionNotificationService mentions, NotificationCenterService notifications) {
        this.userMapper = userMapper;
        this.privacyMapper = privacyMapper;
        this.statsMapper = statsMapper;
        this.folderMapper = folderMapper;
        this.queryMapper = queryMapper;
        this.counterMapper = counterMapper;
        this.mentions = mentions;
        this.notifications = notifications;
    }

    @Transactional
    public ApiResponse<Map<String, Object>> register(RegisterRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setNickname(req.getNickname());
        user.setAvatarUrl(isBlank(req.getAvatarUrl()) ? BizConstants.DEFAULT_AVATAR : req.getAvatarUrl());
        user.setPhone(isBlank(req.getPhone()) ? null : req.getPhone());
        user.setLevel(0);
        user.setExp(0);
        user.setRole(0);
        user.setStatus(1);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.ACCOUNT_EXISTS);
        }
        Long uid = user.getId();
        initAfterRegister(uid);
        StpUtil.login(uid);
        return ApiResponse.ok(tokenPayload(uid));
    }

    /** 注册后一并建好隐私设置、计数行和默认收藏夹，避免后续各处再补。 */
    private void initAfterRegister(Long uid) {
        UserPrivacy p = new UserPrivacy();
        p.setUserId(uid);
        privacyMapper.insert(p);

        UserStats s = new UserStats();
        s.setUserId(uid);
        statsMapper.insert(s);

        FavoriteFolder folder = new FavoriteFolder();
        folder.setOwnerId(uid);
        folder.setName(BizConstants.DEFAULT_FOLDER_NAME);
        folder.setDescription(BizConstants.DEFAULT_FOLDER_DESC);
        folder.setVisibility(1);
        folder.setIsDefault(Boolean.TRUE);
        folder.setSortOrder(0);
        folder.setItemCount(0);
        folderMapper.insert(folder);
    }

    @Transactional
    public ApiResponse<Map<String, Object>> login(LoginRequest req) {
        LoginRow row = queryMapper.selectForLogin(req.getUsername());
        if (row == null || row.getDeletedAt() != null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(req.getPassword(), row.getPasswordHash())) {
            throw new BizException(ErrorCode.PASSWORD_ERROR);
        }
        if (row.getStatus() != null && row.getStatus() == 0) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        StpUtil.login(row.getUserId());

        // 每日登录时间记录：用日期比较，同一天只更新一次
        User u = userMapper.selectById(row.getUserId());
        LocalDateTime today = LocalDateTime.now();
        if (u != null && (u.getLastLoginAt() == null || !u.getLastLoginAt().toLocalDate().isEqual(today.toLocalDate()))) {
            u.setLastLoginAt(today);
            userMapper.updateById(u);
        }
        return ApiResponse.ok(tokenPayload(row.getUserId()));
    }

    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok();
    }

    public UserProfileVO profile(Long userId) {
        Long viewer = AuthContext.currentUserIdOrNull();
        UserProfileVO vo = queryMapper.selectProfile(userId, viewer);
        if (vo == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        vo.setIsSelf(userId.equals(viewer));
        UserPrivacy privacy = privacyMapper.selectOne(Wrappers.<UserPrivacy>lambdaQuery()
                .eq(UserPrivacy::getUserId, userId));
        vo.setHomeSectionOrder(readSectionOrder(privacy == null ? null : privacy.getHomeSectionOrder()));
        if (userId.equals(viewer)) {
            Map<String, Object> counts = mentions.unread(userId);
            vo.setMessageNumber(((Number) counts.get("messageNumber")).longValue());
            vo.setReplyCommentNumber(((Number) counts.get("replyCommentNumber")).longValue());
            vo.setAtNumber(((Number) counts.get("atNumber")).longValue());
            vo.setLikeAllNumber(((Number) counts.get("likeAllNumber")).longValue());
            vo.setAllMessageNumber(((Number) counts.get("allMessageNumber")).longValue());
            Map<String, Object> settings = notifications.settings(userId);
            vo.setMessageWarn(((Number) settings.get("messageWarn")).intValue());
            vo.setReplyMessageWarn(((Number) settings.get("replyMessageWarn")).intValue());
            vo.setAtMessageWarn(((Number) settings.get("atMessageWarn")).intValue());
            vo.setLikeMessageWarn(((Number) settings.get("likeMessageWarn")).intValue());
        }
        if (!userId.equals(viewer)) {
            if (!Boolean.TRUE.equals(vo.getShowBirthday())) vo.setBirthday(null);
            vo.setRecordHistory(null);
        }
        if (viewer == null) {
            vo.setFollowed(null);
        }
        return vo;
    }

    public UserProfileVO currentProfile() {
        return profile(AuthContext.requireUserId());
    }

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        requireActiveUser(userId);
        userMapper.update(null, Wrappers.<User>lambdaUpdate().eq(User::getId, userId)
                .set(User::getSignature, request.getSignature()));
    }

    @Transactional
    public void updateAccount(Long userId, AccountUpdateRequest request) {
        requireActiveUser(userId);
        userMapper.update(null, Wrappers.<User>lambdaUpdate().eq(User::getId, userId)
                .set(User::getNickname, request.getNickname().trim())
                .set(User::getSignature, request.getSignature())
                .set(User::getGender, request.getGender()).set(User::getBirthday, request.getBirthday()));
    }

    @Transactional
    public void updateMedia(Long userId, String category, String url) {
        requireActiveUser(userId);
        if (!"avatar".equals(category) && !"background".equals(category)) throw new BizException(ErrorCode.PARAM_INVALID);
        userMapper.update(null, Wrappers.<User>lambdaUpdate().eq(User::getId, userId)
                .set("avatar".equals(category), User::getAvatarUrl, url)
                .set("background".equals(category), User::getBackgroundUrl, url));
    }

    @Transactional
    public void updatePrivacy(Long userId, PrivacyUpdateRequest request) {
        requireActiveUser(userId);
        if (request.getShowFavoriteList() == null && request.getShowFollowList() == null &&
                request.getShowFansList() == null &&
                request.getShowLikeList() == null && request.getShowBirthday() == null &&
                request.getRecordHistory() == null && request.getHomeSectionOrder() == null)
            throw new BizException(ErrorCode.PARAM_INVALID);
        if (request.getHomeSectionOrder() != null &&
                (request.getHomeSectionOrder().size() != HOME_SECTIONS.size() ||
                 !new HashSet<>(request.getHomeSectionOrder()).equals(Set.copyOf(HOME_SECTIONS))))
            throw new BizException(ErrorCode.PARAM_INVALID, "首页排序必须包含全部五个栏目");
        UserPrivacy privacy = privacyMapper.selectOne(Wrappers.<UserPrivacy>lambdaQuery()
                .eq(UserPrivacy::getUserId, userId));
        if (privacy == null) {
            privacy = new UserPrivacy();
            privacy.setUserId(userId);
            privacy.setShowFavoriteList(Boolean.TRUE.equals(request.getShowFavoriteList()));
            privacy.setShowFollowList(Boolean.TRUE.equals(request.getShowFollowList()));
            privacy.setShowFansList(Boolean.TRUE.equals(request.getShowFansList()));
            privacy.setShowLikeList(Boolean.TRUE.equals(request.getShowLikeList()));
            privacy.setShowBirthday(Boolean.TRUE.equals(request.getShowBirthday()));
            privacy.setRecordHistory(!Boolean.FALSE.equals(request.getRecordHistory()));
            privacy.setHomeSectionOrder(writeSectionOrder(request.getHomeSectionOrder()));
            privacyMapper.insert(privacy);
        } else {
            privacyMapper.update(null, Wrappers.<UserPrivacy>lambdaUpdate()
                    .eq(UserPrivacy::getUserId, userId)
                    .set(request.getShowFavoriteList() != null, UserPrivacy::getShowFavoriteList, request.getShowFavoriteList())
                    .set(request.getShowFollowList() != null, UserPrivacy::getShowFollowList, request.getShowFollowList())
                    .set(request.getShowFansList() != null, UserPrivacy::getShowFansList, request.getShowFansList())
                    .set(request.getShowLikeList() != null, UserPrivacy::getShowLikeList, request.getShowLikeList())
                    .set(request.getShowBirthday() != null, UserPrivacy::getShowBirthday, request.getShowBirthday())
                    .set(request.getRecordHistory() != null, UserPrivacy::getRecordHistory, request.getRecordHistory())
                    .set(request.getHomeSectionOrder() != null, UserPrivacy::getHomeSectionOrder,
                            writeSectionOrder(request.getHomeSectionOrder())));
        }
    }

    private List<String> readSectionOrder(String raw) {
        if (raw == null) return HOME_SECTIONS;
        try {
            List<String> order = new ObjectMapper().readValue(raw, new TypeReference<List<String>>() {});
            return order.size() == HOME_SECTIONS.size() && new HashSet<>(order).equals(Set.copyOf(HOME_SECTIONS))
                    ? order : HOME_SECTIONS;
        } catch (JsonProcessingException | NullPointerException e) {
            return HOME_SECTIONS;
        }
    }

    private String writeSectionOrder(List<String> order) {
        if (order == null) return null;
        try {
            return new ObjectMapper().writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
    }

    private Map<String, Object> tokenPayload(Long uid) {
        Map<String, Object> data = new HashMap<>(4);
        data.put("token", StpUtil.getTokenValue());
        data.put("tokenName", StpUtil.getTokenName());
        data.put("userId", uid);
        data.put("expiresIn", StpUtil.getTokenTimeout());
        return data;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** 供其它模块查询某用户是否仍有效，避免各处重复判断。 */
    public User requireActiveUser(Long userId) {
        User u = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getId, userId));
        if (u == null || u.getDeletedAt() != null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(u.getStatus())) throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        return u;
    }

    /** 等级与经验推进：升到 MAX_USER_LEVEL 为止。 */
    public void addExp(Long userId, int delta) {
        User u = requireActiveUser(userId);
        int exp = (u.getExp() == null ? 0 : u.getExp()) + delta;
        int level = u.getLevel() == null ? 0 : u.getLevel();
        while (level < BizConstants.MAX_USER_LEVEL && exp >= levelThreshold(level)) {
            exp -= levelThreshold(level);
            level++;
        }
        if (level >= BizConstants.MAX_USER_LEVEL) {
            level = BizConstants.MAX_USER_LEVEL;
        }
        u.setExp(exp);
        u.setLevel(level);
        userMapper.updateById(u);
    }

    private int levelThreshold(int level) {
        return (level + 1) * BizConstants.LEVEL_EXP_BASE;
    }
}