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

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserPrivacyMapper privacyMapper;
    private final UserStatsMapper statsMapper;
    private final FavoriteFolderMapper folderMapper;
    private final UserQueryMapper queryMapper;
    private final CounterMapper counterMapper;

    public AuthService(UserMapper userMapper, UserPrivacyMapper privacyMapper,
                       UserStatsMapper statsMapper, FavoriteFolderMapper folderMapper,
                       UserQueryMapper queryMapper, CounterMapper counterMapper) {
        this.userMapper = userMapper;
        this.privacyMapper = privacyMapper;
        this.statsMapper = statsMapper;
        this.folderMapper = folderMapper;
        this.queryMapper = queryMapper;
        this.counterMapper = counterMapper;
    }

    @Transactional
    public ApiResponse<Map<String, Object>> register(RegisterRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setNickname(req.getNickname());
        user.setAvatarUrl(isBlank(req.getAvatarUrl()) ? BizConstants.DEFAULT_AVATAR : req.getAvatarUrl());
        user.setPhone(isBlank(req.getPhone()) ? null : req.getPhone());
        user.setCoinBalance(BizConstants.REGISTER_REWARD_COIN);
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

        // 每日登录奖励：用日期比较，同一天只给一次
        User u = userMapper.selectById(row.getUserId());
        LocalDateTime today = LocalDateTime.now();
        if (u != null && (u.getLastLoginAt() == null || !u.getLastLoginAt().toLocalDate().isEqual(today.toLocalDate()))) {
            u.setLastLoginAt(today);
            userMapper.updateById(u);
            counterMapper.addCoinBalance(u.getId(), BizConstants.LOGIN_REWARD_COIN);
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
        if (viewer == null) {
            vo.setFollowed(null);
        }
        return vo;
    }

    public UserProfileVO currentProfile() {
        return profile(AuthContext.requireUserId());
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
        if (u == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
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