package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.social.UserFollow;
import com.qingmang.domain.social.UserPost;
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
    private final UserStatsMapper statsMapper;
    private final CounterMapper counterMapper;
    private final SocialQueryMapper queryMapper;
    private final ObjectMapper json;

    public SocialService(UserFollowMapper followMapper, UserPostMapper postMapper,
                         UserStatsMapper statsMapper, CounterMapper counterMapper,
                         SocialQueryMapper queryMapper, ObjectMapper json) {
        this.followMapper = followMapper;
        this.postMapper = postMapper;
        this.statsMapper = statsMapper;
        this.counterMapper = counterMapper;
        this.queryMapper = queryMapper;
        this.json = json;
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
        List<PostVO> rows = queryMapper.selectPosts(viewerId, targetUserId, cursorId, Math.min(size + 1, 101));
        rows.forEach(r -> {
            r.setIsSelf(r.getAuthorId().equals(viewerId));
            r.setImageList(parseImages(r.getImageUrls()));
            r.setImageUrls(null);
        });
        return rows;
    }

    @Transactional
    public Long post(Long userId, PostRequest req) {
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
        p.setVisibility(req.getVisibility() == null ? 0 : req.getVisibility());
        p.setStatus(1);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setShareCount(0);
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            try {
                p.setImageUrls(json.writeValueAsString(req.getImageUrls()));
            } catch (Exception e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "图片地址格式不正确");
            }
        }
        postMapper.insert(p);
        counterMapper.increaseUserStat(userId, "post_count", 1);
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

    private List<String> parseImages(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return json.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}