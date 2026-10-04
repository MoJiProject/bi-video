package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.KeywordVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchQueryMapper {

    /**
     * 视频搜索。
     *
     * <p>先用 FULLTEXT ... WITH PARSER ngram 出候选集再排「热度」，
     * 而不是 LIKE '%词%' 全表扫。</p>
     */
    List<VideoListItemVO> searchVideos(@Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("cursorId") Long cursorId,
                                       @Param("sort") String sort,
                                       @Param("limit") int limit);

    List<KeywordVO> hotKeywords(@Param("limit") int limit);
}