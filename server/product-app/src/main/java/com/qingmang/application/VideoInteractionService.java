package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.common.enums.ReactionType;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.favorite.FavoriteItem;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.favorite.mapper.FavoriteItemMapper;
import com.qingmang.domain.interaction.WatchHistory;
import com.qingmang.domain.interaction.WatchLater;
import com.qingmang.domain.interaction.VideoReaction;
import com.qingmang.domain.interaction.mapper.WatchHistoryMapper;
import com.qingmang.domain.interaction.mapper.WatchLaterMapper;
import com.qingmang.domain.interaction.mapper.VideoReactionMapper;
import com.qingmang.domain.video.Video;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.domain.video.mapper.VideoStatsMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频互动：点赞 / 投币 / 收藏 / 待看清单 / 观看记录。
 *
 * <p>点赞和收藏是「有/无」状态，用 video_reaction 的一行表示，切换时插删；
 * 投币是「0/1/2」的数量，累加在 coin_count 上。所有计数都交给数据库自增，
 * 这里只负责判断能不能做这次操作。</p>
 */
@Service
public class VideoInteractionService {

    private final VideoReactionMapper reactionMapper;
    private final WatchLaterMapper watchLaterMapper;
    private final WatchHistoryMapper historyMapper;
    private final FavoriteFolderMapper folderMapper;
    private final FavoriteItemMapper favoriteItemMapper;
    private final VideoMapper videoMapper;
    private final VideoStatsMapper statsMapper;
    private final CounterMapper counterMapper;
    private final AuthService authService;

    public VideoInteractionService(VideoReactionMapper reactionMapper, WatchLaterMapper watchLaterMapper,
                                   WatchHistoryMapper historyMapper, FavoriteFolderMapper folderMapper,
                                   FavoriteItemMapper favoriteItemMapper, VideoMapper videoMapper,
                                   VideoStatsMapper statsMapper, CounterMapper counterMapper,
                                   AuthService authService) {
        this.reactionMapper = reactionMapper;
        this.watchLaterMapper = watchLaterMapper;
        this.historyMapper = historyMapper;
        this.folderMapper = folderMapper;
        this.favoriteItemMapper = favoriteItemMapper;
        this.videoMapper = videoMapper;
        this.statsMapper = statsMapper;
        this.counterMapper = counterMapper;
        this.authService = authService;
    }

    /** 点赞/取消点赞。返回操作后的状态。 */
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long videoId) {
        Video video = requirePublished(videoId);
        VideoReaction exist = findReaction(userId, videoId, ReactionType.LIKE);
        boolean liked;
        if (exist == null) {
            insertReaction(userId, videoId, ReactionType.LIKE);
            counterMapper.increaseVideoStat(videoId, "like_count", 1);
            counterMapper.increaseUserStat(video.getOwnerId(), "like_received", 1);
            authService.addExp(video.getOwnerId(), 1);
            liked = true;
        } else {
            reactionMapper.deleteById(exist.getId());
            counterMapper.decreaseVideoStat(videoId, "like_count", 1);
            counterMapper.increaseUserStat(video.getOwnerId(), "like_received", -1);
            liked = false;
        }
        return state(videoId, liked, null, null, null, null);
    }

    /**
     * 投币。coinCount 为本次投的枚数（1 或 2）。
     *
     * <p>余额判断与扣减放在一条 SQL 里，避免并发超扣；
     * 同一个视频上限两枚，超出直接拒绝而不是静默截断。</p>
     */
    @Transactional
    public Map<String, Object> throwCoin(Long userId, Long videoId, int coinCount) {
        if (coinCount < 1 || coinCount > BizConstants.MAX_COIN_PER_VIDEO_PER_USER) {
            throw new BizException(ErrorCode.PARAM_INVALID, "单次投币数量非法");
        }
        Video video = requirePublished(videoId);
        VideoReaction exist = findReaction(userId, videoId, ReactionType.COIN);
        int already = exist == null || exist.getCoinCount() == null ? 0 : exist.getCoinCount();
        if (already + coinCount > BizConstants.MAX_COIN_PER_VIDEO_PER_USER) {
            throw new BizException(ErrorCode.OPERATION_FAILED,
                    "同一个视频最多投 " + BizConstants.MAX_COIN_PER_VIDEO_PER_USER + " 枚硬币");
        }
        int cost = coinCount * BizConstants.COIN_PER_THROW;
        if (counterMapper.deductCoinBalance(userId, cost) == 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "硬币余额不足");
        }
        if (exist == null) {
            VideoReaction r = new VideoReaction();
            r.setUserId(userId);
            r.setVideoId(videoId);
            r.setReactionType(ReactionType.COIN.getCode());
            r.setCoinCount(coinCount);
            reactionMapper.insert(r);
        } else {
            exist.setCoinCount(already + coinCount);
            reactionMapper.updateById(exist);
        }
        counterMapper.increaseVideoStat(videoId, "coin_count", cost);
        counterMapper.increaseUserStat(video.getOwnerId(), "coin_received", cost);
        authService.addExp(video.getOwnerId(), cost);
        authService.addExp(userId, cost);
        return state(videoId, null, true, null, null, null);
    }

    /** 收藏/取消收藏到指定收藏夹，不传则用默认收藏夹。 */
    @Transactional
    public Map<String, Object> toggleFavorite(Long userId, Long videoId, Long folderId) {
        requirePublished(videoId);
        Long target = folderId == null ? defaultFolderId(userId) : folderId;
        FavoriteItem exist = favoriteItemMapper.selectOne(Wrappers.<FavoriteItem>lambdaQuery()
                .eq(FavoriteItem::getFolderId, target)
                .eq(FavoriteItem::getVideoId, videoId));
        boolean collected;
        if (exist == null) {
            FavoriteItem item = new FavoriteItem();
            item.setFolderId(target);
            item.setUserId(userId);
            item.setVideoId(videoId);
            favoriteItemMapper.insert(item);
            counterMapper.increaseVideoStat(videoId, "favorite_count", 1);
            counterMapper.increaseUserStat(userId, "favorite_count", 1);
            collected = true;
        } else {
            favoriteItemMapper.deleteById(exist.getId());
            counterMapper.decreaseVideoStat(videoId, "favorite_count", 1);
            counterMapper.increaseUserStat(userId, "favorite_count", -1);
            collected = false;
        }
        syncFolderCount(target);
        syncReactionFlag(userId, videoId, ReactionType.FAVORITE, collected);
        return state(videoId, null, null, collected, null, null);
    }

    /** 加入/移出待看清单。 */
    @Transactional
    public Map<String, Object> toggleWatchLater(Long userId, Long videoId) {
        requirePublished(videoId);
        WatchLater exist = watchLaterMapper.selectOne(Wrappers.<WatchLater>lambdaQuery()
                .eq(WatchLater::getUserId, userId)
                .eq(WatchLater::getVideoId, videoId));
        boolean added;
        if (exist == null) {
            WatchLater w = new WatchLater();
            w.setUserId(userId);
            w.setVideoId(videoId);
            watchLaterMapper.insert(w);
            added = true;
        } else {
            watchLaterMapper.deleteById(exist.getId());
            added = false;
        }
        return state(videoId, null, null, null, added, null);
    }

    /** 观看记录上报。同一个人同一个视频只保留一条，重复上报就更新进度。 */
    @Transactional
    public Map<String, Object> reportWatch(Long userId, Long videoId, int progressSeconds) {
        WatchHistory h = historyMapper.selectOne(Wrappers.<WatchHistory>lambdaQuery()
                .eq(WatchHistory::getUserId, userId)
                .eq(WatchHistory::getVideoId, videoId));
        if (h == null) {
            h = new WatchHistory();
            h.setUserId(userId);
            h.setVideoId(videoId);
            h.setProgressSeconds(Math.max(0, progressSeconds));
            h.setWatchedTimes(1);
            try {
                historyMapper.insert(h);
            } catch (DuplicateKeyException e) {
                historyMapper.update(null, Wrappers.<WatchHistory>lambdaUpdate()
                        .eq(WatchHistory::getUserId, userId)
                        .eq(WatchHistory::getVideoId, videoId)
                        .set(WatchHistory::getProgressSeconds, Math.max(0, progressSeconds))
                        .setSql("watched_times = watched_times + 1"));
            }
        } else {
            h.setProgressSeconds(Math.max(0, progressSeconds));
            h.setWatchedTimes(h.getWatchedTimes() == null ? 1 : h.getWatchedTimes() + 1);
            historyMapper.updateById(h);
        }
        Map<String, Object> data = new HashMap<>(2);
        data.put("progressSeconds", Math.max(0, progressSeconds));
        return data;
    }

    /** 一次返回全部互动状态，前端不用为每个按钮单独发一次请求。 */
    public Map<String, Object> stateOf(Long userId, Long videoId) {
        if (userId == null) {
            return state(videoId, false, false, false, false, null);
        }
        VideoReaction like = findReaction(userId, videoId, ReactionType.LIKE);
        VideoReaction coin = findReaction(userId, videoId, ReactionType.COIN);
        WatchLater later = watchLaterMapper.selectOne(Wrappers.<WatchLater>lambdaQuery()
                .eq(WatchLater::getUserId, userId).eq(WatchLater::getVideoId, videoId));
        boolean collected = favoriteItemMapper.selectCount(Wrappers.<FavoriteItem>lambdaQuery()
                .eq(FavoriteItem::getUserId, userId).eq(FavoriteItem::getVideoId, videoId)) > 0;
        return state(videoId, like != null, coin != null, collected, later != null,
                coin == null || coin.getCoinCount() == null ? 0 : coin.getCoinCount());
    }

    private Map<String, Object> state(Long videoId, Boolean liked, Boolean coinGiven,
                                       Boolean collected, Boolean watchLater, Integer myCoin) {
        Map<String, Object> m = new HashMap<>(8);
        m.put("videoId", videoId);
        m.put("liked", liked);
        m.put("coinGiven", coinGiven);
        m.put("collected", collected);
        m.put("watchLater", watchLater);
        m.put("myCoinCount", myCoin);
        return m;
    }

    private Video requirePublished(Long videoId) {
        Video v = videoMapper.selectById(videoId);
        if (v == null) {
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        if (v.getStatus() == null || v.getStatus() != 1) {
            throw new BizException(ErrorCode.VIDEO_STATUS_NOT_ALLOW);
        }
        return v;
    }

    private VideoReaction findReaction(Long userId, Long videoId, ReactionType type) {
        return reactionMapper.selectOne(Wrappers.<VideoReaction>lambdaQuery()
                .eq(VideoReaction::getUserId, userId)
                .eq(VideoReaction::getVideoId, videoId)
                .eq(VideoReaction::getReactionType, type.getCode()));
    }

    private void insertReaction(Long userId, Long videoId, ReactionType type) {
        VideoReaction r = new VideoReaction();
        r.setUserId(userId);
        r.setVideoId(videoId);
        r.setReactionType(type.getCode());
        r.setCoinCount(0);
        try {
            reactionMapper.insert(r);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "重复操作，请勿提交过快");
        }
    }

    /** 收藏状态在 video_reaction 里留一行，列表页就能一次 IN 查完。 */
    private void syncReactionFlag(Long userId, Long videoId, ReactionType type, boolean on) {
        VideoReaction exist = findReaction(userId, videoId, type);
        if (on && exist == null) {
            insertReaction(userId, videoId, type);
        } else if (!on && exist != null) {
            reactionMapper.deleteById(exist.getId());
        }
    }

    private Long defaultFolderId(Long userId) {
        var folder = folderMapper.selectOne(Wrappers.<com.qingmang.domain.favorite.FavoriteFolder>lambdaQuery()
                .eq(com.qingmang.domain.favorite.FavoriteFolder::getOwnerId, userId)
                .eq(com.qingmang.domain.favorite.FavoriteFolder::getIsDefault, Boolean.TRUE)
                .last("LIMIT 1"));
        if (folder == null) {
            throw new BizException(ErrorCode.FOLDER_NOT_FOUND, "默认收藏夹不存在");
        }
        return folder.getId();
    }

    private void syncFolderCount(Long folderId) {
        Long n = favoriteItemMapper.selectCount(Wrappers.<FavoriteItem>lambdaQuery()
                .eq(FavoriteItem::getFolderId, folderId));
        folderMapper.update(null, Wrappers.<com.qingmang.domain.favorite.FavoriteFolder>lambdaUpdate()
                .eq(com.qingmang.domain.favorite.FavoriteFolder::getId, folderId)
                .set(com.qingmang.domain.favorite.FavoriteFolder::getItemCount, n == null ? 0 : n));
    }
}