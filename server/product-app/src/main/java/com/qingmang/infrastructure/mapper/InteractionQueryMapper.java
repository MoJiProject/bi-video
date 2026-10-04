package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.CommentVO;
import com.qingmang.interfaces.vo.DanmakuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 评论与弹幕的跨表查询。 */
@Mapper
public interface InteractionQueryMapper {

    /**
     * 一级评论列表。
     *
     * <p>回复不在这里查：拿到这一页的 rootId 之后用 {@link #selectReplies} 一次 IN 全部取回，
     * 避免「列表 N 条 + 每条再查一次回复」的 N+1。</p>
     */
    List<CommentVO> selectComments(@Param("videoId") Long videoId,
                                  @Param("cursorId") Long cursorId,
                                  @Param("limit") int limit,
                                  @Param("viewerId") Long viewerId);

    List<CommentVO> selectReplies(@Param("rootIds") List<Long> rootIds,
                                  @Param("viewerId") Long viewerId);

    long countComments(@Param("videoId") Long videoId);

    List<DanmakuVO> selectDanmaku(@Param("videoId") Long videoId, @Param("limit") int limit);
}