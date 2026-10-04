package com.qingmang.interfaces;

import com.qingmang.application.VideoQueryService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.interfaces.dto.VideoListQuery;
import com.qingmang.interfaces.vo.CategoryVO;
import com.qingmang.interfaces.vo.VideoDetailVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoQueryService videoQueryService;

    public VideoController(VideoQueryService videoQueryService) {
        this.videoQueryService = videoQueryService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.ok(videoQueryService.categories());
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<VideoListItemVO>> list(@Valid VideoListQuery query) {
        return videoQueryService.list(query, AuthContext.currentUserIdOrNull());
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailVO> detail(@PathVariable Long videoId) {
        Long userId = AuthContext.currentUserIdOrNull();
        VideoDetailVO vo = videoQueryService.detail(videoId, userId);
        if (userId != null) {
            videoQueryService.increasePlayCount(videoId);
        }
        return ApiResponse.ok(vo);
    }
}