package com.moji.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moji.FilePathEnum;
import com.moji.dto.SystemCommentSearchDto;
import com.moji.dto.SystemDynamicSearchDto;
import com.moji.dto.SystemKeyWordSearchDto;
import com.moji.dto.SystemLogSearchDto;
import com.moji.dto.SystemMessageSearchDto;
import com.moji.dto.SystemOperateDto;
import com.moji.dto.SystemRecycleSearchDto;
import com.moji.dto.SystemUserSearchDto;
import com.moji.dto.SystemVideoSearchDto;
import com.moji.exception.BaseException;
import com.moji.mapper.CommentControlsMapper;
import com.moji.mapper.CollectMapper;
import com.moji.mapper.CommentsMapper;
import com.moji.mapper.DynamicMapper;
import com.moji.mapper.HistoryMapper;
import com.moji.mapper.KeyWordMapper;
import com.moji.mapper.LikesMapper;
import com.moji.mapper.PrivateMessageMapper;
import com.moji.mapper.RecycleBinMapper;
import com.moji.mapper.ScrollingMapper;
import com.moji.mapper.SystemOperationLogMapper;
import com.moji.mapper.ThrowCoinMapper;
import com.moji.mapper.UserBanMapper;
import com.moji.mapper.UserMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.Collects;
import com.moji.po.CommentControls;
import com.moji.po.Comments;
import com.moji.po.Dynamic;
import com.moji.po.History;
import com.moji.po.KeyWord;
import com.moji.po.Likes;
import com.moji.po.PrivateMessage;
import com.moji.po.RecycleBin;
import com.moji.po.Scrolling;
import com.moji.po.SystemOperationLog;
import com.moji.po.ThrowCoin;
import com.moji.po.UserBan;
import com.moji.po.Users;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CacheService;
import com.moji.service.SystemLogService;
import com.moji.service.SystemService;
import com.moji.service.VideosService;
import com.moji.vo.SystemCommentVo;
import com.moji.vo.SystemDynamicVo;
import com.moji.vo.SystemHotKeyWordVo;
import com.moji.vo.SystemHotVideoVo;
import com.moji.vo.SystemMessageVo;
import com.moji.vo.SystemOverviewVo;
import com.moji.vo.SystemRecycleBinVo;
import com.moji.vo.SystemTrendVo;
import com.moji.vo.SystemUserVo;
import com.moji.vo.SystemVideoListVo;
import com.moji.vo.SystemVideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemServiceImpl implements SystemService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //趋势统计的天数
    private static final int TREND_DAYS = 7;

    //列表分页每页条数
    private static final long PAGE_SIZE = 10L;

    //分组统计的列别名，取值时必须按列名读而不是按列顺序
    private static final String TREND_DATE_COLUMN = "trend_date";
    private static final String TREND_COUNT_COLUMN = "trend_count";

    //视频状态 0未审核 1已通过 2未通过/已下架
    private static final int VIDEO_STATUS_WAIT = 0;
    private static final int VIDEO_STATUS_PASS = 1;
    private static final int VIDEO_STATUS_REJECT = 2;

    //缓存名称，与 VideosController/CollectController 中的 @Cacheable 保持一致
    private static final String CACHE_COLLECT = "collect";
    private static final String CACHE_VIDEO_TITLE = "videoTitle";

    //回收站业务类型
    private static final String RECYCLE_BIZ_VIDEO = "video";
    private static final String RECYCLE_BIZ_COMMENT = "comment";
    private static final String RECYCLE_BIZ_DYNAMIC = "dynamic";

    //回收站状态 0在回收站 1已还原
    private static final int RECYCLE_STATUS_IN_BIN = 0;
    private static final int RECYCLE_STATUS_RESTORED = 1;

    //回收站备份目录(位于 upload/video 下)
    private static final String RECYCLE_SUBDIR_ROOT = "recycle_bin/";
    private static final String RECYCLE_SUBDIR_VIDEO = "video";
    private static final String RECYCLE_SUBDIR_COVER = "cover";

    //批量删除单次最大条数
    private static final int MAX_BATCH_SIZE = 200;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentControlsMapper commentControlsMapper;

    @Autowired
    private DynamicMapper dynamicMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private KeyWordMapper keyWordMapper;

    @Autowired
    private LikesMapper likesMapper;

    @Autowired
    private UserBanMapper userBanMapper;

    @Autowired
    private SystemOperationLogMapper systemOperationLogMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private VideosService videosService;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RecycleBinMapper recycleBinMapper;

    @Autowired
    private ThrowCoinMapper throwCoinMapper;

    @Autowired
    private ScrollingMapper scrollingMapper;

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private com.moji.service.CommentService commentService;

    @Override
    public Boolean checkAdmin(Integer operatorId, String token) {

        if (operatorId == null || !StringUtils.hasText(token))
            return false;

        Users users = userMapper.selectById(operatorId);
        if (users == null || users.getAdminFlag() == null || users.getAdminFlag() == 0)
            return false;

        LoginLimiterServer limiterServer = new LoginLimiterServer();
        return limiterServer.checkUser(operatorId, token);
    }

    @Override
    public SystemOverviewVo getOverview() {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        Long userNumber = userMapper.selectCount(null);
        Long adminNumber = userMapper.selectCount(new LambdaQueryWrapper<Users>().eq(Users::getAdminFlag, 1));
        Long todayUserNumber = userMapper.selectCount(
                new LambdaQueryWrapper<Users>().ge(Users::getCreateTime, todayStart).lt(Users::getCreateTime, tomorrowStart));

        Long videoNumber = videosMapper.selectCount(null);
        Long todayVideoNumber = videosMapper.selectCount(
                new LambdaQueryWrapper<Videos>().ge(Videos::getCreateTime, todayStart).lt(Videos::getCreateTime, tomorrowStart));
        //status 0是未审核
        Long pendingVideoNumber = videosMapper.selectCount(new LambdaQueryWrapper<Videos>().eq(Videos::getStatus, 0));

        Long commentNumber = commentsMapper.selectCount(null);
        Long todayCommentNumber = commentsMapper.selectCount(
                new LambdaQueryWrapper<Comments>().ge(Comments::getCommentTime, todayStart).lt(Comments::getCommentTime, tomorrowStart));

        //up主自己的动态 fans_id 为空，这里只统计全部动态
        Long dynamicNumber = dynamicMapper.selectCount(null);
        Long messageNumber = privateMessageMapper.selectCount(null);
        Long keyWordNumber = keyWordMapper.selectCount(null);
        Long banNumber = userBanMapper.selectCount(new LambdaQueryWrapper<UserBan>().eq(UserBan::getStatus, 1));

        //总播放量与总点赞量用聚合查询，避免把全表加载到内存
        Long totalPlayNumber = sumPlayNumber();
        Long totalLikeNumber = sumLikeCommentNumber();

        return SystemOverviewVo.builder()
                .userNumber(zeroIfNull(userNumber))
                .adminNumber(zeroIfNull(adminNumber))
                .todayUserNumber(zeroIfNull(todayUserNumber))
                .videoNumber(zeroIfNull(videoNumber))
                .todayVideoNumber(zeroIfNull(todayVideoNumber))
                .pendingVideoNumber(zeroIfNull(pendingVideoNumber))
                .commentNumber(zeroIfNull(commentNumber))
                .todayCommentNumber(zeroIfNull(todayCommentNumber))
                .dynamicNumber(zeroIfNull(dynamicNumber))
                .messageNumber(zeroIfNull(messageNumber))
                .keyWordNumber(zeroIfNull(keyWordNumber))
                .banNumber(zeroIfNull(banNumber))
                .totalPlayNumber(zeroIfNull(totalPlayNumber))
                .totalLikeNumber(zeroIfNull(totalLikeNumber))
                .userTrend(buildUserTrend())
                .videoTrend(buildVideoTrend())
                .commentTrend(buildCommentTrend())
                .hotVideos(buildHotVideos())
                .hotKeyWords(buildHotKeyWords())
                .build();
    }

    @Override
    public Page<SystemUserVo> searchUsers(SystemUserSearchDto dto) {

        Page<Users> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(Users::getUserName, keyword).or().like(Users::getPhone, keyword));
        }

        //type -1全部 0普通用户 1管理员 2已封禁
        if (dto.getType() != null && dto.getType() == 1) {
            wrapper.eq(Users::getAdminFlag, 1);
        } else if (dto.getType() != null && dto.getType() == 0) {
            wrapper.eq(Users::getAdminFlag, 0);
        }

        //loginFlag 0最近7天未登录 1最近7天已登录
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (dto.getLoginFlag() != null && dto.getLoginFlag() == 0) {
            wrapper.and(w -> w.isNull(Users::getLoginDateTime).or().lt(Users::getLoginDateTime, sevenDaysAgo));
        } else if (dto.getLoginFlag() != null && dto.getLoginFlag() == 1) {
            wrapper.ge(Users::getLoginDateTime, sevenDaysAgo);
        }

        wrapper.orderByDesc(Users::getCreateTime);
        Page<Users> usersPage = userMapper.selectPage(page, wrapper);

        List<SystemUserVo> records = new ArrayList<>();
        List<Users> userList = usersPage.getRecords();
        if (userList.isEmpty())
            return toUserVoPage(usersPage, records);

        //一次性取出本页涉及到的封禁记录，避免逐条查询
        List<Integer> userIds = userList.stream().map(Users::getId).collect(Collectors.toList());
        Map<Integer, UserBan> banMap = userBanMapper.selectList(new LambdaQueryWrapper<UserBan>().in(UserBan::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(UserBan::getUserId, Function.identity(), (a, b) -> b));

        for (Users record : userList) {
            UserBan userBan = banMap.get(record.getId());
            boolean banned = userBan != null && Objects.equals(userBan.getStatus(), 1);
            records.add(SystemUserVo.builder()
                    .id(record.getId())
                    .userName(record.getUserName())
                    .avatarAddress(record.getAvatarAddress())
                    .phone(record.getPhone())
                    .gender(record.getGender())
                    .createTime(record.getCreateTime())
                    .loginDateTime(record.getLoginDateTime())
                    .videoNumber(record.getVideoNumber())
                    .fansNumber(record.getFansNumber())
                    .followNumber(record.getFollowNumber())
                    .likeNumber(record.getLikeNumber())
                    .coinNumber(record.getCoinNumber())
                    .grade(record.getGrade())
                    .introduce(record.getIntroduce())
                    .adminFlag(record.getAdminFlag())
                    .banFlag(banned ? 1 : 0)
                    .banReason(banned ? userBan.getReason() : null)
                    .banTime(banned ? userBan.getBanTime() : null)
                    .build());
        }

        //type为2时只展示封禁中的用户，需要过滤后重新计算总数
        if (dto.getType() != null && dto.getType() == 2) {
            List<SystemUserVo> bannedRecords = records.stream().filter(v -> Objects.equals(v.getBanFlag(), 1)).collect(Collectors.toList());
            Page<SystemUserVo> voPage = new Page<>(usersPage.getCurrent(), usersPage.getSize());
            voPage.setRecords(bannedRecords);
            voPage.setTotal(countBannedUsers(dto.getKeyword()));
            return voPage;
        }

        return toUserVoPage(usersPage, records);
    }

    @Override
    @Transactional
    public Boolean putAdmin(SystemOperateDto dto, Integer targetId) {

        if (targetId == null)
            throw new BaseException("请选择要操作的用户");

        Users users = userMapper.selectById(targetId);
        if (users == null)
            throw new BaseException("该用户不存在");

        int adminFlag = Objects.equals(users.getAdminFlag(), 1) ? 0 : 1;
        //最后一个管理员不能被降级，避免系统失去管理入口
        if (adminFlag == 0 && countAdmin() <= 1) {
            systemLogService.write(dto, "user", "putAdmin", "user", targetId, users.getUserName(),
                    "系统至少需要保留一名管理员，操作已拒绝", 0);
            throw new BaseException("系统至少需要保留一名管理员");
        }

        users.setAdminFlag(adminFlag);
        if (userMapper.updateById(users) <= 0)
            throw new BaseException("修改失败，请稍后重试");

        systemLogService.write(dto, "user", "putAdmin", "user", targetId, users.getUserName(),
                adminFlag == 1 ? "设为管理员" : "取消管理员", 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean banUser(SystemOperateDto dto, Integer targetId) {

        if (targetId == null)
            throw new BaseException("请选择要封禁的用户");

        if (!StringUtils.hasText(dto.getReason()))
            throw new BaseException("请填写封禁原因");

        Users users = userMapper.selectById(targetId);
        if (users == null)
            throw new BaseException("该用户不存在");

        //不允许封禁管理员，避免把管理账号踢下线
        if (Objects.equals(users.getAdminFlag(), 1)) {
            systemLogService.write(dto, "user", "banUser", "user", targetId, users.getUserName(), "管理员不可被封禁", 0);
            throw new BaseException("管理员不可被封禁");
        }

        LambdaQueryWrapper<UserBan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBan::getUserId, targetId);
        UserBan userBan = userBanMapper.selectOne(wrapper);

        if (userBan == null) {
            userBan = UserBan.builder()
                    .userId(targetId)
                    .userName(users.getUserName())
                    .reason(dto.getReason())
                    .status(1)
                    .operatorId(dto.getOperatorId())
                    .operatorName(operatorName(dto.getOperatorId()))
                    .banTime(LocalDateTime.now())
                    .build();
            userBanMapper.insert(userBan);
        } else {
            //已解除的封禁记录重新启用并覆盖原因
            userBan.setStatus(1);
            userBan.setReason(dto.getReason());
            userBan.setUserName(users.getUserName());
            userBan.setOperatorId(dto.getOperatorId());
            userBan.setOperatorName(operatorName(dto.getOperatorId()));
            userBan.setBanTime(LocalDateTime.now());
            //updateById 会忽略null字段，这里用UpdateWrapper显式把上次解除时间清空
            userBanMapper.update(null, new LambdaUpdateWrapper<UserBan>()
                    .eq(UserBan::getId, userBan.getId())
                    .set(UserBan::getStatus, 1)
                    .set(UserBan::getReason, dto.getReason())
                    .set(UserBan::getUserName, users.getUserName())
                    .set(UserBan::getOperatorId, dto.getOperatorId())
                    .set(UserBan::getOperatorName, operatorName(dto.getOperatorId()))
                    .set(UserBan::getBanTime, LocalDateTime.now())
                    .set(UserBan::getUnbanTime, null));
        }

        //封禁后立即让该用户的所有会话失效
        kickout(targetId);

        systemLogService.write(dto, "user", "banUser", "user", targetId, users.getUserName(), "封禁：" + dto.getReason(), 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean unbanUser(SystemOperateDto dto, Integer targetId) {

        if (targetId == null)
            throw new BaseException("请选择要解除封禁的用户");

        LambdaQueryWrapper<UserBan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBan::getUserId, targetId);
        UserBan userBan = userBanMapper.selectOne(wrapper);
        if (userBan == null || !Objects.equals(userBan.getStatus(), 1))
            throw new BaseException("该用户当前未被封禁");

        userBan.setStatus(0);
        userBan.setUnbanTime(LocalDateTime.now());
        userBanMapper.updateById(userBan);

        Users users = userMapper.selectById(targetId);
        systemLogService.write(dto, "user", "unbanUser", "user", targetId,
                users == null ? null : users.getUserName(), "解除封禁", 1);
        return true;
    }

    @Override
    public Page<UserBan> searchBanList(Integer operatorId, Integer pageNum, String keyword) {

        Page<UserBan> page = buildPage(pageNum);
        LambdaQueryWrapper<UserBan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(UserBan::getUserName, value).or().like(UserBan::getReason, value));
        }
        wrapper.orderByDesc(UserBan::getBanTime);
        return userBanMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<SystemCommentVo> searchComments(SystemCommentSearchDto dto) {

        Page<Comments> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Comments::getContent, dto.getKeyword().trim());
        }
        applyUserIdOrName(wrapper, "user_id", dto.getUserId());
        applyVideoIdOrTitle(wrapper, dto.getVideoId());
        //type -1全部 0主评论 1回复评论
        if (dto.getType() != null && dto.getType() == 0) {
            wrapper.isNull(Comments::getMainCommentId);
        } else if (dto.getType() != null && dto.getType() == 1) {
            wrapper.isNotNull(Comments::getMainCommentId);
        }

        wrapper.orderByDesc(Comments::getCommentTime);
        Page<Comments> commentsPage = commentsMapper.selectPage(page, wrapper);

        List<SystemCommentVo> records = new ArrayList<>();
        for (Comments record : commentsPage.getRecords()) {
            SystemCommentVo vo = SystemCommentVo.builder()
                    .id(record.getId())
                    .userId(record.getUserId())
                    .userName(record.getUserName())
                    .videoId(record.getVideoId())
                    .dynamicId(record.getDynamicId())
                    .content(record.getContent())
                    .commentTime(record.getCommentTime())
                    .likeCommentNumber(record.getLikeCommentNumber())
                    .imgAddress(record.getImgAddress())
                    .replyCommentId(record.getReplyCommentId())
                    .mainCommentId(record.getMainCommentId())
                    .upFlag(record.getUpFlag())
                    .dynamicFlag(record.getDynamicFlag())
                    .build();
            records.add(vo);
        }

        //补充评论人头像、所属视频标题、被回复人用户名
        fillUserAvatars(records, SystemCommentVo::getUserId, SystemCommentVo::setUserAvatar);
        fillVideoTitles(records, SystemCommentVo::getVideoId, SystemCommentVo::setVideoTitle);
        fillReplyUserNames(records);

        Page<SystemCommentVo> voPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize());
        voPage.setRecords(records);
        voPage.setTotal(commentsPage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public Integer deleteComments(SystemOperateDto dto, List<Integer> commentIds) {

        List<Integer> ids = normalizeIds(commentIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的评论");

        List<Comments> commentList = commentsMapper.selectList(new LambdaQueryWrapper<Comments>().in(Comments::getId, ids));
        if (commentList.isEmpty())
            throw new BaseException("选中的评论可能已不存在");

        //按视频/动态分组计数，保证评论数只被扣减一次且数量准确
        Map<Integer, Integer> videoCountMap = new LinkedHashMap<>();
        Map<Integer, Integer> dynamicCountMap = new LinkedHashMap<>();

        int deleteNumber = 0;
        for (Comments comment : commentList) {
            //dynamicFlag 1表示已经生成过动态，删除评论时保留动态只隐藏评论内容
            if (Objects.equals(comment.getDynamicFlag(), 1)) {
                comment.setDynamicFlag(2);
                comment.setContent(null);
                commentsMapper.updateById(comment);
                continue;
            }

            commentControlsMapper.delete(new LambdaQueryWrapper<CommentControls>()
                    .eq(CommentControls::getCommentId, comment.getId()));
            likesMapper.delete(new LambdaQueryWrapper<Likes>()
                    .eq(Likes::getFondId, comment.getId())
                    .eq(Likes::getLikeType, 2));

            commentsMapper.deleteById(comment.getId());
            deleteNumber++;

            if (comment.getVideoId() != null)
                videoCountMap.merge(comment.getVideoId(), 1, Integer::sum);
            else if (comment.getDynamicId() != null)
                dynamicCountMap.merge(comment.getDynamicId(), 1, Integer::sum);
        }

        //扣减评论数并清理评论缓存
        videoCountMap.forEach((videoId, number) -> {
            if (number <= 0)
                return;
            Videos videos = videosMapper.selectById(videoId);
            if (videos != null) {
                videos.setCommentNumber(Math.max(0, videos.getCommentNumber() - number));
                videosMapper.updateById(videos);
            }
            cacheService.deleteCommentCacheByVideoId(videoId, null, 0);
        });

        dynamicCountMap.forEach((dynamicId, number) -> {
            if (number <= 0)
                return;
            Dynamic dynamic = dynamicMapper.selectById(dynamicId);
            if (dynamic != null) {
                dynamic.setCommentNumber(Math.max(0, dynamic.getCommentNumber() - number));
                dynamicMapper.updateById(dynamic);
            }
            cacheService.deleteCommentCacheByVideoId(null, dynamicId, 0);
        });

        String targetNames = commentList.stream().map(c -> "#" + c.getId()).limit(10).collect(Collectors.joining(","));
        systemLogService.write(dto, "comment", "deleteComment", "comment", ids.get(0),
                targetNames + (ids.size() > 10 ? " 等" : ""),
                "删除评论 " + deleteNumber + " 条" + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return deleteNumber;
    }

    @Override
    public Page<SystemDynamicVo> searchDynamics(SystemDynamicSearchDto dto) {

        Page<Dynamic> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<Dynamic> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            //标题、正文、发布者昵称一起模糊匹配
            String value = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Dynamic::getTitle, value)
                    .or()
                    .like(Dynamic::getContent, value)
                    .or()
                    .apply("follow_id IN (SELECT id FROM users WHERE user_name LIKE CONCAT('%',{0},'%'))", value));
        }
        applyUserIdOrName(wrapper, "follow_id", dto.getUserId());
        //type -1全部 0视频动态 1评论动态
        if (dto.getType() != null && dto.getType() == 0) {
            wrapper.isNotNull(Dynamic::getVideoId).isNull(Dynamic::getCommentId);
        } else if (dto.getType() != null && dto.getType() == 1) {
            wrapper.isNotNull(Dynamic::getCommentId).isNull(Dynamic::getVideoId);
        }

        //同一个视频动态在表里有「UP主自己发布的1条」+「每个粉丝各1条副本」，
        //不区分来源的话列表会出现看起来重复的很多行，所以默认只显示UP主自己发布的
        if (dto.getSource() != null && dto.getSource() == 0) {
            wrapper.isNull(Dynamic::getFansId);
        } else if (dto.getSource() != null && dto.getSource() == 1) {
            wrapper.isNotNull(Dynamic::getFansId);
        }

        wrapper.orderByDesc(Dynamic::getPublishTime);
        Page<Dynamic> dynamicPage = dynamicMapper.selectPage(page, wrapper);

        List<SystemDynamicVo> records = new ArrayList<>();
        for (Dynamic record : dynamicPage.getRecords()) {
            int dynamicFlag = record.getCommentId() != null ? 1 : (record.getVideoId() != null ? 0 : 2);
            records.add(SystemDynamicVo.builder()
                    .id(record.getId())
                    .followId(record.getFollowId())
                    .fansId(record.getFansId())
                    .videoId(record.getVideoId())
                    .commentId(record.getCommentId())
                    .title(record.getTitle())
                    .imgAddress(record.getImgAddress())
                    .content(record.getContent())
                    .likeNumber(record.getLikeNumber())
                    .commentNumber(record.getCommentNumber())
                    .shareNumber(record.getShareNumber())
                    .upFlag(record.getUpFlag())
                    .publishTime(record.getPublishTime())
                    .dynamicFlag(dynamicFlag)
                    .build());
        }

        fillDynamicUserAvatars(records);
        fillVideoTitles(records, SystemDynamicVo::getVideoId, SystemDynamicVo::setVideoTitle);
        fillVideoCovers(records);
        fillDynamicCommentContents(records);

        Page<SystemDynamicVo> voPage = new Page<>(dynamicPage.getCurrent(), dynamicPage.getSize());
        voPage.setRecords(records);
        voPage.setTotal(dynamicPage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public Integer deleteDynamics(SystemOperateDto dto, List<Integer> dynamicIds) {

        List<Integer> ids = normalizeIds(dynamicIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的动态");

        //up主自己的动态 fans_id 为空才允许删除，避免管理员删掉用户收到的动态副本
        List<Dynamic> dynamicList = dynamicMapper.selectList(
                new LambdaQueryWrapper<Dynamic>().in(Dynamic::getId, ids).isNull(Dynamic::getFansId));
        if (dynamicList.isEmpty())
            throw new BaseException("选中的动态可能已不存在，或属于用户收到的动态副本，只允许删除UP主自己发布的动态");

        List<Integer> realIds = dynamicList.stream().map(Dynamic::getId).collect(Collectors.toList());
        int deleteNumber = dynamicMapper.delete(new LambdaQueryWrapper<Dynamic>().in(Dynamic::getId, realIds));

        String targetNames = dynamicList.stream()
                .map(d -> StringUtils.hasText(d.getContent()) ? d.getContent() : "#" + d.getId())
                .limit(5).collect(Collectors.joining(" / "));
        systemLogService.write(dto, "dynamic", "deleteDynamic", "dynamic", realIds.get(0), targetNames,
                "删除动态 " + deleteNumber + " 条" + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return deleteNumber;
    }

    @Override
    public Page<SystemMessageVo> searchMessages(SystemMessageSearchDto dto) {

        Page<PrivateMessage> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(PrivateMessage::getContent, dto.getKeyword().trim());
        }
        applyUserIdOrName(wrapper, "sender_id", dto.getUserId());
        applyUserIdOrName(wrapper, "receiver_id", dto.getReceiverId());

        wrapper.orderByDesc(PrivateMessage::getSendTime);
        Page<PrivateMessage> messagePage = privateMessageMapper.selectPage(page, wrapper);

        List<SystemMessageVo> records = new ArrayList<>();
        Set<Integer> userIds = new HashSet<>();
        for (PrivateMessage record : messagePage.getRecords()) {
            records.add(SystemMessageVo.builder()
                    .id(record.getId())
                    .senderId(record.getSenderId())
                    .receiverId(record.getReceiverId())
                    .content(record.getContent())
                    .sendTime(record.getSendTime())
                    .status(record.getStatus())
                    .messageType(record.getMessageType())
                    .build());
            if (record.getSenderId() != null)
                userIds.add(record.getSenderId());
            if (record.getReceiverId() != null)
                userIds.add(record.getReceiverId());
        }

        if (!userIds.isEmpty()) {
            Map<Integer, Users> userMap = userMapper.selectList(new LambdaQueryWrapper<Users>().in(Users::getId, userIds))
                    .stream().collect(Collectors.toMap(Users::getId, Function.identity(), (a, b) -> b));
            for (SystemMessageVo vo : records) {
                Users sender = userMap.get(vo.getSenderId());
                Users receiver = userMap.get(vo.getReceiverId());
                if (sender != null) {
                    vo.setSenderName(sender.getUserName());
                    vo.setSenderAvatar(sender.getAvatarAddress());
                }
                if (receiver != null) {
                    vo.setReceiverName(receiver.getUserName());
                    vo.setReceiverAvatar(receiver.getAvatarAddress());
                }
            }
        }

        Page<SystemMessageVo> voPage = new Page<>(messagePage.getCurrent(), messagePage.getSize());
        voPage.setRecords(records);
        voPage.setTotal(messagePage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public Integer deleteMessages(SystemOperateDto dto, List<Integer> messageIds) {

        List<Integer> ids = normalizeIds(messageIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的私信");

        List<PrivateMessage> messageList = privateMessageMapper.selectList(
                new LambdaQueryWrapper<PrivateMessage>().in(PrivateMessage::getId, ids));
        if (messageList.isEmpty())
            throw new BaseException("选中的私信可能已不存在");

        List<Integer> realIds = messageList.stream().map(PrivateMessage::getId).collect(Collectors.toList());
        int deleteNumber = privateMessageMapper.delete(new LambdaQueryWrapper<PrivateMessage>().in(PrivateMessage::getId, realIds));

        //私信列表有缓存，删除后要按会话双方清理
        Set<Integer> userIds = new HashSet<>();
        for (PrivateMessage message : messageList) {
            userIds.add(message.getSenderId());
            userIds.add(message.getReceiverId());
        }
        for (Integer userId : userIds)
            cacheService.deleteMessageByUserId(userId, null);

        String targetNames = messageList.stream()
                .map(m -> StringUtils.hasText(m.getContent()) ? m.getContent() : "#" + m.getId())
                .limit(5).collect(Collectors.joining(" / "));
        systemLogService.write(dto, "message", "deleteMessage", "privateMessage", realIds.get(0), targetNames,
                "删除私信 " + deleteNumber + " 条" + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return deleteNumber;
    }

    @Override
    public Page<KeyWord> searchKeyWords(SystemKeyWordSearchDto dto) {

        Page<KeyWord> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<KeyWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(KeyWord::getWord, dto.getKeyword().trim());
        }
        wrapper.orderByDesc(KeyWord::getCount).orderByDesc(KeyWord::getId);
        return keyWordMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public Boolean addKeyWord(SystemOperateDto dto, String word) {

        if (!StringUtils.hasText(word))
            throw new BaseException("请填写搜索词");

        String value = word.trim();
        //与现有搜索词校验保持一致，至少两个字
        if (value.length() <= 1)
            throw new BaseException("搜索词至少2个字");

        LambdaQueryWrapper<KeyWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KeyWord::getWord, value);
        if (keyWordMapper.selectOne(wrapper) != null)
            throw new BaseException("该搜索词已存在");

        keyWordMapper.insert(KeyWord.builder().word(value).count(1).build());
        systemLogService.write(dto, "keyWord", "addKeyWord", "keyWord", null, value, "新增搜索词", 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean putKeyWord(SystemOperateDto dto, Integer keyWordId, String word) {

        if (keyWordId == null)
            throw new BaseException("请选择要修改的搜索词");

        if (!StringUtils.hasText(word))
            throw new BaseException("请填写搜索词");

        String value = word.trim();
        if (value.length() <= 1)
            throw new BaseException("搜索词至少2个字");

        KeyWord keyWord = keyWordMapper.selectById(keyWordId);
        if (keyWord == null)
            throw new BaseException("该搜索词不存在");

        LambdaQueryWrapper<KeyWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KeyWord::getWord, value).ne(KeyWord::getId, keyWordId);
        if (keyWordMapper.selectOne(wrapper) != null)
            throw new BaseException("该搜索词已存在");

        String oldWord = keyWord.getWord();
        keyWord.setWord(value);
        if (keyWordMapper.updateById(keyWord) <= 0)
            throw new BaseException("修改失败，请稍后重试");

        systemLogService.write(dto, "keyWord", "putKeyWord", "keyWord", keyWordId, value,
                "由「" + oldWord + "」修改为「" + value + "」", 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean putKeyWordCount(SystemOperateDto dto, Integer keyWordId, Integer count) {

        if (keyWordId == null)
            throw new BaseException("请选择要修改的搜索词");

        if (count == null || count < 0)
            throw new BaseException("搜索次数必须是0或正整数");

        KeyWord keyWord = keyWordMapper.selectById(keyWordId);
        if (keyWord == null)
            throw new BaseException("该搜索词不存在");

        Integer oldCount = keyWord.getCount();
        keyWord.setCount(count);
        if (keyWordMapper.updateById(keyWord) <= 0)
            throw new BaseException("修改失败，请稍后重试");

        systemLogService.write(dto, "keyWord", "putKeyWordCount", "keyWord", keyWordId, keyWord.getWord(),
                "搜索次数由 " + oldCount + " 修改为 " + count, 1);
        return true;
    }

    @Override
    @Transactional
    public Integer deleteKeyWords(SystemOperateDto dto, List<Integer> keyWordIds) {

        List<Integer> ids = normalizeIds(keyWordIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的搜索词");

        List<KeyWord> keyWordList = keyWordMapper.selectList(new LambdaQueryWrapper<KeyWord>().in(KeyWord::getId, ids));
        if (keyWordList.isEmpty())
            throw new BaseException("选中的搜索词可能已不存在");

        List<Integer> realIds = keyWordList.stream().map(KeyWord::getId).collect(Collectors.toList());
        int deleteNumber = keyWordMapper.delete(new LambdaQueryWrapper<KeyWord>().in(KeyWord::getId, realIds));

        String targetNames = keyWordList.stream().map(KeyWord::getWord).limit(10).collect(Collectors.joining(","));
        systemLogService.write(dto, "keyWord", "deleteKeyWord", "keyWord", realIds.get(0), targetNames,
                "删除搜索词 " + deleteNumber + " 条", 1);
        return deleteNumber;
    }

    @Override
    public Page<SystemOperationLog> searchLogs(SystemLogSearchDto dto) {

        Page<SystemOperationLog> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<SystemOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getModule()))
            wrapper.eq(SystemOperationLog::getModule, dto.getModule());
        if (dto.getSuccess() != null)
            wrapper.eq(SystemOperationLog::getSuccess, dto.getSuccess());
        wrapper.orderByDesc(SystemOperationLog::getCreateTime);
        Page<SystemOperationLog> binPage = systemOperationLogMapper.selectPage(page, wrapper);

        //批量带出操作人头像，避免逐条查users
        fillOperatorAvatars(binPage.getRecords());

        return binPage;
    }

    /**
     * 给操作日志填充操作人头像
     */
    private void fillOperatorAvatars(List<SystemOperationLog> records) {

        if (records == null || records.isEmpty())
            return;

        Set<Integer> operatorIds = records.stream()
                .map(SystemOperationLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (operatorIds.isEmpty())
            return;

        Map<Integer, String> avatarMap = userMapper.selectList(new LambdaQueryWrapper<Users>()
                        .select(Users::getId, Users::getAvatarAddress)
                        .in(Users::getId, operatorIds))
                .stream()
                .collect(Collectors.toMap(Users::getId, Users::getAvatarAddress, (a, b) -> b));

        for (SystemOperationLog record : records)
            record.setOperatorAvatar(avatarMap.get(record.getOperatorId()));
    }

    @Override
    public Long countLogs(SystemLogSearchDto dto) {

        LambdaQueryWrapper<SystemOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getModule()))
            wrapper.eq(SystemOperationLog::getModule, dto.getModule());
        if (dto.getSuccess() != null)
            wrapper.eq(SystemOperationLog::getSuccess, dto.getSuccess());
        return zeroIfNull(systemOperationLogMapper.selectCount(wrapper));
    }

    // ==================== 视频管理 ====================

    @Override
    public SystemVideoListVo searchVideos(SystemVideoSearchDto dto) {

        Page<Videos> page = buildPage(dto.getPageNum());
        //status 为 -1 表示不按状态过滤，直接透传会把 -1 当成真实状态导致查不到任何数据
        Integer filterStatus = dto.getStatus() == null || dto.getStatus() < 0 ? null : dto.getStatus();
        Page<Videos> videosPage = videosMapper.selectPage(page, buildVideoQuery(dto, filterStatus, true));
        List<Videos> videoList = videosPage.getRecords();

        List<SystemVideoVo> records = new ArrayList<>();
        for (Videos record : videoList) {
            records.add(SystemVideoVo.builder()
                    .id(record.getId())
                    .userId(record.getUserId())
                    .userName(record.getUserName())
                    .title(record.getTitle())
                    .content(record.getContent())
                    .tag(record.getTag())
                    .coverAddress(record.getCoverAddress())
                    .videoAddress(record.getVideoAddress())
                    .videoTime(record.getVideoTime())
                    .subZoneKey(record.getSubZoneKey())
                    .subZoneValue(record.getSubZoneValue())
                    .type(record.getType())
                    .allowTwo(record.getAllowTwo())
                    .status(record.getStatus())
                    .examineFiledMessage(record.getExamineFiledMessage())
                    .playNumber(record.getPlayNumber())
                    .likeNumber(record.getLikeNumber())
                    .commentNumber(record.getCommentNumber())
                    .collectNumber(record.getCollectNumber())
                    .coinThrowNumber(record.getCoinThrowNumber())
                    .createTime(record.getCreateTime())
                    .build());
        }
        fillVideoUpAvatars(records);

        //三个状态的数量都基于「忽略状态」的筛选条件计算，这样切换页签时角标不会跟着变
        Long waitNumber = countVideos(buildVideoQuery(dto, VIDEO_STATUS_WAIT, false));
        Long passNumber = countVideos(buildVideoQuery(dto, VIDEO_STATUS_PASS, false));
        Long rejectNumber = countVideos(buildVideoQuery(dto, VIDEO_STATUS_REJECT, false));

        return SystemVideoListVo.builder()
                .records(records)
                .total(videosPage.getTotal())
                .current(videosPage.getCurrent())
                .size(videosPage.getSize())
                .waitNumber(waitNumber)
                .passNumber(passNumber)
                .rejectNumber(rejectNumber)
                .build();
    }

    /**
     * 构造视频查询条件，status 为 null 时不加状态过滤
     */
    private LambdaQueryWrapper<Videos> buildVideoQuery(SystemVideoSearchDto dto, Integer status, boolean withSort) {

        LambdaQueryWrapper<Videos> wrapper = new LambdaQueryWrapper<>();

        //关键词同时匹配视频标题和UP主昵称
        applyVideoKeyword(wrapper, dto.getKeyword());
        if (StringUtils.hasText(dto.getSubZoneKey()))
            wrapper.eq(Videos::getSubZoneKey, dto.getSubZoneKey());
        if (dto.getUserId() != null)
            wrapper.eq(Videos::getUserId, dto.getUserId());
        if (status != null)
            wrapper.eq(Videos::getStatus, status);

        //统计数量的查询不需要排序，排序只影响分页结果的展示
        if (!withSort)
            return wrapper;

        //sortWay 0最新发布 1播放量最多 2点赞最多
        if (Objects.equals(dto.getSortWay(), 1))
            wrapper.orderByDesc(Videos::getPlayNumber);
        else if (Objects.equals(dto.getSortWay(), 2))
            wrapper.orderByDesc(Videos::getLikeNumber);
        else
            wrapper.orderByDesc(Videos::getCreateTime);

        return wrapper;
    }

    private Long countVideos(LambdaQueryWrapper<Videos> wrapper) {
        return zeroIfNull(videosMapper.selectCount(wrapper));
    }

    /**
     * 批量填充UP主头像
     */
    private void fillVideoUpAvatars(List<SystemVideoVo> list) {

        Set<Integer> userIds = list.stream().map(SystemVideoVo::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty())
            return;

        Map<Integer, String> avatarMap = userMapper.selectList(new LambdaQueryWrapper<Users>()
                        .select(Users::getId, Users::getAvatarAddress).in(Users::getId, userIds))
                .stream().collect(Collectors.toMap(Users::getId, Users::getAvatarAddress, (a, b) -> b));

        for (SystemVideoVo vo : list)
            vo.setUserAvatar(vo.getUserId() == null ? null : avatarMap.get(vo.getUserId()));
    }

    @Override
    public List<String> getSubZoneKeys() {

        //分区选项直接从库里取，保证与实际数据一致，不会出现下拉里有但永远搜不到结果的项
        List<String> keys = videosMapper.selectList(new LambdaQueryWrapper<Videos>()
                        .select(Videos::getSubZoneKey)
                        .isNotNull(Videos::getSubZoneKey)
                        .groupBy(Videos::getSubZoneKey)
                        .orderByAsc(Videos::getSubZoneKey))
                .stream()
                .map(Videos::getSubZoneKey)
                .filter(StringUtils::hasText)
                //历史上写入过纯数字的脏数据，这类值不是真正的分区名，不放进下拉里
                .filter(key -> !key.chars().allMatch(Character::isDigit))
                .distinct()
                .collect(Collectors.toList());

        return keys == null ? List.of() : keys;
    }

    @Override
    @Transactional
    public Boolean examineVideo(SystemOperateDto dto, Integer videoId) {

        Videos videos = requireVideo(videoId);

        //只有未审核的视频可以放行，重复放行会重复给粉丝推送动态并重复累加UP主计数
        if (!Objects.equals(videos.getStatus(), VIDEO_STATUS_WAIT)) {
            systemLogService.write(dto, "video", "examineVideo", "video", videoId, videos.getTitle(),
                    "重复审核通过已被拒绝", 0);
            throw new BaseException("该视频不是待审核状态，无需重复审核");
        }

        //复用创作中心的审核逻辑：重置发布时间、累加UP主计数、向粉丝推送动态
        Boolean b = videosService.examineVideo(videoId);
        if (!Objects.equals(b, true))
            throw new BaseException("审核失败，请稍后重试");

        evictVideoCache(videoId);
        systemLogService.write(dto, "video", "examineVideo", "video", videoId, videos.getTitle(), "审核通过", 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectVideo(SystemOperateDto dto, Integer videoId) {

        Videos videos = requireVideo(videoId);

        if (Objects.equals(videos.getStatus(), VIDEO_STATUS_PASS)) {
            systemLogService.write(dto, "video", "rejectVideo", "video", videoId, videos.getTitle(),
                    "已通过审核的视频不能直接退回，请使用强制下架", 0);
            throw new BaseException("该视频已审核通过，如需下架请使用「强制下架」");
        }

        if (!StringUtils.hasText(dto.getReason()))
            throw new BaseException("请填写退回原因");

        videos.setStatus(VIDEO_STATUS_REJECT);
        videos.setExamineFiledMessage(dto.getReason());
        if (videosMapper.updateById(videos) <= 0)
            throw new BaseException("操作失败，请稍后重试");

        evictVideoCache(videoId);
        systemLogService.write(dto, "video", "rejectVideo", "video", videoId, videos.getTitle(),
                "审核退回：" + dto.getReason(), 1);
        return true;
    }

    @Override
    @Transactional
    public Boolean takeDownVideo(SystemOperateDto dto, Integer videoId) {

        Videos videos = requireVideo(videoId);

        if (!Objects.equals(videos.getStatus(), VIDEO_STATUS_PASS)) {
            systemLogService.write(dto, "video", "takeDownVideo", "video", videoId, videos.getTitle(),
                    "非已通过状态无需强制下架", 0);
            throw new BaseException("只有已通过审核的视频才能强制下架");
        }

        if (!StringUtils.hasText(dto.getReason()))
            throw new BaseException("请填写下架原因");

        //与审核通过严格互逆：回退UP主计数、回退粉丝动态红点、删除已推送的动态副本、软删收藏
        rollbackPublishedVideo(videos);

        videos.setStatus(VIDEO_STATUS_REJECT);
        videos.setExamineFiledMessage(dto.getReason());
        if (videosMapper.updateById(videos) <= 0)
            throw new BaseException("操作失败，请稍后重试");

        evictVideoCache(videoId);
        systemLogService.write(dto, "video", "takeDownVideo", "video", videoId, videos.getTitle(),
                "强制下架：" + dto.getReason(), 1);
        return true;
    }

    /**
     * 回滚一个已发布视频带来的全部影响
     */
    private void rollbackPublishedVideo(Videos videos) {

        Integer videoId = videos.getId();

        //回退UP主的投稿数与动态数
        Users up = userMapper.selectById(videos.getUserId());
        if (up != null) {
            up.setVideoNumber(Math.max(0, nullToZero(up.getVideoNumber()) - 1));
            up.setOwnDynamicNumber(Math.max(0, nullToZero(up.getOwnDynamicNumber()) - 1));
            userMapper.updateById(up);
        }

        //回退收到该动态的粉丝红点计数，并删除动态副本
        List<Dynamic> fanDynamics = dynamicMapper.selectList(new LambdaQueryWrapper<Dynamic>()
                .eq(Dynamic::getVideoId, videoId)
                .isNotNull(Dynamic::getFansId));
        for (Dynamic dynamic : fanDynamics) {
            Users fan = userMapper.selectById(dynamic.getFansId());
            if (fan != null) {
                fan.setDynamicNumber(Math.max(0, nullToZero(fan.getDynamicNumber()) - 1));
                userMapper.updateById(fan);
            }
        }

        dynamicMapper.delete(new LambdaQueryWrapper<Dynamic>()
                .eq(Dynamic::getVideoId, videoId)
                .isNull(Dynamic::getCommentId));

        //收藏同步置为已删除，避免用户收藏夹里出现已下架的视频
        collectMapper.update(Collects.builder().deleteFlag(1).build(),
                new LambdaQueryWrapper<Collects>().eq(Collects::getVideoId, videoId));
    }

    @Override
    @Transactional
    public Integer deleteVideos(SystemOperateDto dto, List<Integer> videoIds) {

        List<Integer> ids = normalizeIds(videoIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的视频");

        List<Videos> videoList = videosMapper.selectList(new LambdaQueryWrapper<Videos>().in(Videos::getId, ids));
        if (videoList.isEmpty())
            throw new BaseException("选中的视频可能已不存在");

        int deleteNumber = 0;
        List<Integer> realIds = new ArrayList<>();
        for (Videos video : videoList) {
            //复用现有级联删除逻辑：删除磁盘文件并清理弹幕/评论/收藏/历史/点赞/投币
            Boolean b = videosService.deleteVideo(video.getId());
            if (Objects.equals(b, true)) {
                deleteNumber++;
                realIds.add(video.getId());
                //评论缓存按视频维度清理
                cacheService.deleteCommentCacheByVideoId(video.getId(), null, 0);
            }
        }

        if (deleteNumber > 0) {
            evictVideoCache(realIds);
            String targetNames = videoList.stream().map(Videos::getTitle).limit(5).collect(Collectors.joining(" / "));
            systemLogService.write(dto, "video", "deleteVideo", "video", realIds.get(0), targetNames,
                    "删除视频 " + deleteNumber + " 条（含磁盘文件与关联数据）"
                            + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        }

        return deleteNumber;
    }

    private Videos requireVideo(Integer videoId) {

        if (videoId == null)
            throw new BaseException("请选择要操作的视频");

        Videos videos = videosMapper.selectById(videoId);
        if (videos == null)
            throw new BaseException("该视频不存在或已被删除");

        return videos;
    }

    // ==================== 回收站 ====================

    @Override
    public Page<SystemRecycleBinVo> searchRecycleBin(SystemRecycleSearchDto dto) {

        Page<RecycleBin> page = buildPage(dto.getPageNum());
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getBizType()))
            wrapper.eq(RecycleBin::getBizType, dto.getBizType());
        if (dto.getStatus() != null && dto.getStatus() >= 0)
            wrapper.eq(RecycleBin::getStatus, dto.getStatus());
        if (StringUtils.hasText(dto.getKeyword()))
            wrapper.like(RecycleBin::getTitle, dto.getKeyword().trim());

        wrapper.orderByDesc(RecycleBin::getDeleteTime);
        Page<RecycleBin> binPage = recycleBinMapper.selectPage(page, wrapper);

        List<SystemRecycleBinVo> records = new ArrayList<>();
        for (RecycleBin record : binPage.getRecords()) {
            SystemRecycleBinVo vo = SystemRecycleBinVo.builder()
                    .id(record.getId())
                    .bizType(record.getBizType())
                    .bizId(record.getBizId())
                    .title(record.getTitle())
                    .coverAddress(record.getCoverAddress())
                    .videoAddress(record.getVideoAddress())
                    .reason(record.getReason())
                    .status(record.getStatus())
                    .deleteTime(record.getDeleteTime())
                    .operatorId(record.getOperatorId())
                    .operatorName(record.getOperatorName())
                    .restoreTime(record.getRestoreTime())
                    .build();
            fillRecycleBinExtra(vo);
            records.add(vo);
        }

        Page<SystemRecycleBinVo> voPage = new Page<>(binPage.getCurrent(), binPage.getSize());
        voPage.setRecords(records);
        voPage.setTotal(binPage.getTotal());
        return voPage;
    }

    /**
     * 补充回收站列表的归属用户名与关联数据量
     */
    private void fillRecycleBinExtra(SystemRecycleBinVo vo) {

        if (RECYCLE_BIZ_VIDEO.equals(vo.getBizType())) {
            Map<String, Object> payload = readPayload(vo);
            Object ownerName = payload.get("ownerName");
            if (ownerName != null)
                vo.setOwnerName(String.valueOf(ownerName));

            Object video = payload.get("video");
            if (video instanceof Map) {
                Object userId = ((Map<?, ?>) video).get("userId");
                if (userId instanceof Number)
                    vo.setRelatedNumber(
                            commentsMapper.selectCount(new LambdaQueryWrapper<Comments>().eq(Comments::getVideoId, vo.getBizId())).intValue());
            }
        } else if (RECYCLE_BIZ_COMMENT.equals(vo.getBizType()) || RECYCLE_BIZ_DYNAMIC.equals(vo.getBizType())) {
            Map<String, Object> payload = readPayload(vo);
            Object ownerName = payload.get("ownerName");
            if (ownerName != null)
                vo.setOwnerName(String.valueOf(ownerName));
        }
    }

    @Override
    @Transactional
    public Integer recycleVideos(SystemOperateDto dto, List<Integer> videoIds) {

        List<Integer> ids = normalizeIds(videoIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的视频");

        List<Videos> videoList = videosMapper.selectList(new LambdaQueryWrapper<Videos>().in(Videos::getId, ids));
        if (videoList.isEmpty())
            throw new BaseException("选中的视频可能已不存在");

        int recycleNumber = 0;
        List<Integer> doneIds = new ArrayList<>();
        for (Videos video : videoList) {

            //已经进过回收站的先清掉旧快照，避免唯一键冲突
            recycleBinMapper.delete(new LambdaQueryWrapper<RecycleBin>()
                    .eq(RecycleBin::getBizType, RECYCLE_BIZ_VIDEO)
                    .eq(RecycleBin::getBizId, video.getId()));

            Map<String, Object> payload = buildVideoPayload(video);
            try {
                recycleBinMapper.insert(RecycleBin.builder()
                        .bizType(RECYCLE_BIZ_VIDEO)
                        .bizId(video.getId())
                        .title(video.getTitle())
                        .coverAddress(video.getCoverAddress())
                        .videoAddress(video.getVideoAddress())
                        .payload(writeJson(payload))
                        .reason(dto.getReason())
                        .status(RECYCLE_STATUS_IN_BIN)
                        .deleteTime(LocalDateTime.now())
                        .operatorId(dto.getOperatorId())
                        .operatorName(operatorName(dto.getOperatorId()))
                        .build());
            } catch (Exception e) {
                log.error("写入回收站快照失败 videoId={} err={}", video.getId(), e.getMessage());
                continue;
            }

            //文件先挪到回收站目录，还原时再挪回来
            moveFileToRecycleBin(video.getVideoAddress(), RECYCLE_SUBDIR_VIDEO);
            moveFileToRecycleBin(video.getCoverAddress(), RECYCLE_SUBDIR_COVER);

            //删除视频的同时评论缓存也要失效，否则页面还会显示已被删的评论
            cacheService.deleteCommentCacheByVideoId(video.getId(), null, 0);

            //复用现有级联删除，把弹幕/评论/收藏/历史/点赞/投币一并清掉
            if (Objects.equals(videosService.deleteVideo(video.getId()), true)) {
                recycleNumber++;
                doneIds.add(video.getId());
            }
        }

        if (recycleNumber == 0)
            throw new BaseException("移入回收站失败，请稍后重试");

        evictVideoCache(doneIds);
        String targetNames = videoList.stream().map(Videos::getTitle).limit(5).collect(Collectors.joining(" / "));
        systemLogService.write(dto, "recycleBin", "recycleVideo", "video", doneIds.get(0), targetNames,
                "移入回收站 " + recycleNumber + " 个视频"
                        + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return recycleNumber;
    }

    @Override
    @Transactional
    public Integer restoreVideos(SystemOperateDto dto, List<Integer> videoIds) {

        List<Integer> ids = normalizeIds(videoIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要还原的视频");

        int restoreNumber = 0;
        for (Integer videoId : ids) {

            LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecycleBin::getBizType, RECYCLE_BIZ_VIDEO)
                    .eq(RecycleBin::getBizId, videoId)
                    .eq(RecycleBin::getStatus, RECYCLE_STATUS_IN_BIN);
            RecycleBin bin = recycleBinMapper.selectOne(wrapper);
            if (bin == null)
                continue;

            Map<String, Object> payload = readPayload(bin);
            Map<String, Object> videoMap = asMap(payload.get("video"));
            if (videoMap == null)
                continue;

            Videos videos = mapToEntity(videoMap, Videos.class);
            if (videos == null || videos.getId() == null)
                continue;

            //主键可能已被占用(例如同id被重新占用)，此时跳过避免覆盖别人的数据
            if (videosMapper.selectById(videos.getId()) != null)
                continue;

            restoreVideoRelated(videoId, payload);
            videosMapper.insert(videos);

            //视频自身的计数字段随快照一起回来了，
            //但删除时扣减过UP主的投稿数/动态数，这里要补回来
            if (Objects.equals(videos.getStatus(), VIDEO_STATUS_PASS) && videos.getUserId() != null) {
                userMapper.update(null, new LambdaUpdateWrapper<Users>()
                        .eq(Users::getId, videos.getUserId())
                        .setSql("video_number = IFNULL(video_number,0) + 1")
                        .setSql("own_dynamic_number = IFNULL(own_dynamic_number,0) + 1"));
            }

            moveFileFromRecycleBin(videos.getVideoAddress(), RECYCLE_SUBDIR_VIDEO);
            moveFileFromRecycleBin(videos.getCoverAddress(), RECYCLE_SUBDIR_COVER);

            markRestored(bin);
            evictVideoCache(videoId);
            //视频的评论是随快照一起插回来的，Redis里的评论列表缓存必须按视频维度全量失效，
            //否则观看者还会看到移走之前的旧评论列表
            cacheService.deleteCommentCacheByVideoId(videoId, null, 0);
            restoreNumber++;
        }

        if (restoreNumber == 0)
            throw new BaseException("还原失败，记录可能已被还原或数据不完整");

        systemLogService.write(dto, "recycleBin", "restoreVideo", "video", ids.get(0), null,
                "从回收站还原 " + restoreNumber + " 个视频", 1);
        return restoreNumber;
    }

    /**
     * 还原视频的关联数据
     */
    private void restoreVideoRelated(Integer videoId, Map<String, Object> payload) {

        //评论(删除视频时是物理删除，直接插回)
        List<Comments> comments = toEntities(payload.get("comments"), Comments.class);
        if (!comments.isEmpty())
            insertInBatches(comments, commentsMapper::insert);

        //收藏(删除视频时只是软删除 delete_flag=1，行还在)
        //这里必须先判断哪些行还在，否则直接insert会撞主键，导致整个还原回滚
        List<Collects> collects = toEntities(payload.get("collects"), Collects.class);
        if (!collects.isEmpty()) {
            List<Integer> ids = collects.stream().map(Collects::getId).filter(Objects::nonNull).collect(Collectors.toList());
            Set<Integer> existingIds = ids.isEmpty() ? Set.of()
                    : collectMapper.selectBatchIds(ids).stream().map(Collects::getId).collect(Collectors.toSet());
            if (!existingIds.isEmpty())
                collectMapper.update(null, new LambdaUpdateWrapper<Collects>()
                        .in(Collects::getId, existingIds)
                        .set(Collects::getDeleteFlag, 0));
            List<Collects> missing = collects.stream()
                    .filter(c -> !existingIds.contains(c.getId()))
                    .collect(Collectors.toList());
            if (!missing.isEmpty())
                insertInBatches(missing, collectMapper::insert);
        }

        //投币
        List<ThrowCoin> throwCoins = toEntities(payload.get("throwCoins"), ThrowCoin.class);
        if (!throwCoins.isEmpty())
            insertInBatches(throwCoins, throwCoinMapper::insert);

        //弹幕
        List<Scrolling> scrollings = toEntities(payload.get("scrollings"), Scrolling.class);
        if (!scrollings.isEmpty())
            insertInBatches(scrollings, scrollingMapper::insert);

        //观看历史
        List<History> historys = toEntities(payload.get("historys"), History.class);
        if (!historys.isEmpty())
            insertInBatches(historys, historyMapper::insert);

        //点赞
        List<Likes> likes = toEntities(payload.get("likes"), Likes.class);
        if (!likes.isEmpty())
            insertInBatches(likes, likesMapper::insert);

        //注意：评论数/收藏数/播放数等计数字段已经包含在还原回来的 videos 行里，
        //这里再累加会造成重复计数，所以不做任何 +1
    }

    /**
     * 把快照里的对象列表转成实体列表
     */
    private <T> List<T> toEntities(Object value, Class<T> clazz) {

        List<Map<String, Object>> list = asMapList(value);
        List<T> result = new ArrayList<>();
        for (Map<String, Object> item : list) {
            T entity = mapToEntity(item, clazz);
            if (entity != null)
                result.add(entity);
        }
        return result;
    }

    /**
     * 逐条插入实体。
     * MyBatis-Plus 3.5.6 的 BaseMapper 还没有 insertOrBatch，这里统一走循环插入，
     * 语义与批量插入一致，也避免一次性拼超长SQL。
     */
    private <T> void insertInBatches(List<T> list, java.util.function.Consumer<T> inserter) {
        for (T item : list)
            inserter.accept(item);
    }

    /**
     * 组装视频的完整快照，包含所有会被级联删除的关联数据
     */
    private Map<String, Object> buildVideoPayload(Videos video) {

        Integer videoId = video.getId();
        Map<String, Object> payload = new HashMap<>();
        payload.put("video", video);
        payload.put("ownerName", video.getUserName());

        List<Comments> comments = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                .eq(Comments::getVideoId, videoId));
        payload.put("comments", comments);

        List<Collects> collects = collectMapper.selectList(new LambdaQueryWrapper<Collects>()
                .eq(Collects::getVideoId, videoId));
        payload.put("collects", collects);

        List<ThrowCoin> throwCoins = throwCoinMapper.selectList(new LambdaQueryWrapper<ThrowCoin>()
                .eq(ThrowCoin::getVideoId, videoId));
        payload.put("throwCoins", throwCoins);

        List<Scrolling> scrollings = scrollingMapper.selectList(new LambdaQueryWrapper<Scrolling>()
                .eq(Scrolling::getVideoId, videoId));
        payload.put("scrollings", scrollings);

        List<History> historys = historyMapper.selectList(new LambdaQueryWrapper<History>()
                .eq(History::getVideoId, videoId));
        payload.put("historys", historys);

        List<Likes> likes = likesMapper.selectList(new LambdaQueryWrapper<Likes>()
                .eq(Likes::getFondId, videoId)
                .eq(Likes::getLikeType, 1));
        payload.put("likes", likes);

        return payload;
    }

    @Override
    @Transactional
    public Integer recycleComments(SystemOperateDto dto, List<Integer> commentIds) {

        List<Integer> ids = normalizeIds(commentIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的评论");

        List<Comments> commentList = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                .in(Comments::getId, ids)
                .eq(Comments::getDeleteSign, 0));
        if (commentList.isEmpty())
            throw new BaseException("选中的评论可能已不存在或已被删除");

        int recycleNumber = 0;
        List<Integer> doneIds = new ArrayList<>();
        for (Comments comment : commentList) {

            recycleBinMapper.delete(new LambdaQueryWrapper<RecycleBin>()
                    .eq(RecycleBin::getBizType, RECYCLE_BIZ_COMMENT)
                    .eq(RecycleBin::getBizId, comment.getId()));

            Map<String, Object> payload = new HashMap<>();
            payload.put("comment", comment);
            payload.put("ownerName", comment.getUserName());

            try {
                recycleBinMapper.insert(RecycleBin.builder()
                        .bizType(RECYCLE_BIZ_COMMENT)
                        .bizId(comment.getId())
                        .title(ellipsisForBin(comment.getContent(), 200))
                        .payload(writeJson(payload))
                        .reason(dto.getReason())
                        .status(RECYCLE_STATUS_IN_BIN)
                        .deleteTime(LocalDateTime.now())
                        .operatorId(dto.getOperatorId())
                        .operatorName(operatorName(dto.getOperatorId()))
                        .build());
            } catch (Exception e) {
                log.error("写入评论回收站快照失败 commentId={} err={}", comment.getId(), e.getMessage());
                continue;
            }

            //评论必须走软删除(delete_sign=1)才能还原。
            //这里原来复用了 deleteComments(物理删除)，导致行直接消失，还原时查不到记录。
            commentService.deleteReply(Collections.singletonList(comment), true, false);
            recycleNumber++;
            doneIds.add(comment.getId());
        }

        if (recycleNumber == 0)
            throw new BaseException("移入回收站失败，请稍后重试");

        String targetNames = commentList.stream().map(c -> "#" + c.getId()).limit(10).collect(Collectors.joining(","));
        systemLogService.write(dto, "recycleBin", "recycleComment", "comment", doneIds.get(0), targetNames,
                "移入回收站 " + recycleNumber + " 条评论"
                        + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return recycleNumber;
    }

    @Override
    @Transactional
    public Integer restoreComments(SystemOperateDto dto, List<Integer> commentIds) {

        List<Integer> ids = normalizeIds(commentIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要还原的评论");

        int restoreNumber = 0;
        for (Integer commentId : ids) {

            LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecycleBin::getBizType, RECYCLE_BIZ_COMMENT)
                    .eq(RecycleBin::getBizId, commentId)
                    .eq(RecycleBin::getStatus, RECYCLE_STATUS_IN_BIN);
            RecycleBin bin = recycleBinMapper.selectOne(wrapper);
            if (bin == null)
                continue;

            Comments comment = commentsMapper.selectById(commentId);
            //已经被物理删除(比如UP删了视频)的评论无法还原
            if (comment == null)
                continue;

            Map<String, Object> payload = readPayload(bin);
            Map<String, Object> commentMap = asMap(payload.get("comment"));
            String content = commentMap == null ? null : String.valueOf(commentMap.get("content"));

            commentsMapper.update(null, new LambdaUpdateWrapper<Comments>()
                    .eq(Comments::getId, commentId)
                    .set(Comments::getDeleteSign, 0)
                    .set(Comments::getContent, content));

            //把评论数加回去
            if (comment.getVideoId() != null) {
                videosMapper.update(null, new LambdaUpdateWrapper<Videos>()
                        .eq(Videos::getId, comment.getVideoId())
                        .setSql("comment_number = IFNULL(comment_number,0) + 1"));
                cacheService.deleteCommentCacheByVideoId(comment.getVideoId(), null, comment.getUserId());
            } else if (comment.getDynamicId() != null) {
                dynamicMapper.update(null, new LambdaUpdateWrapper<Dynamic>()
                        .eq(Dynamic::getId, comment.getDynamicId())
                        .setSql("comment_number = IFNULL(comment_number,0) + 1"));
                cacheService.deleteCommentCacheByVideoId(null, comment.getDynamicId(), comment.getUserId());
            }

            markRestored(bin);
            restoreNumber++;
        }

        if (restoreNumber == 0)
            throw new BaseException("还原失败，记录可能已被还原或原数据已不存在");

        systemLogService.write(dto, "recycleBin", "restoreComment", "comment", ids.get(0), null,
                "从回收站还原 " + restoreNumber + " 条评论", 1);
        return restoreNumber;
    }

    @Override
    @Transactional
    public Integer purgeRecycleBin(SystemOperateDto dto, List<Integer> recycleIds) {

        List<Integer> ids = normalizeIds(recycleIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要清除的记录");

        List<RecycleBin> binList = recycleBinMapper.selectBatchIds(ids);
        if (binList.isEmpty())
            throw new BaseException("选中的记录可能已不存在");

        int purgeNumber = recycleBinMapper.deleteBatchIds(ids);

        //视频已不在业务表里，这里把备份文件一并清掉，避免磁盘残留
        for (RecycleBin bin : binList) {
            if (RECYCLE_BIZ_VIDEO.equals(bin.getBizType())) {
                moveFileToRecycleBin(bin.getVideoAddress(), RECYCLE_SUBDIR_VIDEO);
                moveFileToRecycleBin(bin.getCoverAddress(), RECYCLE_SUBDIR_COVER);
            }
        }

        String targetNames = binList.stream().map(RecycleBin::getTitle).limit(5).collect(Collectors.joining(" / "));
        systemLogService.write(dto, "recycleBin", "purgeRecycleBin", "recycleBin", ids.get(0), targetNames,
                "彻底清除 " + purgeNumber + " 条回收站记录（不可恢复）"
                        + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return purgeNumber;
    }

    @Override
    @Transactional
    public Integer cleanRestoredRecycleBin(SystemOperateDto dto) {

        int number = recycleBinMapper.delete(new LambdaQueryWrapper<RecycleBin>()
                .eq(RecycleBin::getStatus, RECYCLE_STATUS_RESTORED));
        systemLogService.write(dto, "recycleBin", "cleanRestoredRecycleBin", "recycleBin", null, null,
                "清理已还原的回收站记录 " + number + " 条", 1);
        return number;
    }

    @Override
    @Transactional
    public Integer recycleDynamics(SystemOperateDto dto, List<Integer> dynamicIds) {

        List<Integer> ids = normalizeIds(dynamicIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要删除的动态");

        //与现有删除规则保持一致：只处理UP主自己发布的动态(fans_id为空)
        List<Dynamic> dynamicList = dynamicMapper.selectList(new LambdaQueryWrapper<Dynamic>()
                .in(Dynamic::getId, ids)
                .isNull(Dynamic::getFansId));
        if (dynamicList.isEmpty())
            throw new BaseException("选中的动态可能已不存在，或属于用户收到的动态副本，只允许删除UP主自己发布的动态");

        int recycleNumber = 0;
        List<Integer> doneIds = new ArrayList<>();
        for (Dynamic dynamic : dynamicList) {

            recycleBinMapper.delete(new LambdaQueryWrapper<RecycleBin>()
                    .eq(RecycleBin::getBizType, RECYCLE_BIZ_DYNAMIC)
                    .eq(RecycleBin::getBizId, dynamic.getId()));

            Map<String, Object> payload = buildDynamicPayload(dynamic);
            try {
                recycleBinMapper.insert(RecycleBin.builder()
                        .bizType(RECYCLE_BIZ_DYNAMIC)
                        .bizId(dynamic.getId())
                        .title(ellipsisForBin(dynamicTitle(dynamic), 200))
                        .payload(writeJson(payload))
                        .reason(dto.getReason())
                        .status(RECYCLE_STATUS_IN_BIN)
                        .deleteTime(LocalDateTime.now())
                        .operatorId(dto.getOperatorId())
                        .operatorName(operatorName(dto.getOperatorId()))
                        .build());
            } catch (Exception e) {
                log.error("写入动态回收站快照失败 dynamicId={} err={}", dynamic.getId(), e.getMessage());
                continue;
            }

            //动态下的评论一并清理，并把上级(videoId/dynamicId)的评论数扣掉
            int commentNumber = deleteDynamicWithComments(dynamic);
            recycleNumber++;
            doneIds.add(dynamic.getId());
            evictDynamicCache(dynamic, commentNumber);
        }

        if (recycleNumber == 0)
            throw new BaseException("移入回收站失败，请稍后重试");

        String targetNames = dynamicList.stream()
                .map(this::dynamicTitle).limit(5).collect(Collectors.joining(" / "));
        systemLogService.write(dto, "recycleBin", "recycleDynamic", "dynamic", doneIds.get(0), targetNames,
                "移入回收站 " + recycleNumber + " 条动态"
                        + (StringUtils.hasText(dto.getReason()) ? "，原因：" + dto.getReason() : ""), 1);
        return recycleNumber;
    }

    @Override
    @Transactional
    public Integer restoreDynamics(SystemOperateDto dto, List<Integer> dynamicIds) {

        List<Integer> ids = normalizeIds(dynamicIds);
        if (ids.isEmpty())
            throw new BaseException("请先选择要还原的动态");

        int restoreNumber = 0;
        for (Integer dynamicId : ids) {

            LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecycleBin::getBizType, RECYCLE_BIZ_DYNAMIC)
                    .eq(RecycleBin::getBizId, dynamicId)
                    .eq(RecycleBin::getStatus, RECYCLE_STATUS_IN_BIN);
            RecycleBin bin = recycleBinMapper.selectOne(wrapper);
            if (bin == null)
                continue;

            Map<String, Object> payload = readPayload(bin);
            Map<String, Object> dynamicMap = asMap(payload.get("dynamic"));
            if (dynamicMap == null)
                continue;

            Dynamic dynamic = mapToEntity(dynamicMap, Dynamic.class);
            if (dynamic == null || dynamic.getId() == null)
                continue;
            //主键被占用时跳过，避免覆盖别人的数据
            if (dynamicMapper.selectById(dynamic.getId()) != null)
                continue;

            List<Comments> comments = toEntities(payload.get("comments"), Comments.class);
            if (!comments.isEmpty())
                insertInBatches(comments, commentsMapper::insert);

            dynamicMapper.insert(dynamic);

            //评论数加回去
            if (!comments.isEmpty()) {
                if (dynamic.getVideoId() != null) {
                    videosMapper.update(null, new LambdaUpdateWrapper<Videos>()
                            .eq(Videos::getId, dynamic.getVideoId())
                            .setSql("comment_number = IFNULL(comment_number,0) + " + comments.size()));
                } else if (dynamic.getCommentId() != null) {
                    Dynamic parent = dynamicMapper.selectById(dynamic.getCommentId());
                    if (parent != null) {
                        dynamicMapper.update(null, new LambdaUpdateWrapper<Dynamic>()
                                .eq(Dynamic::getId, parent.getId())
                                .setSql("comment_number = IFNULL(comment_number,0) + " + comments.size()));
                    }
                }
            }

            markRestored(bin);
            evictDynamicCache(dynamic, comments.size());
            restoreNumber++;
        }

        if (restoreNumber == 0)
            throw new BaseException("还原失败，记录可能已被还原或数据不完整");

        systemLogService.write(dto, "recycleBin", "restoreDynamic", "dynamic", ids.get(0), null,
                "从回收站还原 " + restoreNumber + " 条动态", 1);
        return restoreNumber;
    }

    /**
     * 组装动态快照，包含该动态下的评论
     */
    private Map<String, Object> buildDynamicPayload(Dynamic dynamic) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("dynamic", dynamic);

        List<Comments> comments = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                .eq(Comments::getDynamicId, dynamic.getId()));
        payload.put("comments", comments);

        Users owner = userMapper.selectById(dynamic.getFollowId());
        payload.put("ownerName", owner == null ? null : owner.getUserName());

        return payload;
    }

    /**
     * 删除动态及其评论，返回删除的评论数
     */
    private int deleteDynamicWithComments(Dynamic dynamic) {

        Integer dynamicId = dynamic.getId();
        List<Comments> comments = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                .eq(Comments::getDynamicId, dynamicId));
        if (!comments.isEmpty()) {
            for (Comments comment : comments) {
                commentControlsMapper.delete(new LambdaQueryWrapper<CommentControls>()
                        .eq(CommentControls::getCommentId, comment.getId()));
                likesMapper.delete(new LambdaQueryWrapper<Likes>()
                        .eq(Likes::getFondId, comment.getId())
                        .eq(Likes::getLikeType, 2));
            }
            commentsMapper.delete(new LambdaQueryWrapper<Comments>().eq(Comments::getDynamicId, dynamicId));

            if (dynamic.getVideoId() != null) {
                videosMapper.update(null, new LambdaUpdateWrapper<Videos>()
                        .eq(Videos::getId, dynamic.getVideoId())
                        .setSql("comment_number = GREATEST(IFNULL(comment_number,0) - " + comments.size() + ", 0)"));
            } else if (dynamic.getCommentId() != null) {
                dynamicMapper.update(null, new LambdaUpdateWrapper<Dynamic>()
                        .eq(Dynamic::getId, dynamic.getCommentId())
                        .setSql("comment_number = GREATEST(IFNULL(comment_number,0) - " + comments.size() + ", 0)"));
            }
        }

        dynamicMapper.deleteById(dynamicId);
        return comments.size();
    }

    private String dynamicTitle(Dynamic dynamic) {

        if (dynamic == null)
            return "(无标题)";
        if (dynamic.getVideoId() != null) {
            Videos videos = videosMapper.selectById(dynamic.getVideoId());
            if (videos != null && StringUtils.hasText(videos.getTitle()))
                return videos.getTitle();
        }
        return StringUtils.hasText(dynamic.getContent()) ? dynamic.getContent() : "动态 #" + dynamic.getId();
    }

    /**
     * 动态删除/还原后清理评论缓存，避免还原后仍看到删除前的评论列表
     */
    private void evictDynamicCache(Dynamic dynamic, int commentNumber) {

        if (dynamic == null)
            return;
        if (dynamic.getVideoId() != null)
            cacheService.deleteCommentCacheByVideoId(dynamic.getVideoId(), null, 0);
        else if (dynamic.getCommentId() != null)
            cacheService.deleteCommentCacheByVideoId(null, dynamic.getCommentId(), 0);
    }

    private void markRestored(RecycleBin bin) {
        recycleBinMapper.update(null, new LambdaUpdateWrapper<RecycleBin>()
                .eq(RecycleBin::getId, bin.getId())
                .set(RecycleBin::getStatus, RECYCLE_STATUS_RESTORED)
                .set(RecycleBin::getRestoreTime, LocalDateTime.now()));
    }

    /**
     * 把业务文件挪到回收站目录(同目录下的recycle_bin子目录)
     */
    private void moveFileToRecycleBin(String address, String subDir) {

        if (!StringUtils.hasText(address))
            return;

        try {
            String fileName = address.substring(address.lastIndexOf('/') + 1);
            if (fileName.isEmpty())
                return;
            Path source = Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + fileName);
            Path target = Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + RECYCLE_SUBDIR_ROOT + subDir + "/" + fileName);
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            //文件可能本来就不存在，不影响数据本身的快照与还原
            log.warn("移动文件到回收站失败 address={} err={}", address, e.getMessage());
        }
    }

    /**
     * 把文件从回收站目录挪回原位
     */
    private void moveFileFromRecycleBin(String address, String subDir) {

        if (!StringUtils.hasText(address))
            return;

        try {
            String fileName = address.substring(address.lastIndexOf('/') + 1);
            if (fileName.isEmpty())
                return;
            Path source = Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + RECYCLE_SUBDIR_ROOT + subDir + "/" + fileName);
            Path target = Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + fileName);
            if (Files.exists(source))
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.warn("从回收站还原文件失败 address={} err={}", address, e.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BaseException("序列化失败，操作已终止");
        }
    }

    private Map<String, Object> readPayload(SystemRecycleBinVo vo) {
        return vo == null ? Map.of() : readPayloadById(vo.getId());
    }

    private Map<String, Object> readPayload(RecycleBin bin) {

        if (bin == null || !StringUtils.hasText(bin.getPayload()))
            return Map.of();
        try {
            return objectMapper.readValue(bin.getPayload(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("回收站快照解析失败 id={} err={}", bin.getId(), e.getMessage());
            return Map.of();
        }
    }

    private Map<String, Object> readPayloadById(Integer recycleId) {

        RecycleBin bin = recycleBinMapper.selectById(recycleId);
        if (bin == null || !StringUtils.hasText(bin.getPayload()))
            return Map.of();
        try {
            return objectMapper.readValue(bin.getPayload(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map)
            return (Map<String, Object>) value;
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asMapList(Object value) {
        if (value instanceof List)
            return (List<Map<String, Object>>) value;
        return List.of();
    }

    /**
     * 把快照里的Map还原成实体对象
     */
    private <T> T mapToEntity(Map<String, Object> map, Class<T> clazz) {

        if (map == null || map.isEmpty())
            return null;
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            log.warn("快照转换失败 clazz={} err={}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private String ellipsisForBin(String value, int max) {
        if (value == null)
            return "(无标题)";
        return value.length() > max ? value.substring(0, max) : value;
    }

    /**
     * 「ID 或昵称」模糊匹配。
     * 输入纯数字时按ID等值匹配，否则按 users.user_name 模糊匹配，
     * 这样后台的筛选框既能输ID也能输昵称。
     *
     * @param wrapper 查询条件
     * @param column  目标表里指向 users.id 的字段名
     * @param value   用户输入
     */
    private void applyUserIdOrName(LambdaQueryWrapper<?> wrapper, String column, String value) {

        if (!StringUtils.hasText(value))
            return;

        String text = value.trim();
        Integer numeric = parseIntegerOrNull(text);

        if (numeric != null) {
            wrapper.and(w -> w.apply(column + " = {0}", numeric)
                    .or()
                    .apply(column + " IN (SELECT id FROM users WHERE user_name LIKE CONCAT('%',{0},'%'))", text));
        } else {
            wrapper.apply(column + " IN (SELECT id FROM users WHERE user_name LIKE CONCAT('%',{0},'%'))", text);
        }
    }

    /**
     * 「视频ID 或 视频标题」模糊匹配
     */
    private void applyVideoIdOrTitle(LambdaQueryWrapper<Comments> wrapper, String value) {

        if (!StringUtils.hasText(value))
            return;

        String text = value.trim();
        Integer numeric = parseIntegerOrNull(text);

        if (numeric != null) {
            wrapper.and(w -> w.apply("video_id = {0}", numeric)
                    .or()
                    .apply("video_id IN (SELECT id FROM videos WHERE title LIKE CONCAT('%',{0},'%'))", text));
        } else {
            wrapper.apply("video_id IN (SELECT id FROM videos WHERE title LIKE CONCAT('%',{0},'%'))", text);
        }
    }

    /**
     * 视频列表的关键词：标题或UP主昵称
     */
    private void applyVideoKeyword(LambdaQueryWrapper<Videos> wrapper, String keyword) {

        if (!StringUtils.hasText(keyword))
            return;

        String value = keyword.trim();
        wrapper.and(w -> w.like(Videos::getTitle, value)
                .or()
                .like(Videos::getUserName, value));
    }

    private Integer parseIntegerOrNull(String value) {

        if (value == null || value.isEmpty())
            return null;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i)))
                return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 视频状态变化后清理相关缓存，与 VideosController 的 @CacheEvict 保持一致
     */
    private void evictVideoCache(Integer videoId) {
        evictVideoCache(List.of(videoId));
    }

    private void evictVideoCache(List<Integer> videoIds) {

        //videoTitle 缓存的是 selectVideoById 结果，状态变更后必须失效，否则用户仍能看到旧状态
        for (Integer videoId : videoIds) {
            org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_VIDEO_TITLE);
            if (cache != null)
                cache.evict(videoId);
        }

        //收藏页按 userId 聚合，视频增删会影响收藏展示，整体清空
        org.springframework.cache.Cache collectCache = cacheManager.getCache(CACHE_COLLECT);
        if (collectCache != null)
            collectCache.clear();
    }

    // ==================== 仪表盘统计辅助 ====================

    /**
     * 统计 videos 表播放量合计，用聚合查询避免把全表加载到内存
     */
    private Long sumPlayNumber() {

        QueryWrapper<Videos> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(SUM(play_number),0) AS total");
        return firstNumber(videosMapper.selectMaps(wrapper), "total");
    }

    /**
     * 统计 comments 表点赞数合计
     */
    private Long sumLikeCommentNumber() {

        QueryWrapper<Comments> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(SUM(like_comment_number),0) AS total");
        return firstNumber(commentsMapper.selectMaps(wrapper), "total");
    }

    /**
     * 按列名取出聚合结果里的数值
     */
    private Long firstNumber(List<Map<String, Object>> rows, String column) {

        if (rows == null || rows.isEmpty())
            return 0L;
        return readLong(rows.get(0).get(column));
    }

    /**
     * 按天分组统计近7日新增用户
     */
    private List<SystemTrendVo> buildUserTrend() {

        LocalDate start = trendStart();
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.select("DATE_FORMAT(create_time,'%Y-%m-%d') AS " + TREND_DATE_COLUMN
                        + ", COUNT(*) AS " + TREND_COUNT_COLUMN)
                .ge("create_time", start.atStartOfDay())
                .groupBy("DATE_FORMAT(create_time,'%Y-%m-%d')");
        List<Map<String, Object>> rows = userMapper.selectMaps(wrapper);
        return toTrend(rows, start);
    }

    /**
     * 按天分组统计近7日新增视频
     */
    private List<SystemTrendVo> buildVideoTrend() {

        LocalDate start = trendStart();
        QueryWrapper<Videos> wrapper = new QueryWrapper<>();
        wrapper.select("DATE_FORMAT(create_time,'%Y-%m-%d') AS " + TREND_DATE_COLUMN
                        + ", COUNT(*) AS " + TREND_COUNT_COLUMN)
                .ge("create_time", start.atStartOfDay())
                .groupBy("DATE_FORMAT(create_time,'%Y-%m-%d')");
        List<Map<String, Object>> rows = videosMapper.selectMaps(wrapper);
        return toTrend(rows, start);
    }

    /**
     * 按天分组统计近7日新增评论
     */
    private List<SystemTrendVo> buildCommentTrend() {

        LocalDate start = trendStart();
        QueryWrapper<Comments> wrapper = new QueryWrapper<>();
        wrapper.select("DATE_FORMAT(comment_time,'%Y-%m-%d') AS " + TREND_DATE_COLUMN
                        + ", COUNT(*) AS " + TREND_COUNT_COLUMN)
                .ge("comment_time", start.atStartOfDay())
                .groupBy("DATE_FORMAT(comment_time,'%Y-%m-%d')");
        List<Map<String, Object>> rows = commentsMapper.selectMaps(wrapper);
        return toTrend(rows, start);
    }

    /**
     * 趋势统计的起始日期(含当天共TREND_DAYS天)
     */
    private LocalDate trendStart() {
        return LocalDate.now().minusDays(TREND_DAYS - 1L);
    }

    /**
     * 把分组统计结果补齐成连续TREND_DAYS天，没有数据的日期补0
     */
    private List<SystemTrendVo> toTrend(List<Map<String, Object>> rows, LocalDate start) {

        Map<String, Long> countMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                //必须按列名取值，MyBatis 返回的 Map 不保证列顺序
                String date = readDate(row.get(TREND_DATE_COLUMN));
                Long number = readLong(row.get(TREND_COUNT_COLUMN));
                if (date == null)
                    continue;
                countMap.put(date, number);
            }
        }

        List<SystemTrendVo> trend = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            String date = start.plusDays(i).format(DAY_FORMATTER);
            trend.add(SystemTrendVo.builder().date(date).number(countMap.getOrDefault(date, 0L)).build());
        }
        return trend;
    }

    /**
     * 读取分组统计的日期列，DATE_FORMAT 在不同驱动下可能返回String或日期类型
     */
    private String readDate(Object value) {

        if (value == null)
            return null;
        if (value instanceof LocalDate)
            return ((LocalDate) value).format(DAY_FORMATTER);
        if (value instanceof LocalDateTime)
            return ((LocalDateTime) value).toLocalDate().format(DAY_FORMATTER);
        if (value instanceof java.util.Date)
            return new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime().toLocalDate().format(DAY_FORMATTER);

        String text = String.valueOf(value);
        return text.length() > 10 ? text.substring(0, 10) : text;
    }

    private Long readLong(Object value) {
        if (value == null)
            return 0L;
        if (value instanceof Number)
            return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 播放量最高的10个视频
     */
    private List<SystemHotVideoVo> buildHotVideos() {

        LambdaQueryWrapper<Videos> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Videos::getPlayNumber).last("LIMIT 10");
        List<Videos> videosList = videosMapper.selectList(wrapper);
        List<SystemHotVideoVo> hotVideos = new ArrayList<>();
        for (Videos videos : videosList) {
            hotVideos.add(SystemHotVideoVo.builder()
                    .id(videos.getId())
                    .title(videos.getTitle())
                    .userName(videos.getUserName())
                    .coverAddress(videos.getCoverAddress())
                    .playNumber(videos.getPlayNumber())
                    .likeNumber(videos.getLikeNumber())
                    .commentNumber(videos.getCommentNumber())
                    .createTime(videos.getCreateTime())
                    .build());
        }
        return hotVideos;
    }

    /**
     * 搜索次数最高的10个词
     */
    private List<SystemHotKeyWordVo> buildHotKeyWords() {

        LambdaQueryWrapper<KeyWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KeyWord::getCount).last("LIMIT 10");
        List<KeyWord> keyWords = keyWordMapper.selectList(wrapper);
        List<SystemHotKeyWordVo> hotKeyWords = new ArrayList<>();
        for (KeyWord keyWord : keyWords) {
            hotKeyWords.add(SystemHotKeyWordVo.builder()
                    .id(keyWord.getId())
                    .word(keyWord.getWord())
                    .count(keyWord.getCount())
                    .build());
        }
        return hotKeyWords;
    }

    // ==================== 列表字段填充辅助 ====================

    /**
     * 批量填充用户头像，避免 N+1 查询
     */
    private <T> void fillUserAvatars(List<T> list, Function<T, Integer> idGetter,
                                    java.util.function.BiConsumer<T, String> setter) {

        Set<Integer> userIds = list.stream().map(idGetter).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty())
            return;

        Map<Integer, String> avatarMap = userMapper.selectList(new LambdaQueryWrapper<Users>()
                        .select(Users::getId, Users::getAvatarAddress).in(Users::getId, userIds))
                .stream().collect(Collectors.toMap(Users::getId, Users::getAvatarAddress, (a, b) -> b));

        for (T item : list) {
            Integer userId = idGetter.apply(item);
            setter.accept(item, userId == null ? null : avatarMap.get(userId));
        }
    }

    /**
     * 动态列表同时需要用户名与头像，这里单独处理
     */
    private void fillDynamicUserAvatars(List<SystemDynamicVo> list) {

        Set<Integer> userIds = list.stream().map(SystemDynamicVo::getFollowId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty())
            return;

        Map<Integer, Users> userMap = userMapper.selectList(new LambdaQueryWrapper<Users>().in(Users::getId, userIds))
                .stream().collect(Collectors.toMap(Users::getId, Function.identity(), (a, b) -> b));

        for (SystemDynamicVo vo : list) {
            Users user = userMap.get(vo.getFollowId());
            if (user != null) {
                vo.setFollowUserName(user.getUserName());
                vo.setFollowUserAvatar(user.getAvatarAddress());
            }
        }
    }

    /**
     * 批量填充视频标题
     */
    private <T> void fillVideoTitles(List<T> list, Function<T, Integer> idGetter,
                                     java.util.function.BiConsumer<T, String> setter) {

        Set<Integer> videoIds = list.stream().map(idGetter).filter(Objects::nonNull).collect(Collectors.toSet());
        if (videoIds.isEmpty())
            return;

        Map<Integer, String> titleMap = videosMapper.selectList(new LambdaQueryWrapper<Videos>()
                        .select(Videos::getId, Videos::getTitle).in(Videos::getId, videoIds))
                .stream().collect(Collectors.toMap(Videos::getId, Videos::getTitle, (a, b) -> b));

        for (T item : list) {
            Integer videoId = idGetter.apply(item);
            setter.accept(item, videoId == null ? null : titleMap.get(videoId));
        }
    }

    /**
     * 批量填充视频封面
     */
    private void fillVideoCovers(List<SystemDynamicVo> list) {

        Set<Integer> videoIds = list.stream().map(SystemDynamicVo::getVideoId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (videoIds.isEmpty())
            return;

        Map<Integer, String> coverMap = videosMapper.selectList(new LambdaQueryWrapper<Videos>()
                        .select(Videos::getId, Videos::getCoverAddress).in(Videos::getId, videoIds))
                .stream().collect(Collectors.toMap(Videos::getId, Videos::getCoverAddress, (a, b) -> b));

        for (SystemDynamicVo vo : list)
            vo.setVideoCover(vo.getVideoId() == null ? null : coverMap.get(vo.getVideoId()));
    }

    /**
     * 批量填充动态关联的评论内容
     */
    private void fillDynamicCommentContents(List<SystemDynamicVo> list) {

        Set<Integer> commentIds = list.stream().map(SystemDynamicVo::getCommentId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (commentIds.isEmpty())
            return;

        Map<Integer, String> contentMap = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                        .select(Comments::getId, Comments::getContent).in(Comments::getId, commentIds))
                .stream().collect(Collectors.toMap(Comments::getId, Comments::getContent, (a, b) -> b));

        for (SystemDynamicVo vo : list)
            vo.setCommentContent(vo.getCommentId() == null ? null : contentMap.get(vo.getCommentId()));
    }

    /**
     * 填充回复评论的被回复人用户名
     */
    private void fillReplyUserNames(List<SystemCommentVo> list) {

        Set<Integer> commentIds = list.stream().map(SystemCommentVo::getReplyCommentId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (commentIds.isEmpty())
            return;

        Map<Integer, String> nameMap = commentsMapper.selectList(new LambdaQueryWrapper<Comments>()
                        .select(Comments::getId, Comments::getUserName).in(Comments::getId, commentIds))
                .stream().collect(Collectors.toMap(Comments::getId, Comments::getUserName, (a, b) -> b));

        for (SystemCommentVo vo : list) {
            if (vo.getReplyCommentId() != null)
                vo.setReplyUserName(nameMap.get(vo.getReplyCommentId()));
        }
    }

    // ==================== 通用辅助 ====================

    /**
     * 构建分页对象，页码非法时回落到第1页
     */
    private <T> Page<T> buildPage(Integer pageNum) {

        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        return new Page<>(current, PAGE_SIZE);
    }

    /**
     * 清理并限制批量操作的id数量
     */
    private List<Integer> normalizeIds(List<Integer> ids) {

        if (ids == null || ids.isEmpty())
            return List.of();
        List<Integer> result = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (result.size() > MAX_BATCH_SIZE)
            return result.subList(0, MAX_BATCH_SIZE);
        return result;
    }

    private Long countAdmin() {
        return zeroIfNull(userMapper.selectCount(new LambdaQueryWrapper<Users>().eq(Users::getAdminFlag, 1)));
    }

    /**
     * type为2时统计封禁中的用户数量
     */
    private Long countBannedUsers(String keyword) {

        List<Integer> bannedUserIds = userBanMapper.selectList(new LambdaQueryWrapper<UserBan>().eq(UserBan::getStatus, 1))
                .stream().map(UserBan::getUserId).collect(Collectors.toList());
        if (bannedUserIds.isEmpty())
            return 0L;

        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Users::getId, bannedUserIds);
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(Users::getUserName, value).or().like(Users::getPhone, value));
        }
        return zeroIfNull(userMapper.selectCount(wrapper));
    }

    private Page<SystemUserVo> toUserVoPage(Page<Users> usersPage, List<SystemUserVo> records) {
        Page<SystemUserVo> voPage = new Page<>(usersPage.getCurrent(), usersPage.getSize());
        voPage.setRecords(records);
        voPage.setTotal(usersPage.getTotal());
        return voPage;
    }

    private String operatorName(Integer operatorId) {
        if (operatorId == null)
            return null;
        Users operator = userMapper.selectById(operatorId);
        return operator == null ? null : operator.getUserName();
    }

    /**
     * 让被封禁用户的全部会话立即失效
     */
    private void kickout(Integer userId) {
        try {
            StpUtil.kickout(userId);
        } catch (Exception e) {
            log.warn("踢出用户会话失败 userId={} err={}", userId, e.getMessage());
        }
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

}
