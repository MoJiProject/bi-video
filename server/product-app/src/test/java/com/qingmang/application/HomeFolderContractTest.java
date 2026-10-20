package com.qingmang.application;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.qingmang.domain.favorite.FavoriteFolder;
import com.qingmang.domain.favorite.FavoriteItem;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.favorite.mapper.FavoriteItemMapper;
import com.qingmang.domain.interaction.mapper.WatchHistoryMapper;
import com.qingmang.domain.interaction.mapper.WatchLaterMapper;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.infrastructure.mapper.CounterMapper;
import com.qingmang.infrastructure.mapper.UserContentQueryMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HomeFolderContractTest {
    @BeforeAll
    static void initializeTableMetadata() {
        MapperBuilderAssistant builder = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(builder, FavoriteItem.class);
        TableInfoHelper.initTableInfo(builder, FavoriteFolder.class);
    }

    @Test
    void deletingFolderMergesDuplicateVideosBeforeMovingRemainingItems() {
        FavoriteFolderMapper folders = mock(FavoriteFolderMapper.class);
        FavoriteItemMapper items = mock(FavoriteItemMapper.class);
        CounterMapper counters = mock(CounterMapper.class);
        UserContentQueryMapper query = mock(UserContentQueryMapper.class);
        FavoriteFolder source = new FavoriteFolder().setId(2L).setIsDefault(false);
        FavoriteFolder target = new FavoriteFolder().setId(1L).setIsDefault(true);
        when(folders.selectOne(any())).thenReturn(source, target);
        when(query.removeDuplicateFolderItems(2L, 1L)).thenReturn(1);
        UserContentService service = new UserContentService(folders, items, mock(WatchLaterMapper.class),
                mock(WatchHistoryMapper.class), mock(VideoMapper.class), counters, query);
        service.deleteFolder(7L, 2L);
        var order = inOrder(query, counters, items, folders);
        order.verify(query).decreaseDuplicateFolderStats(2L, 1L);
        order.verify(query).removeDuplicateFolderItems(2L, 1L);
        order.verify(counters).increaseUserStat(7L, "favorite_count", -1);
        order.verify(items).update(isNull(), any());
        order.verify(folders).deleteById(2L);
    }

    @Test
    void removingAbsentVideoDoesNotDecrementCounters() {
        FavoriteFolderMapper folders = mock(FavoriteFolderMapper.class);
        FavoriteItemMapper items = mock(FavoriteItemMapper.class);
        CounterMapper counters = mock(CounterMapper.class);
        when(folders.selectOne(any())).thenReturn(new FavoriteFolder().setId(2L));
        when(items.delete(any())).thenReturn(0);
        UserContentService service = new UserContentService(folders, items, mock(WatchLaterMapper.class),
                mock(WatchHistoryMapper.class), mock(VideoMapper.class), counters, mock(UserContentQueryMapper.class));
        service.removeFromFolder(7L, 2L, 3L);
        verifyNoInteractions(counters);
    }
}
