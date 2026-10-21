package com.qingmang.interfaces;

import com.qingmang.application.VideoQueryService;
import com.qingmang.application.VideoInteractionService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoQueryService videoQueryService;
    private final VideoInteractionService interactionService;

    public VideoController(VideoQueryService videoQueryService, VideoInteractionService interactionService) {
        this.videoQueryService = videoQueryService;
        this.interactionService = interactionService;
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
        return ApiResponse.ok(vo);
    }

    @PostMapping("/{videoId}/play")
    public ApiResponse<Void> play(@PathVariable Long videoId) {
        videoQueryService.detail(videoId, AuthContext.currentUserIdOrNull());
        videoQueryService.increasePlayCount(videoId);
        return ApiResponse.ok();
    }

    @GetMapping("/{videoId}/folders")
    public ApiResponse<List<Map<String, Object>>> folders(@PathVariable Long videoId) {
        return ApiResponse.ok(interactionService.videoFolders(AuthContext.requireUserId(), videoId));
    }

    @GetMapping("/getVideoTitle/{videoId}")
    public ApiResponse<String> title(@PathVariable Long videoId) {
        return ApiResponse.ok(videoQueryService.detail(videoId, AuthContext.currentUserIdOrNull()).getTitle());
    }
}