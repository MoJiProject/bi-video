package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.social.PrivateMessage;
import com.qingmang.domain.social.UserFollow;
import com.qingmang.domain.social.UserPost;
import com.qingmang.domain.social.PostLike;
import com.qingmang.domain.social.mapper.PostLikeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qingmang.domain.social.mapper.PrivateMessageMapper;
import com.qingmang.domain.social.mapper.UserFollowMapper;
import com.qingmang.domain.social.mapper.UserPostMapper;
import com.qingmang.domain.user.mapper.UserStatsMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.SocialQueryMapper;
import com.qingmang.interfaces.dto.PostRequest;
import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 关注关系与动态。 */
@Service
public class SocialService {

    private final UserFollowMapper followMapper;
    private final UserPostMapper postMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final UserStatsMapper statsMapper;
    private final CounterMapper counterMapper;
    private final SocialQueryMapper queryMapper;
    private final ObjectMapper json;
    private final AuthService auth;
    private final PostLikeMapper postLikes;
    private final MentionNotificationService mentions;
    private final NotificationCenterService notifications;

    public SocialService(UserFollowMapper followMapper, UserPostMapper postMapper,
                         PrivateMessageMapper privateMessageMapper,
                         UserStatsMapper statsMapper, CounterMapper counterMapper,
                         SocialQueryMapper queryMapper, ObjectMapper json, AuthService auth, PostLikeMapper postLikes,
                         MentionNotificationService mentions, NotificationCenterService notifications) {
        this.followMapper = followMapper;
        this.postMapper = postMapper;
        this.privateMessageMapper = privateMessageMapper;
        this.statsMapper = statsMapper;
        this.counterMapper = counterMapper;
        this.queryMapper = queryMapper;
        this.json = json;
        this.auth = auth;
        this.postLikes = postLikes;
        this.mentions = mentions;
        this.notifications = notifications;
    }

    @Transactional
    public void setFollowing(Long userId, Long targetUserId, boolean following) {
        if (userId.equals(targetUserId)) throw new BizException(ErrorCode.FOLLOW_SELF);
        auth.requireActiveUser(targetUserId);
        if (following) {
            UserFollow relation = new UserFollow();
            relation.setFollowerId(userId);
            relation.setFolloweeId(targetUserId);
            try {
                followMapper.insert(relation);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                return;
            }
        } else {
            int removed = followMapper.delete(Wrappers.<UserFollow>lambdaQuery()
                    .eq(UserFollow::getFollowerId, userId).eq(UserFollow::getFolloweeId, targetUserId));
            if (removed == 0) return;
        }
        int delta = following ? 1 : -1;
        counterMapper.increaseUserStat(targetUserId, "follower_count", delta);
        counterMapper.increaseUserStat(userId, "following_count", delta);
    }

    /** 关注/取关。粉丝数与关注数一起改，靠唯一键防重复关注。 */
    @Transactional
    public boolean toggleFollow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BizException(ErrorCode.FOLLOW_SELF);
        }
        UserFollow exist = followMapper.selectOne(Wrappers.<UserFollow>lambdaQuery()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFolloweeId, targetUserId));
        boolean following;
        if (exist == null) {
            UserFollow f = new UserFollow();
            f.setFollowerId(userId);
            f.setFolloweeId(targetUserId);
            try {
                followMapper.insert(f);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                throw new BizException(ErrorCode.ALREADY_FOLLOWED);
            }
            counterMapper.increaseUserStat(targetUserId, "follower_count", 1);
            counterMapper.increaseUserStat(userId, "following_count", 1);
            following = true;
        } else {
            followMapper.deleteById(exist.getId());
            counterMapper.increaseUserStat(targetUserId, "follower_count", -1);
            counterMapper.increaseUserStat(userId, "following_count", -1);
            following = false;
        }
        return following;
    }

    /** targetUserId 为空看公开动态流，不为空看某个人的。 */
    public List<PostVO> posts(Long viewerId, Long targetUserId, Long cursorId, int size) {
        if (size < 1 || size > 100) throw new BizException(ErrorCode.PARAM_INVALID);
        List<PostVO> rows = queryMapper.selectPosts(viewerId, targetUserId, cursorId, Math.min(size + 1, 101));
        rows.forEach(r -> {
            r.setIsSelf(r.getAuthorId().equals(viewerId));
            r.setImageList(parseImages(r.getImageUrls()));
            r.setImageUrls(null);
        });
        return rows;
    }

    public PostVO detail(Long postId, Long viewerId) {
        PostVO row = queryMapper.selectPost(postId, viewerId);
        if (row == null) throw new BizException(ErrorCode.POST_NOT_FOUND);
        row.setIsSelf(row.getAuthorId().equals(viewerId));
        row.setImageList(parseImages(row.getImageUrls()));
        row.setImageUrls(null);
        return row;
    }

    public List<PostVO> feed(Long viewerId, Long targetUserId, Long cursorId, int size, boolean followingOnly, int type) {
        if (size < 1 || size > 100 || type < 1 || type > 3) throw new BizException(ErrorCode.PARAM_INVALID);
        if (followingOnly && viewerId == null) throw new BizException(ErrorCode.NOT_LOGIN);
        List<PostVO> rows = queryMapper.selectFeed(viewerId, targetUserId, cursorId, size + 1, followingOnly, type);
        rows.forEach(row -> {
            row.setIsSelf(row.getAuthorId().equals(viewerId));
            row.setImageList(parseImages(row.getImageUrls()));
            row.setImageUrls(null);
        });
        return rows;
    }

    @Transactional
    public boolean pin(Long userId, Long postId) {
        PostVO post = detail(postId, userId);
        if (!userId.equals(post.getAuthorId())) throw new BizException(ErrorCode.NO_PERMISSION);
        boolean pin = !Boolean.TRUE.equals(post.getIsPinned());
        postMapper.update(null, Wrappers.<UserPost>lambdaUpdate()
                .eq(UserPost::getAuthorId, userId).set(UserPost::getIsPinned, false));
        if (pin) postMapper.update(null, Wrappers.<UserPost>lambdaUpdate()
                .eq(UserPost::getId, postId).eq(UserPost::getAuthorId, userId).set(UserPost::getIsPinned, true));
        return pin;
    }

    @Transactional
    public boolean togglePostLike(Long userId, Long postId) {
        PostVO post = detail(postId, userId);
        PostLike existing = postLikes.selectOne(Wrappers.<PostLike>lambdaQuery()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId));
        boolean liked = existing == null;
        if (liked) {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            try { postLikes.insert(like); }
            catch (org.springframework.dao.DuplicateKeyException e) { throw new BizException(ErrorCode.REQUEST_TOO_FREQUENT); }
        } else if (postLikes.delete(Wrappers.<PostLike>lambdaQuery()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId)) == 0) {
            return false;
        }
        postMapper.update(null, Wrappers.<UserPost>lambdaUpdate().eq(UserPost::getId, postId)
                .setSql(liked ? "like_count = like_count + 1" : "like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END"));
        if (liked) notifications.publish(userId, post.getAuthorId(), 8, 3, postId, post.getContent());
        else notifications.removeLike(userId, post.getAuthorId(), 8, 3, postId);
        return liked;
    }

    @Transactional
    public Long post(Long userId, PostRequest req) {
        if (req.getVideoId() != null && !queryMapper.videoAvailable(req.getVideoId()))
            throw new BizException(ErrorCode.VIDEO_NOT_FOUND);
        boolean empty = (req.getContent() == null || req.getContent().isBlank())
                && (req.getImageUrls() == null || req.getImageUrls().isEmpty())
                && req.getVideoId() == null;
        if (empty) {
            throw new BizException(ErrorCode.PARAM_INVALID, "动态内容不能为空");
        }
        UserPost p = new UserPost();
        p.setAuthorId(userId);
        p.setVideoId(req.getVideoId());
        p.setContent(req.getContent());
        p.setTitle(req.getTitle() == null ? "" : req.getTitle());
        p.setVisibility(req.getVisibility() == null ? 0 : req.getVisibility());
        p.setStatus(1);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setShareCount(0);
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            try {
                p.setImageUrls(json.writeValueAsString(req.getImageUrls()));
            } catch (JsonProcessingException e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "图片地址格式不正确");
            }
        }
        postMapper.insert(p);
        counterMapper.increaseUserStat(userId, "post_count", 1);
        if (p.getVisibility() == 0) mentions.publish(userId, 3, p.getId(), req.getMentionUserIds(), p.getContent());
        return p.getId();
    }

    @Transactional
    public void deletePost(Long userId, Long postId, boolean admin) {
        UserPost p = postMapper.selectById(postId);
        if (p == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        if (!admin && !p.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.NO_PERMISSION);
        }
        postMapper.deleteById(postId);
        counterMapper.increaseUserStat(p.getAuthorId(), "post_count", -1);
    }

    /** otherId 为空看我关注的人，否则看关注我的人。 */
    public List<UserBriefVO> follows(Long viewerId, Long otherId, Long cursorId, int size) {
        return queryMapper.selectFollows(viewerId, otherId, cursorId, Math.min(size + 1, 101));
    }

    public List<UserBriefVO> mentions(Long viewerId, Long cursorId) {
        return queryMapper.selectMentions(viewerId, cursorId, 101);
    }

    public List<UserBriefVO> feedAuthors(Long viewerId, Long cursorId) {
        return queryMapper.selectFeedAuthors(viewerId, cursorId, 101);
    }

    public void markFeedRead(Long viewerId, Long targetUserId) {
        queryMapper.markFeedRead(viewerId, targetUserId);
    }

    public int unreadPrivateMessageCount(Long userId) {
        if (userId == null) return 0;
        Long count = privateMessageMapper.selectCount(Wrappers.<PrivateMessage>lambdaQuery()
                .eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, Boolean.FALSE)
                .isNull(PrivateMessage::getReceiverDeletedAt)
                .isNull(PrivateMessage::getRevokedAt)
                .isNull(PrivateMessage::getDeletedAt));
        return count == null ? 0 : count.intValue();
    }

    private List<String> parseImages(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return json.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "动态图片数据格式错误");
        }
    }
}