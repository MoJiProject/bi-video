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
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.domain.social.UserPost;
import com.qingmang.domain.social.mapper.UserPostMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.InteractionQueryMapper;
import com.qingmang.interfaces.dto.CommentPostRequest;
import com.qingmang.interfaces.dto.DanmakuPostRequest;
import com.qingmang.interfaces.vo.CommentVO;
import com.qingmang.interfaces.vo.DanmakuVO;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class CommentDanmakuService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final DanmakuMapper danmakuMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final CounterMapper counterMapper;
    private final InteractionQueryMapper queryMapper;
    private final ObjectMapper json;
    private final UserPostMapper postMapper;
    private final ApplicationEventPublisher events;
    private final MentionNotificationService mentions;
    private final NotificationCenterService notifications;

    public CommentDanmakuService(CommentMapper commentMapper, CommentLikeMapper commentLikeMapper,
                                 DanmakuMapper danmakuMapper, VideoMapper videoMapper,
                                 UserMapper userMapper, CounterMapper counterMapper,
                                 InteractionQueryMapper queryMapper, ObjectMapper json, UserPostMapper postMapper,
                                 ApplicationEventPublisher events, MentionNotificationService mentions,
                                 NotificationCenterService notifications) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.danmakuMapper = danmakuMapper;
        this.videoMapper = videoMapper;
        this.userMapper = userMapper;
        this.counterMapper = counterMapper;
        this.queryMapper = queryMapper;
        this.json = json;
        this.postMapper = postMapper;
        this.events = events;
        this.mentions = mentions;
        this.notifications = notifications;
    }

    /** 一级评论分页 + 一次性批量取回它们的回复。 */
    public Map<String, Object> list(Long videoId, Long cursorId, int size, Long viewerId) {
        return list(videoId, null, cursorId, size, viewerId);
    }

    public Map<String, Object> list(Long videoId, Long postId, Long cursorId, int size, Long viewerId) {
        return list(videoId, postId, cursorId, size, viewerId, "new");
    }

    public Map<String, Object> list(Long videoId, Long postId, Long cursorId, int size, Long viewerId, String sort) {
        if ((videoId == null) == (postId == null) || size < 1 || size > 100)
            throw new BizException(ErrorCode.PARAM_INVALID);
        if (!List.of("new", "hot").contains(sort)) throw new BizException(ErrorCode.PARAM_INVALID);
        if (postId != null) requirePost(postId, viewerId);
        List<CommentVO> roots = postId == null ? queryMapper.selectComments(videoId, cursorId, size + 1, viewerId, sort)
                : queryMapper.selectPostComments(postId, cursorId, size + 1, viewerId, sort);
        boolean hasMore = roots.size() > size;
        if (hasMore) {
            roots = roots.subList(0, size);
        }
        if (!roots.isEmpty()) {
            List<Long> rootIds = roots.stream().map(CommentVO::getId).toList();
            List<CommentVO> replies = queryMapper.selectReplies(rootIds, viewerId);
            Map<Long, List<CommentVO>> grouped = new HashMap<>();
            for (CommentVO r : replies) {
                parseImages(r);
                grouped.computeIfAbsent(r.getRootId(), k -> new ArrayList<>()).add(r);
            }
            roots.forEach(r -> r.setReplies(grouped.getOrDefault(r.getId(), List.of())));
        }
        roots.forEach(this::parseImages);
        Map<String, Object> data = new HashMap<>(4);
        data.put("records", roots);
        data.put("total", postId == null ? queryMapper.countComments(videoId) : queryMapper.countPostComments(postId));
        data.put("hasMore", hasMore);
        data.put("cursorId", roots.isEmpty() ? cursorId : roots.get(roots.size() - 1).getId());
        return data;
    }

    @Transactional
    public Long post(Long userId, CommentPostRequest req) {
        if (!req.isTargetValid()) throw new BizException(ErrorCode.PARAM_INVALID);
        if (req.getPostId() != null) requirePost(req.getPostId(), userId);
        else {
        var video = videoMapper.selectById(req.getVideoId());
        if (video == null) {
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        if (video.getAllowComment() != null && !video.getAllowComment()) {
            throw new BizException(ErrorCode.COMMENT_CLOSED);
        }
        }
        Long rootId = null;
        Long replyToUserId = null;
        if (req.getRootId() != null) {
            Comment root = commentMapper.selectById(req.getRootId());
            if (root == null || root.getDeletedAt() != null || !Integer.valueOf(1).equals(root.getStatus())
                    || !Objects.equals(root.getVideoId(), req.getVideoId())
                    || !Objects.equals(root.getPostId(), req.getPostId())) {
                throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
            }
            // 回复要挂在一级评论下，不能把楼中楼再套一层
            rootId = root.getRootId() == null ? root.getId() : root.getRootId();
            Comment parent = commentMapper.selectById(rootId);
            if (parent == null || parent.getDeletedAt() != null) throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
            replyToUserId = root.getAuthorId();
        }

        Comment c = new Comment();
        c.setVideoId(req.getVideoId());
        c.setPostId(req.getPostId());
        c.setAuthorId(userId);
        c.setRootId(rootId);
        c.setReplyToId(req.getRootId());
        c.setReplyToUserId(replyToUserId);
        c.setContent(req.getContent());
        c.setStatus(1);
        c.setPinned(false);
        c.setLikeCount(0);
        c.setReplyCount(0);
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            try {
                c.setImageUrls(json.writeValueAsString(req.getImageUrls()));
            } catch (JsonProcessingException e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "图片地址格式不正确");
            }
        }
        commentMapper.insert(c);
        Long owner = req.getVideoId() == null ? requirePost(req.getPostId(), userId).getAuthorId()
                : videoMapper.selectById(req.getVideoId()).getOwnerId();
        notifications.publish(userId, replyToUserId == null ? owner : replyToUserId,
                replyToUserId == null ? 3 : 2, 2, c.getId(), c.getContent());
        if (req.getPostId() == null || requirePost(req.getPostId(), userId).getVisibility() == 0)
            mentions.publish(userId, 2, c.getId(), req.getMentionUserIds(), c.getContent());

        // 回复也计入视频总评论数；根节点的 replyCount 单独维护
        adjustCommentCount(c, 1);
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
        if (c == null || c.getDeletedAt() != null || !Integer.valueOf(1).equals(c.getStatus())) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (c.getPostId() != null) requirePost(c.getPostId(), userId);
        CommentLike exist = commentLikeMapper.selectOne(Wrappers.<CommentLike>lambdaQuery()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));

        int before = exist != null && !Boolean.TRUE.equals(exist.getIsDislike()) ? 1 : 0;
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
        if (after - before != 0) {
            bumpLikeCount(commentId, after - before);
            if (after == 1) notifications.publish(userId, c.getAuthorId(), 5, 2, commentId, c.getContent());
            else notifications.removeLike(userId, c.getAuthorId(), 5, 2, commentId);
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
        if (c == null || c.getDeletedAt() != null) {
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        }
        var video = c.getVideoId() == null ? null : videoMapper.selectById(c.getVideoId());
        boolean videoOwner = video != null && Objects.equals(video.getOwnerId(), userId);
        if (!admin && !c.getAuthorId().equals(userId) && !videoOwner) {
            throw new BizException(ErrorCode.NO_PERMISSION);
        }
        int removed = 1;
        if (c.getRootId() == null) {
            removed += commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                    .eq(Comment::getRootId, commentId).isNull(Comment::getDeletedAt)
                    .set(Comment::getDeletedAt, LocalDateTime.now()));
        }
        commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                .eq(Comment::getId, commentId).set(Comment::getDeletedAt, LocalDateTime.now()));
        adjustCommentCount(c, -removed);
        if (c.getRootId() != null) {
            commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                    .eq(Comment::getId, c.getRootId()).setSql("reply_count = GREATEST(reply_count - 1, 0)"));
        }
    }

    private UserPost requirePost(Long postId, Long viewerId) {
        UserPost post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null || !Integer.valueOf(1).equals(post.getStatus())
                || (Integer.valueOf(1).equals(post.getVisibility()) && !post.getAuthorId().equals(viewerId)))
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        return post;
    }

    private void adjustCommentCount(Comment comment, int delta) {
        if (comment.getPostId() == null) {
            if (delta < 0) counterMapper.decreaseVideoStat(comment.getVideoId(), "comment_count", -delta);
            else counterMapper.increaseVideoStat(comment.getVideoId(), "comment_count", delta);
        }
        else postMapper.update(null, Wrappers.<UserPost>lambdaUpdate().eq(UserPost::getId, comment.getPostId())
                .setSql("comment_count=GREATEST(comment_count+" + delta + ",0)"));
    }

    public List<DanmakuVO> danmaku(Long videoId, int limit) {
        return queryMapper.selectDanmaku(videoId, Math.min(Math.max(limit, 1), 5000));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> danmakuSnapshot(Long videoId, int limit) {
        var video = videoMapper.selectById(videoId);
        if (video == null || !Integer.valueOf(1).equals(video.getStatus()))
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        return Map.of("records", danmaku(videoId, limit), "count", danmakuCount(videoId));
    }

    @Transactional
    public boolean togglePin(Long userId, Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null || c.getDeletedAt() != null || !Integer.valueOf(1).equals(c.getStatus())
                || c.getVideoId() == null || c.getRootId() != null)
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        var video = videoMapper.selectById(c.getVideoId());
        if (video == null || !Objects.equals(video.getOwnerId(), userId))
            throw new BizException(ErrorCode.OPERATION_FAILED, "仅视频作者可置顶一级评论");
        boolean pinned = !Boolean.TRUE.equals(c.getPinned());
        if (pinned) commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                .eq(Comment::getVideoId, c.getVideoId()).eq(Comment::getPinned, true).set(Comment::getPinned, false));
        commentMapper.update(null, Wrappers.<Comment>lambdaUpdate().eq(Comment::getId, commentId)
                .set(Comment::getPinned, pinned));
        return pinned;
    }

    public CommentVO detail(Long commentId, Long viewerId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null || c.getDeletedAt() != null || !Integer.valueOf(1).equals(c.getStatus()))
            throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        if (c.getPostId() != null) requirePost(c.getPostId(), viewerId);
        else {
            var video = videoMapper.selectById(c.getVideoId());
            if (video == null || !Integer.valueOf(1).equals(video.getStatus()))
                throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        }
        Long rootId = c.getRootId() == null ? c.getId() : c.getRootId();
        CommentVO root = queryMapper.selectComment(rootId, viewerId);
        if (root == null) throw new BizException(ErrorCode.COMMENT_NOT_FOUND);
        List<CommentVO> replies = queryMapper.selectReplies(List.of(rootId), viewerId);
        replies.forEach(this::parseImages);
        root.setReplies(replies);
        parseImages(root);
        return root;
    }

    @Transactional
    public long deleteDanmaku(Long userId, Long danmakuId) {
        Danmaku d = danmakuMapper.selectById(danmakuId);
        if (d == null || !Objects.equals(d.getUserId(), userId))
            throw new BizException(ErrorCode.OPERATION_FAILED, "仅可撤回自己的弹幕");
        if (danmakuMapper.deleteById(danmakuId) > 0) {
            counterMapper.decreaseVideoStat(d.getVideoId(), "danmaku_count", 1);
            events.publishEvent(new DanmakuChanged(d.getVideoId(), null, danmakuId,
                    danmakuCount(d.getVideoId())));
        }
        return danmakuCount(d.getVideoId());
    }

    @Transactional
    public DanmakuVO postDanmaku(Long userId, DanmakuPostRequest req) {
        var sender = userMapper.selectById(userId);
        if (sender == null) throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
        var video = videoMapper.selectById(req.getVideoId());
        if (video == null || !Integer.valueOf(1).equals(video.getStatus())) {
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
        d.setColorful(Boolean.TRUE.equals(req.getColorful()));
        d.setFontSize(req.getFontSize() == null ? 25 : req.getFontSize());
        d.setMode(req.getMode() == null ? 0 : req.getMode());
        d.setVideoTimeMs(Math.max(0, req.getVideoTimeMs() == null ? 0 : req.getVideoTimeMs()));
        d.setCreatedAt(LocalDateTime.now());
        danmakuMapper.insert(d);

        counterMapper.increaseVideoStat(req.getVideoId(), "danmaku_count", 1);
        DanmakuVO vo = new DanmakuVO();
        vo.setId(d.getId());
        vo.setVideoId(d.getVideoId());
        vo.setContent(d.getContent());
        vo.setColor(d.getColor());
        vo.setColorful(d.getColorful());
        vo.setFontSize(d.getFontSize());
        vo.setMode(d.getMode());
        vo.setVideoTimeMs(d.getVideoTimeMs());
        vo.setUserId(userId);
        vo.setUserNickname(sender.getNickname());
        vo.setCreatedAt(d.getCreatedAt());
        vo.setTotalCount(danmakuCount(d.getVideoId()));
        events.publishEvent(new DanmakuChanged(d.getVideoId(), json.convertValue(vo, DanmakuVO.class), null, vo.getTotalCount()));
        vo.setMine(Boolean.TRUE);
        return vo;
    }

    private long danmakuCount(Long videoId) {
        return danmakuMapper.selectCount(Wrappers.<Danmaku>lambdaQuery().eq(Danmaku::getVideoId, videoId));
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
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.DB_ERROR, "评论图片数据格式错误");
        }
    }
}