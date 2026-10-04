package com.qingmang.interfaces;

import com.qingmang.application.SearchService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.interfaces.dto.SearchQuery;
import com.qingmang.interfaces.vo.KeywordVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService search;

    public SearchController(SearchService search) {
        this.search = search;
    }

    @GetMapping("/video")
    public ApiResponse<PageResult<VideoListItemVO>> searchVideos(@Valid SearchQuery query) {
        return search.search(query);
    }

    /** 搜索热榜，走 Redis 缓存。 */
    @GetMapping("/hot")
    public ApiResponse<List<KeywordVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(search.hot(limit));
    }
}