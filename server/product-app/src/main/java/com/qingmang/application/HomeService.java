package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.user.UserPrivacy;
import com.qingmang.domain.user.mapper.UserPrivacyMapper;
import com.qingmang.infrastructure.mapper.HomeQueryMapper;
import com.qingmang.infrastructure.mapper.UserContentQueryMapper;
import com.qingmang.interfaces.dto.HomeQueryRequest;
import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.HomeImageItemVO;
import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {
    private final AuthService auth;
    private final UserPrivacyMapper privacyMapper;
    private final UserContentQueryMapper content;
    private final HomeQueryMapper query;
    private final ObjectMapper json;

    public HomeService(AuthService auth, UserPrivacyMapper privacyMapper, UserContentQueryMapper content,
                       HomeQueryMapper query, ObjectMapper json) {
        this.auth = auth;
        this.privacyMapper = privacyMapper;
        this.content = content;
        this.query = query;
        this.json = json;
    }

    public PageResult<VideoListItemVO> videos(HomeQueryRequest q, Long viewerId) {
        auth.requireActiveUser(q.getHomeUserId());
        return PageResult.of(query.videos(q, viewerId, offset(q)), query.videoCount(q), q.getPageNum(), 20);
    }

    public List<VideoListItemVO> reactions(Long ownerId, Long viewerId, int type) {
        auth.requireActiveUser(ownerId);
        if (!Objects.equals(ownerId, viewerId)) {
            UserPrivacy privacy = privacyMapper.selectOne(Wrappers.<UserPrivacy>lambdaQuery()
                    .eq(UserPrivacy::getUserId, ownerId));
            boolean visible = privacy != null && Boolean.TRUE.equals(type == 1
                    ? privacy.getShowLikeList() : privacy.getShowCoinList());
            if (!visible) throw new BizException(ErrorCode.NO_PERMISSION);
        }
        return query.reactions(ownerId, viewerId, type);
    }

    @Transactional
    public void updateFeatured(Long ownerId, List<Long> videoIds) {
        auth.requireActiveUser(ownerId);
        if (videoIds == null || videoIds.size() > 3 || videoIds.stream().anyMatch(id -> id == null || id <= 0) ||
                new HashSet<>(videoIds).size() != videoIds.size())
            throw new BizException(ErrorCode.PARAM_INVALID);
        query.lockOwner(ownerId);
        if (!videoIds.isEmpty() && query.countOwnedVideos(ownerId, videoIds) != videoIds.size())
            throw new BizException(ErrorCode.NO_PERMISSION, "只能选择自己的已发布视频");
        query.clearFeatured(ownerId);
        if (!videoIds.isEmpty()) query.setFeatured(ownerId, videoIds);
    }

    public List<FolderVO> folders(Long ownerId, Long viewerId) {
        requireListAccess(ownerId, viewerId, 5);
        return content.selectFolders(ownerId).stream()
                .filter(f -> Objects.equals(ownerId, viewerId) || Integer.valueOf(1).equals(f.getVisibility()))
                .toList();
    }

    public PageResult<VideoListItemVO> folderVideos(HomeQueryRequest q, Long viewerId) {
        List<FolderVO> folders = folders(q.getHomeUserId(), viewerId);
        if (q.getType() == 3) throw new BizException(ErrorCode.PARAM_INVALID);
        Long folderId = null;
        if (q.getType() == 1) {
            FolderVO selected = folders.stream().filter(f -> q.getFolderId() != null
                            ? Objects.equals(f.getId(), q.getFolderId())
                            : Objects.equals(f.getName(), q.getCollectName()))
                    .findFirst().orElseThrow(() -> new BizException(ErrorCode.FOLDER_NOT_FOUND));
            folderId = selected.getId();
        }
        return PageResult.of(query.folderVideos(q, viewerId, folderId, offset(q)),
                query.folderVideoCount(q, viewerId, folderId), q.getPageNum(), 20);
    }

    public PageResult<PostVO> posts(HomeQueryRequest q, Long viewerId) {
        auth.requireActiveUser(q.getHomeUserId());
        if (q.getType() == 3) throw new BizException(ErrorCode.PARAM_INVALID);
        List<PostVO> rows = query.posts(q, viewerId, offset(q));
        rows.forEach(row -> preparePost(row, viewerId));
        return PageResult.of(rows, query.postCount(q, viewerId), q.getPageNum(), 20);
    }

    public PageResult<HomeImageItemVO> images(HomeQueryRequest q, Long viewerId) {
        auth.requireActiveUser(q.getHomeUserId());
        List<HomeImageItemVO> rows = query.images(q, viewerId, offset(q));
        rows.forEach(row -> {
            preparePost(row, viewerId);
            if (Integer.valueOf(2).equals(row.getSourceType())) row.setIsSelf(false);
        });
        return PageResult.of(rows, query.imageCount(q, viewerId), q.getPageNum(), 20);
    }

    public PageResult<UserBriefVO> relations(Long ownerId, Long viewerId, int type, int pageNum, String keyword) {
        if (ownerId == null || ownerId <= 0 || (type != 8 && type != 9) || pageNum < 1 || pageNum > 10000 ||
                (keyword != null && keyword.length() > 50)) throw new BizException(ErrorCode.PARAM_INVALID);
        requireListAccess(ownerId, viewerId, type);
        boolean fans = type == 9;
        return PageResult.of(query.relations(ownerId, viewerId, fans, keyword, (pageNum - 1) * 24),
                query.relationCount(ownerId, fans, keyword), pageNum, 24);
    }

    private void requireListAccess(Long ownerId, Long viewerId, int type) {
        auth.requireActiveUser(ownerId);
        if (Objects.equals(ownerId, viewerId)) return;
        UserPrivacy privacy = privacyMapper.selectOne(Wrappers.<UserPrivacy>lambdaQuery()
                .eq(UserPrivacy::getUserId, ownerId));
        boolean visible = privacy != null && Boolean.TRUE.equals(switch (type) {
            case 5 -> privacy.getShowFavoriteList();
            case 8 -> privacy.getShowFollowList();
            case 9 -> privacy.getShowFansList();
            default -> false;
        });
        if (!visible) throw new BizException(ErrorCode.NO_PERMISSION);
    }

    private void preparePost(PostVO row, Long viewerId) {
        row.setIsSelf(Objects.equals(row.getAuthorId(), viewerId));
        String raw = row.getImageUrls();
        if (raw == null || raw.isBlank()) row.setImageList(List.of());
        else {
            try {
                row.setImageList(json.readValue(raw, new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "动态图片数据格式错误");
            }
        }
        row.setImageUrls(null);
    }

    private int offset(HomeQueryRequest q) {
        return (q.getPageNum() - 1) * 20;
    }
}
