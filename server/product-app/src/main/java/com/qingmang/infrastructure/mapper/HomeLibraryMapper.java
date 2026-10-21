package com.qingmang.infrastructure.mapper;

import com.qingmang.domain.favorite.FavoriteFolder;
import com.qingmang.domain.favorite.FavoriteItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HomeLibraryMapper {
    @Select("SELECT * FROM favorite_folder WHERE owner_id = #{ownerId} AND deleted_at IS NULL ORDER BY id FOR UPDATE")
    List<FavoriteFolder> lockFolders(@Param("ownerId") Long ownerId);

    @Select("SELECT fi.* FROM favorite_item fi LEFT JOIN video v ON v.id = fi.video_id " +
            "LEFT JOIN `user` u ON u.id = v.owner_id " +
            "WHERE fi.folder_id = #{folderId} AND fi.user_id = #{ownerId} " +
            "AND (v.id IS NULL OR v.deleted_at IS NOT NULL OR v.status != 1 " +
            "OR u.id IS NULL OR u.deleted_at IS NOT NULL)")
    List<FavoriteItem> invalidItems(@Param("folderId") Long folderId, @Param("ownerId") Long ownerId);

    @Update("UPDATE favorite_folder SET item_count = (SELECT COUNT(*) FROM favorite_item WHERE folder_id = #{folderId}) " +
            "WHERE id = #{folderId} AND owner_id = #{ownerId} AND deleted_at IS NULL")
    int syncCount(@Param("folderId") Long folderId, @Param("ownerId") Long ownerId);
}
