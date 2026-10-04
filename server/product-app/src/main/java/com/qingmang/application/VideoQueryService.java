package com.qingmang.application;

import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.common.enums.ReactionType;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.domain.user.mapper.UserStatsMapper;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.domain.video.mapper.VideoStatsMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.VideoQueryMapper;
import com.qingmang.interfaces.dto.VideoListQuery;
import com.qingmang.interfaces.vo.CategoryVO;
import com.qingmang.interfaces.vo.UserReactionVO;
import com.qingmang.interfaces.vo.VideoDetailVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoQueryService {

    private final VideoQueryMapper queryMapper;
    private final VideoMapper videoMapper;
    private final VideoStatsMapper statsMapper;
    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final FavoriteFolderMapper folderMapper;
    private final CounterMapper counterMapper;

    public VideoQueryService(VideoQueryMapper queryMapper, VideoMapper videoMapper,
                             VideoStatsMapper statsMapper, UserMapper userMapper,
                             UserStatsMapper userStatsMapper, FavoriteFolderMapper folderMapper,
                             CounterMapper counterMapper) {
        this.queryMapper = queryMapper;
        this.videoMapper = videoMapper;
        this.statsMapper = statsMapper;
        this.userMapper = userMapper;
        this.userStatsMapper = userStatsMapper;
        this.folderMapper = folderMapper;
        this.counterMapper = counterMapper;
    }

    @Cacheable(cacheNames = "category", key = "'all'")
    public List<CategoryVO> categories() {
        return queryMapper.selectEnabledCategories();
    }

    public ApiResponse<PageResult<VideoListItemVO>> list(VideoListQuery q, Long userId) {
        int size = Math.min(Math.max(q.getPageSize() == null ? BizConstants.DEFAULT_PAGE_SIZE : q.getPageSize(), 1),
                BizConstants.MAX_PAGE_SIZE);
        Long cursor = q.getCursorId();
        int fetch = size + 1; // 多取一条判断还有没有下一页

        List<VideoListItemVO> rows = queryMapper.selectVideoList(
                q.getCategoryId(), q.getOwnerId(), cursor,
                q.getSort() == null ? "latest" : q.getSort(), fetch);

        boolean hasMore = rows.size() > size;
        if (hasMore) {
            rows = rows.subList(0, size);
        }
        fillReactions(rows, userId);

        long total = cursor == null ? rows.size() : -1; // 游标分页不算总数
        return ApiResponse.ok(PageResult.of(rows, total, q.getPageNum() == null ? 1 : q.getPageNum(), size));
    }

    /** 一次 IN 补齐互动状态，整页只多一次查询。 */
    private void fillReactions(List<VideoListItemVO> rows, Long userId) {
        if (userId == null || rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(VideoListItemVO::getId).toList();
        List<UserReactionVO> reactions = queryMapper.selectUserReactions(userId, ids);
        if (reactions.isEmpty()) {
            return;
        }
        Map<Long, UserReactionVO> byVideo = new HashMap<>(reactions.size());
        for (UserReactionVO r : reactions) {
            byVideo.put(r.getVideoId(), r);
        }
        for (VideoListItemVO row : rows) {
            UserReactionVO r = byVideo.get(row.getId());
            if (r == null) {
                continue;
            }
            int type = r.getReactionType() == null ? 0 : r.getReactionType();
            row.setLiked(type == ReactionType.LIKE.getCode());
            row.setCollected(type == ReactionType.FAVORITE.getCode());
            if (type == ReactionType.COIN.getCode()) {
                row.setCoinGiven(true);
                row.setMyCoinCount(r.getCoinCount() == null ? 0 : r.getCoinCount());
            }
        }
    }

    public VideoDetailVO detail(Long videoId, Long userId) {
        VideoDetailVO vo = queryMapper.selectVideoDetail(videoId, userId);
        if (vo == null) {
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        if (userId != null) {
            List<UserReactionVO> rs = queryMapper.selectUserReactions(userId, Collections.singletonList(videoId));
            for (UserReactionVO r : rs) {
                int type = r.getReactionType() == null ? 0 : r.getReactionType();
                vo.setLiked(type == ReactionType.LIKE.getCode());
                vo.setCollected(type == ReactionType.FAVORITE.getCode());
                if (type == ReactionType.COIN.getCode()) {
                    vo.setCoinGiven(true);
                    vo.setMyCoinCount(r.getCoinCount() == null ? 0 : r.getCoinCount());
                }
            }
        }
        return vo;
    }

    /** 播放量 +1。用数据库自增而不是先查后写，少一次往返也不会并发覆盖。 */
    @Transactional
    public void increasePlayCount(Long videoId) {
        ensureStats(videoId);
        counterMapper.increaseVideoStat(videoId, "play_count", 1);
    }

    /** 保证统计行存在（老数据可能没有对应 stats 行）。 */
    public void ensureStats(Long videoId) {
        Long c = statsMapper.selectCount(Wrappers.<com.qingmang.domain.video.VideoStats>lambdaQuery()
                .eq(com.qingmang.domain.video.VideoStats::getVideoId, videoId));
        if (c == null || c == 0) {
            com.qingmang.domain.video.VideoStats s = new com.qingmang.domain.video.VideoStats();
            s.setVideoId(videoId);
            statsMapper.insert(s);
        }
    }
}