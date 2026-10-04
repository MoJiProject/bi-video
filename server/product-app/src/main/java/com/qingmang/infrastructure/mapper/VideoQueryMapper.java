package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.CategoryVO;
import com.qingmang.interfaces.vo.UserReactionVO;
import com.qingmang.interfaces.vo.VideoDetailVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跨表查询专用 Mapper。
 *
 * <p>视频列表/详情需要同时拿视频、作者、计数、当前用户互动状态，
 * 这些查询写成 N+1 的话 20 条列表就是 60+ 次数据库往返。这里统一收在 XML 里。</p>
 */
@Mapper
public interface VideoQueryMapper {

    List<CategoryVO> selectEnabledCategories();

    /**
     * @param categoryId 分区，null 表示不限
     * @param ownerId    创作者，null 表示不限
     * @param cursorId   游标，只返回 id 小于它的（首页/时间线用，避免深分页）
     * @param sort       hot=最热 / latest=最新
     */
    List<VideoListItemVO> selectVideoList(@Param("categoryId") Long categoryId,
                                          @Param("ownerId") Long ownerId,
                                          @Param("cursorId") Long cursorId,
                                          @Param("sort") String sort,
                                          @Param("limit") int limit);

    List<UserReactionVO> selectUserReactions(@Param("userId") Long userId,
                                            @Param("videoIds") List<Long> videoIds);

    VideoDetailVO selectVideoDetail(@Param("videoId") Long videoId,
                                    @Param("userId") Long userId);
}