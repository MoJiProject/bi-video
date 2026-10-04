package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.interaction.Comment;
import com.qingmang.domain.interaction.CommentLike;
import com.qingmang.domain.interaction.Danmaku;
import com.qingmang.domain.interaction.mapper.CommentLikeMapper;
import com.qingmang.domain.interaction.mapper.CommentMapper;
import com.qingmang.domain.interaction.mapper.DanmakuMapper;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.domain.video.mapper.VideoStatsMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.InteractionQueryMapper;
import com.qingmang.interfaces.dto.CommentPostRequest;
import com.qingmang.interfaces.dto.DanmakuPostRequest;
import com.qingmang.interfaces.vo.CommentVO;
import com.qingmang.interfaces.vo.DanmakuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentDanmakuService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final DanmakuMapper danmakuMapper;
    private final VideoMapper videoMapper;
    private final VideoStatsMapper statsMapper;
    private final CounterMapper counterMapper;
    private final InteractionQueryMapper queryMapper;
    private final ObjectMapper json;

    public CommentDanmakuService(CommentMapper commentMapper, CommentLikeMapper commentLikeMapper,
                                 DanmakuMapper danmakuMapper, VideoMapper videoMapper,
                                 VideoStatsMapper statsMapper, CounterMapper counterMapper,
                                 InteractionQueryMapper queryMapper, ObjectMapper json) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.danmakuMapper = danmakuMapper;
        this.videoMapper = videoMapper;
        this.statsMapper = statsMapper;
        this.counterMapper = counterMapper;
        this.queryMapper = queryMapper;
        this.json = json;
    }

    /** 一级评论分页 + 一次性批量取回它们的回复。 */
    public Map<String, Object> list(Long videoId, Long cursorId, int size, Long viewerId) {
        List<CommentVO> roots = queryMapper.selectComments(videoId, cursorId, size + 1, viewerId);
        boolean hasMore = roots.size() > size;
        if (hasMore) {
            roots = roots.subList(0, size);
        }
        if (!roots.isEmpty()) {
            List<Long> rootIds = roots.stream().map(CommentVO::getId).toList();
            List<CommentVO> replies = queryMapper.selectReplies(rootIds, viewerId);
            Map<Long, List<CommentVO>> grouped = new HashMap<>();
            for (CommentVO r : replies) {
                grouped.computeIfAbsent(r.getRootId(), k -> new ArrayList<>()).add(r);
            }
            roots.forEach(r -> r.setReplies(grouped.getOrDefault(r.getId(), List.of())));
        }
        roots.forEach(this::parseImages);
        Map<String, Object> data = new HashMap<>(4);
        data.put("records", roots);
        data.put("total", queryMapper.countComments(videoId));
        data.put("hasMore", hasMore);
        data.put("cursorId", roots.isEmpty() ? cursorId : roots.get(roots.size() - 1).getId());
        return data;
    }

    @Transactional
    public Long post(Long userId, CommentPostRequest req) {
        var video = videoMapper.selectById(req.getVideoId());
        if (video == null) {
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        if (video.getAllowComment() != null && !video.getAllowComment()) {
            throw new BizException(ErrorCode.COMMENT_CLOSED);
        }
        Long rootId = null;
        if (req.getRootId() != null) {
            Comment root = commentMapper.selectById(req.getRootId());
            if (root == null) {
                throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
            }
            // 回复要挂在一级评论下，不能把楼中楼再套一层
            rootId = root.getRootId() == null ? root.getId() : root.getRootId();
        }

        Comment c = new Comment();
        c.setVideoId(req.getVideoId());
        c.setAuthorId(userId);
        c.setRootId(rootId);
        c.setReplyToUserId(req.getReplyToUserId());
        c.setContent(req.getContent());
        c.setStatus(1);
        c.setLikeCount(0);
        c.setReplyCount(0);
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            try {
                c.setImageUrls(json.writeValueAsString(req.getImageUrls()));
            } catch (Exception e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "图片地址格式不正确");
            }
        }
        commentMapper.insert(c);

        // 回复也计入视频总评论数；根节点的 replyCount 单独维护
        counterMapper.increaseVideoStat(req.getVideoId(), "comment_count", 1);
        if (rootId != null) {
            commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                    .eq(Comment::getId, rootId)
                    .setSql("reply_count = reply_count + 1"));
        }
        return c.getId();
    }

    /** 赞/踩切换。同一用户对同一条评论只保留一种态度。 */
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long commentId, boolean dislike) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        }
        CommentLike exist = commentLikeMapper.selectOne(Wrappers.<CommentLike>lambdaQuery()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));

        boolean active;
        int after;
        if (exist == null) {
            CommentLike l = new CommentLike();
            l.setCommentId(commentId);
            l.setUserId(userId);
            l.setIsDislike(dislike);
            commentLikeMapper.insert(l);
            active = true;
            after = dislike ? 0 : 1;
        } else if (Boolean.valueOf(dislike).equals(exist.getIsDislike())) {
            // 同一种态度再点一次 = 取消
            commentLikeMapper.delete(Wrappers.<CommentLike>lambdaQuery()
                    .eq(CommentLike::getCommentId, commentId)
                    .eq(CommentLike::getUserId, userId));
            active = false;
            after = 0;
        } else {
            // 赞↔踩 互切
            exist.setIsDislike(dislike);
            commentLikeMapper.updateById(exist);
            active = true;
            after = dislike ? 0 : 1;
        }
        // like_count 只统计赞，所以只有「赞位」的有无变化才动它
        int before = exist != null && !Boolean.TRUE.equals(exist.getIsDislike()) ? 1 : 0;
        if (after - before != 0) {
            bumpLikeCount(commentId, after - before);
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("commentId", commentId);
        data.put("active", active);
        data.put("dislike", active && dislike);
        Comment fresh = commentMapper.selectById(commentId);
        data.put("likeCount", fresh == null ? 0 : fresh.getLikeCount());
        return data;
    }

    private void bumpLikeCount(Long commentId, int delta) {
        commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                .eq(Comment::getId, commentId)
                .setSql("like_count = GREATEST(like_count + " + delta + ", 0)"));
    }

    @Transactional
    public void delete(Long userId, Long commentId, boolean admin) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!admin && !c.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.NO_PERMISSION);
        }
        commentMapper.deleteById(commentId);
        counterMapper.decreaseVideoStat(c.getVideoId(), "comment_count", 1);
        if (c.getRootId() != null) {
            counterMapper.increaseVideoStat(c.getVideoId(), "comment_count", 1);
            commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                    .eq(Comment::getId, c.getRootId()).setSql("reply_count = GREATEST(reply_count - 1, 0)"));
        }
    }

    public List<DanmakuVO> danmaku(Long videoId, int limit) {
        return queryMapper.selectDanmaku(videoId, Math.min(Math.max(limit, 1), 5000));
    }

    @Transactional
    public DanmakuVO postDanmaku(Long userId, DanmakuPostRequest req) {
        var video = videoMapper.selectById(req.getVideoId());
        if (video == null) {
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        if (video.getAllowDanmaku() != null && !video.getAllowDanmaku()) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该视频已关闭弹幕");
        }
        Danmaku d = new Danmaku();
        d.setVideoId(req.getVideoId());
        d.setUserId(userId);
        d.setContent(req.getContent());
        d.setColor(req.getColor() == null || req.getColor().isBlank() ? "#FFFFFF" : req.getColor());
        d.setFontSize(req.getFontSize() == null ? 25 : req.getFontSize());
        d.setMode(req.getMode() == null ? 0 : req.getMode());
        d.setVideoTimeMs(Math.max(0, req.getVideoTimeMs() == null ? 0 : req.getVideoTimeMs()));
        danmakuMapper.insert(d);

        counterMapper.increaseVideoStat(req.getVideoId(), "danmaku_count", 1);
        DanmakuVO vo = new DanmakuVO();
        vo.setId(d.getId());
        vo.setVideoId(d.getVideoId());
        vo.setContent(d.getContent());
        vo.setColor(d.getColor());
        vo.setFontSize(d.getFontSize());
        vo.setMode(d.getMode());
        vo.setVideoTimeMs(d.getVideoTimeMs());
        vo.setUserId(userId);
        vo.setMine(Boolean.TRUE);
        return vo;
    }

    private void parseImages(CommentVO vo) {
        String raw = vo.getImageUrlsJson();
        vo.setImageUrlsJson(null);
        if (raw == null || raw.isBlank()) {
            vo.setImageUrls(List.of());
            return;
        }
        try {
            vo.setImageUrls(json.readValue(raw, new TypeReference<List<String>>() {
            }));
        } catch (Exception e) {
            vo.setImageUrls(List.of());
        }
    }
}