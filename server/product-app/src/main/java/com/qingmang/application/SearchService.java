package com.qingmang.application;

import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.constant.BizConstants;
import com.qingmang.infrastructure.mapper.SearchQueryMapper;
import com.qingmang.interfaces.dto.SearchQuery;
import com.qingmang.interfaces.vo.KeywordVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索。
 *
 * <p>明细查 MySQL 全文索引，热榜走 Redis 缓存；
 * 关键词计数交给 {@link SearchRecorder}，因为缓存失效注解必须在代理上生效。</p>
 */
@Service
public class SearchService {

    private final SearchQueryMapper queryMapper;
    private final SearchRecorder recorder;

    public SearchService(SearchQueryMapper queryMapper, SearchRecorder recorder) {
        this.queryMapper = queryMapper;
        this.recorder = recorder;
    }

    public ApiResponse<PageResult<VideoListItemVO>> search(SearchQuery q) {
        String keyword = q.getKeyword() == null ? "" : q.getKeyword().trim();
        if (keyword.isEmpty()) {
            // 空关键词就是浏览全部，不进全文检索
            return ApiResponse.ok(PageResult.empty(q.getPageNum(), q.getPageSize()));
        }
        recorder.record(keyword);

        int size = Math.min(Math.max(q.getPageSize() == null ? BizConstants.DEFAULT_PAGE_SIZE : q.getPageSize(), 1),
                BizConstants.MAX_PAGE_SIZE);
        List<VideoListItemVO> rows = queryMapper.searchVideos(
                keyword, q.getCategoryId(), q.getCursorId(), q.getSort(), size + 1);
        boolean hasMore = rows.size() > size;
        if (hasMore) {
            rows = rows.subList(0, size);
        }
        return ApiResponse.ok(PageResult.of(rows, hasMore ? -1 : rows.size(), q.getPageNum(), size));
    }

    @Cacheable(cacheNames = "hotKeyword", key = "'top'")
    public List<KeywordVO> hot(int limit) {
        return queryMapper.hotKeywords(Math.min(Math.max(limit, 1), 50));
    }
}