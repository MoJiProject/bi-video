package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.dto.HomeQueryRequest;
import com.qingmang.interfaces.vo.HomeImageItemVO;
import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HomeQueryMapper {
    List<VideoListItemVO> videos(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId,
                                @Param("offset") int offset);
    long videoCount(@Param("q") HomeQueryRequest query);
    List<VideoListItemVO> reactions(@Param("ownerId") Long ownerId, @Param("viewerId") Long viewerId,
                                   @Param("reactionType") int reactionType);
    List<VideoListItemVO> folderVideos(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId,
                                      @Param("folderId") Long folderId, @Param("offset") int offset);
    long folderVideoCount(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId,
                          @Param("folderId") Long folderId);
    List<PostVO> posts(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId,
                       @Param("offset") int offset);
    long postCount(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId);
    List<HomeImageItemVO> images(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId,
                                @Param("offset") int offset);
    long imageCount(@Param("q") HomeQueryRequest query, @Param("viewerId") Long viewerId);
    List<UserBriefVO> relations(@Param("ownerId") Long ownerId, @Param("viewerId") Long viewerId,
                                @Param("fans") boolean fans, @Param("keyword") String keyword,
                                @Param("offset") int offset);
    long relationCount(@Param("ownerId") Long ownerId, @Param("fans") boolean fans,
                       @Param("keyword") String keyword);
}
