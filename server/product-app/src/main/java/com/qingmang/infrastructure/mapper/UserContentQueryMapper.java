package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserContentQueryMapper {

    List<FolderVO> selectFolders(@Param("ownerId") Long ownerId);

    /** 收藏夹里的视频，分页用 offset（收藏夹条目量可控）。 */
    List<VideoListItemVO> selectFolderVideos(@Param("folderId") Long folderId,
                                             @Param("viewerId") Long viewerId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    long countFolderVideos(@Param("folderId") Long folderId);

    /** 待看清单。owner 就是 viewer，所以互动状态用 userId 算。 */
    List<VideoListItemVO> selectWatchLater(@Param("userId") Long userId,
                                           @Param("cursorId") Long cursorId,
                                           @Param("viewerId") Long viewerId,
                                           @Param("limit") int limit);

    /** 观看记录。 */
    List<VideoListItemVO> selectHistory(@Param("userId") Long userId,
                                        @Param("cursorId") Long cursorId,
                                        @Param("viewerId") Long viewerId,
                                        @Param("limit") int limit);

    List<VideoListItemVO> selectUserVideos(@Param("userId") Long userId,
                                           @Param("viewerId") Long viewerId,
                                           @Param("cursorId") Long cursorId,
                                           @Param("limit") int limit);
}