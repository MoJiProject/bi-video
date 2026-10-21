package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.favorite.FavoriteFolder;
import com.qingmang.domain.favorite.FavoriteItem;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.favorite.mapper.FavoriteItemMapper;
import com.qingmang.domain.interaction.WatchHistory;
import com.qingmang.domain.interaction.WatchLater;
import com.qingmang.domain.interaction.mapper.WatchHistoryMapper;
import com.qingmang.domain.interaction.mapper.WatchLaterMapper;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.UserContentQueryMapper;
import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.qingmang.interfaces.dto.FolderRequest;
import com.qingmang.interfaces.dto.UserLibraryQuery;
import com.qingmang.interfaces.vo.UserLibraryVideoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** 用户自己的内容：收藏夹、待看清单、观看记录、投稿。 */
@Service
public class UserContentService {

    private final FavoriteFolderMapper folderMapper;
    private final FavoriteItemMapper itemMapper;
    private final WatchLaterMapper watchLaterMapper;
    private final WatchHistoryMapper historyMapper;
    private final VideoMapper videoMapper;
    private final CounterMapper counterMapper;
    private final UserContentQueryMapper queryMapper;

    public UserContentService(FavoriteFolderMapper folderMapper, FavoriteItemMapper itemMapper,
                              WatchLaterMapper watchLaterMapper, WatchHistoryMapper historyMapper,
                              VideoMapper videoMapper, CounterMapper counterMapper,
                              UserContentQueryMapper queryMapper) {
        this.folderMapper = folderMapper;
        this.itemMapper = itemMapper;
        this.watchLaterMapper = watchLaterMapper;
        this.historyMapper = historyMapper;
        this.videoMapper = videoMapper;
        this.counterMapper = counterMapper;
        this.queryMapper = queryMapper;
    }

    public List<FolderVO> folders(Long userId) {
        return queryMapper.selectFolders(userId);
    }

    public PageResult<VideoListItemVO> folderVideos(Long userId, Long folderId, int pageNum, int pageSize) {
        FolderVO folder = queryMapper.selectFolders(userId).stream()
                .filter(f -> folderId == null || folderId.equals(f.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.FOLDER_NOT_FOUND));
        long total = queryMapper.countFolderVideos(folder.getId());
        int offset = (Math.max(1, pageNum) - 1) * pageSize;
        List<VideoListItemVO> rows = queryMapper.selectFolderVideos(folder.getId(), userId, offset, pageSize);
        return PageResult.of(rows, total, pageNum, pageSize);
    }

    @Transactional
    public FolderVO createFolder(Long userId, String name, String description, int visibility, String coverUrl) {
        long exists = folderMapper.selectCount(Wrappers.<FavoriteFolder>lambdaQuery()
                .eq(FavoriteFolder::getOwnerId, userId)
                .eq(FavoriteFolder::getName, name));
        if (exists > 0) {
            throw new BizException(ErrorCode.FOLDER_NAME_DUPLICATED);
        }
        FavoriteFolder f = new FavoriteFolder();
        f.setOwnerId(userId);
        f.setName(name);
        f.setDescription(description);
        f.setVisibility(visibility);
        f.setCoverUrl(coverUrl);
        f.setIsDefault(Boolean.FALSE);
        f.setItemCount(0);
        folderMapper.insert(f);
        return toVO(f);
    }

    @Transactional
    public void updateFolder(Long userId, Long folderId, FolderRequest request) {
        requireOwnedFolder(userId, folderId);
        long duplicates = folderMapper.selectCount(Wrappers.<FavoriteFolder>lambdaQuery()
                .eq(FavoriteFolder::getOwnerId, userId).eq(FavoriteFolder::getName, request.getName())
                .ne(FavoriteFolder::getId, folderId));
        if (duplicates > 0) throw new BizException(ErrorCode.FOLDER_NAME_DUPLICATED);
        folderMapper.update(null, Wrappers.<FavoriteFolder>lambdaUpdate()
                .eq(FavoriteFolder::getId, folderId)
                .eq(FavoriteFolder::getOwnerId, userId)
                .set(FavoriteFolder::getName, request.getName())
                .set(FavoriteFolder::getDescription, request.getDescription())
                .set(request.getVisibility() != null, FavoriteFolder::getVisibility, request.getVisibility())
                .set(request.getCoverUrl() != null, FavoriteFolder::getCoverUrl, request.getCoverUrl()));
    }

    /**
     * 删收藏夹。
     *
     * <p>默认收藏夹不能直接删，必须先把里面的内容转到别的夹子，
     * 否则用户的收藏会跟着一起消失。</p>
     */
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        FavoriteFolder f = requireOwnedFolder(userId, folderId);
        if (Boolean.TRUE.equals(f.getIsDefault())) {
            throw new BizException(ErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
        }
        // 先把内容搬到默认夹，再删空夹
        FavoriteFolder def = defaultFolder(userId);
        queryMapper.decreaseDuplicateFolderStats(folderId, def.getId());
        int duplicates = queryMapper.removeDuplicateFolderItems(folderId, def.getId());
        if (duplicates > 0) counterMapper.increaseUserStat(userId, "favorite_count", -duplicates);
        itemMapper.update(null, Wrappers.<FavoriteItem>lambdaUpdate()
                .eq(FavoriteItem::getFolderId, folderId)
                .set(FavoriteItem::getFolderId, def.getId()));
        folderMapper.deleteById(folderId);
        syncFolderCount(def.getId());
    }

    @Transactional
    public void removeFromFolder(Long userId, Long folderId, Long videoId) {
        FavoriteFolder f = requireOwnedFolder(userId, folderId);
        int removed = itemMapper.delete(Wrappers.<FavoriteItem>lambdaQuery()
                .eq(FavoriteItem::getFolderId, folderId)
                .eq(FavoriteItem::getVideoId, videoId));
        if (removed == 0) return;
        counterMapper.decreaseVideoStat(videoId, "favorite_count", 1);
        counterMapper.increaseUserStat(userId, "favorite_count", -1);
        syncFolderCount(f.getId());
    }

    /** 待看清单：游标分页。 */
    public List<UserLibraryVideoVO> watchLater(Long userId, UserLibraryQuery query) {
        validateLibraryQuery(query);
        return queryMapper.selectWatchLater(userId, query, query.getSize() + 1);
    }

    public long countWatchLater(Long userId, UserLibraryQuery query) {
        validateLibraryQuery(query);
        return queryMapper.countWatchLater(userId, query);
    }

    @Transactional
    public void removeWatchLaterBatch(Long userId, List<Long> videoIds) {
        watchLaterMapper.delete(Wrappers.<WatchLater>lambdaQuery()
                .eq(WatchLater::getUserId, userId).in(WatchLater::getVideoId, videoIds));
    }

    @Transactional
    public void removeWatchLater(Long userId, Long videoId) {
        watchLaterMapper.delete(Wrappers.<WatchLater>lambdaQuery()
                .eq(WatchLater::getUserId, userId).eq(WatchLater::getVideoId, videoId));
    }

    @Transactional
    public void clearWatchLater(Long userId) {
        // 待看清单不参与视频的 favorite_count，直接清空即可
        watchLaterMapper.delete(Wrappers.<WatchLater>lambdaQuery().eq(WatchLater::getUserId, userId));
    }

    /** 观看记录：按最后观看时间倒序，游标翻页。 */
    public List<UserLibraryVideoVO> history(Long userId, UserLibraryQuery query) {
        validateLibraryQuery(query);
        return queryMapper.selectHistory(userId, userId, query, query.getSize() + 1);
    }

    private void validateLibraryQuery(UserLibraryQuery query) {
        if (query.getSize() < 1 || query.getSize() > 100
                || (query.getKeyword() != null && query.getKeyword().length() > 100)
                || (!"newest".equals(query.getOrder()) && !"oldest".equals(query.getOrder()))
                || (query.getStartTime() != null && query.getEndTime() != null
                    && !query.getStartTime().isBefore(query.getEndTime())))
            throw new BizException(ErrorCode.PARAM_INVALID);
        if (query.getKeyword() != null) query.setKeyword(query.getKeyword().trim());
    }

    @Transactional
    public void deleteHistoryBatch(Long userId, List<Long> videoIds) {
        historyMapper.delete(Wrappers.<WatchHistory>lambdaQuery()
                .eq(WatchHistory::getUserId, userId).in(WatchHistory::getVideoId, videoIds));
    }

    @Transactional
    public void deleteHistory(Long userId, Long videoId) {
        historyMapper.delete(Wrappers.<WatchHistory>lambdaQuery()
                .eq(WatchHistory::getUserId, userId)
                .eq(WatchHistory::getVideoId, videoId));
    }

    @Transactional
    public void clearHistory(Long userId) {
        historyMapper.delete(Wrappers.<WatchHistory>lambdaQuery().eq(WatchHistory::getUserId, userId));
    }

    /** 某个创作者（或自己）的投稿列表。 */
    public List<VideoListItemVO> userVideos(Long userId, Long viewerId, Long cursorId, int size) {
        return queryMapper.selectUserVideos(userId, viewerId, cursorId, Math.min(size + 1, 101));
    }

    private FavoriteFolder requireOwnedFolder(Long userId, Long folderId) {
        FavoriteFolder f = folderMapper.selectOne(Wrappers.<FavoriteFolder>lambdaQuery()
                .eq(FavoriteFolder::getId, folderId)
                .eq(FavoriteFolder::getOwnerId, userId));
        if (f == null) {
            throw new BizException(ErrorCode.FOLDER_NOT_FOUND);
        }
        return f;
    }

    private FavoriteFolder defaultFolder(Long userId) {
        FavoriteFolder f = folderMapper.selectOne(Wrappers.<FavoriteFolder>lambdaQuery()
                .eq(FavoriteFolder::getOwnerId, userId)
                .eq(FavoriteFolder::getIsDefault, Boolean.TRUE)
                .last("LIMIT 1"));
        if (f == null) {
            throw new BizException(ErrorCode.FOLDER_NOT_FOUND, "默认收藏夹不存在");
        }
        return f;
    }

    private void syncFolderCount(Long folderId) {
        Long n = itemMapper.selectCount(Wrappers.<FavoriteItem>lambdaQuery().eq(FavoriteItem::getFolderId, folderId));
        folderMapper.update(null, Wrappers.<FavoriteFolder>lambdaUpdate()
                .eq(FavoriteFolder::getId, folderId)
                .set(FavoriteFolder::getItemCount, n == null ? 0 : n));
    }

    private FolderVO toVO(FavoriteFolder f) {
        FolderVO vo = new FolderVO();
        vo.setId(f.getId());
        vo.setOwnerId(f.getOwnerId());
        vo.setName(f.getName());
        vo.setDescription(f.getDescription());
        vo.setCoverUrl(f.getCoverUrl());
        vo.setVisibility(f.getVisibility());
        vo.setIsDefault(f.getIsDefault());
        vo.setSortOrder(f.getSortOrder());
        vo.setItemCount(f.getItemCount());
        return vo;
    }
}