package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.KeywordVO;
import com.qingmang.interfaces.vo.UserSearchItemVO;
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

    /**
     * 用户搜索。搜索页的「用户」页签用。
     *
     * <p>昵称/账号走 LIKE 前缀匹配而不是全文索引：user 表的 nickname
     * 没有 FULLTEXT 索引，而且用户搜索的候选集本来就小。</p>
     */
    List<UserSearchItemVO> searchUsers(@Param("keyword") String keyword,
                                       @Param("viewerId") Long viewerId,
                                       @Param("sort") String sort,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    List<KeywordVO> hotKeywords(@Param("limit") int limit);
}