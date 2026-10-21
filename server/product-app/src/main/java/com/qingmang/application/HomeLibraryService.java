package com.qingmang.application;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.favorite.FavoriteFolder;
import com.qingmang.domain.favorite.FavoriteItem;
import com.qingmang.domain.favorite.mapper.FavoriteItemMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.HomeLibraryMapper;
import com.qingmang.interfaces.dto.HomeLibraryRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeLibraryService {
    private final HomeLibraryMapper query;
    private final FavoriteItemMapper items;
    private final CounterMapper counters;

    public HomeLibraryService(HomeLibraryMapper query, FavoriteItemMapper items, CounterMapper counters) {
        this.query = query;
        this.items = items;
        this.counters = counters;
    }

    @Transactional
    public int batch(Long ownerId, Long folderId, HomeLibraryRequest request) {
        List<FavoriteFolder> folders = query.lockFolders(ownerId);
        requireOwned(folders, folderId);
        boolean deleting = "delete".equals(request.getOperation());
        if (!deleting) {
            requireOwned(folders, request.getTargetFolderId());
            if (Objects.equals(folderId, request.getTargetFolderId()))
                throw new BizException(ErrorCode.PARAM_INVALID, "请选择其他收藏夹");
        }
        List<FavoriteItem> source = items.selectList(Wrappers.<FavoriteItem>lambdaQuery()
                .eq(FavoriteItem::getFolderId, folderId).eq(FavoriteItem::getUserId, ownerId)
                .in(FavoriteItem::getVideoId, request.getVideoIds()));
        // Reject stale/foreign selections atomically rather than silently processing a subset.
        if (source.size() != new HashSet<>(request.getVideoIds()).size())
            throw new BizException(ErrorCode.PARAM_INVALID, "收藏内容已变化，请刷新后重试");
        int changed = 0;
        for (FavoriteItem item : source) {
            if (!deleting) {
                boolean exists = items.selectCount(Wrappers.<FavoriteItem>lambdaQuery()
                        .eq(FavoriteItem::getFolderId, request.getTargetFolderId())
                        .eq(FavoriteItem::getVideoId, item.getVideoId())) > 0;
                if (!exists) {
                    FavoriteItem copy = new FavoriteItem().setFolderId(request.getTargetFolderId())
                            .setUserId(ownerId).setVideoId(item.getVideoId());
                    items.insert(copy);
                    counters.increaseVideoStat(item.getVideoId(), "favorite_count", 1);
                    counters.increaseUserStat(ownerId, "favorite_count", 1);
                    changed++;
                }
            }
            if (deleting || "move".equals(request.getOperation())) {
                remove(ownerId, item);
                changed++;
            }
        }
        query.syncCount(folderId, ownerId);
        if (!deleting) query.syncCount(request.getTargetFolderId(), ownerId);
        return changed;
    }

    @Transactional
    public int clearInvalid(Long ownerId, Long folderId) {
        requireOwned(query.lockFolders(ownerId), folderId);
        List<FavoriteItem> invalid = query.invalidItems(folderId, ownerId);
        invalid.forEach(item -> remove(ownerId, item));
        query.syncCount(folderId, ownerId);
        return invalid.size();
    }

    private void remove(Long ownerId, FavoriteItem item) {
        if (items.delete(Wrappers.<FavoriteItem>lambdaQuery().eq(FavoriteItem::getId, item.getId())
                .eq(FavoriteItem::getUserId, ownerId)) > 0) {
            counters.decreaseVideoStat(item.getVideoId(), "favorite_count", 1);
            counters.increaseUserStat(ownerId, "favorite_count", -1);
        }
    }

    private void requireOwned(List<FavoriteFolder> folders, Long folderId) {
        if (folderId == null || folders.stream().noneMatch(folder -> Objects.equals(folder.getId(), folderId)))
            throw new BizException(ErrorCode.FOLDER_NOT_FOUND);
    }
}
