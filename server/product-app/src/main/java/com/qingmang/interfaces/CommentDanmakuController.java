package com.qingmang.interfaces;

import com.qingmang.application.CommentDanmakuService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.CommentPostRequest;
import com.qingmang.interfaces.dto.DanmakuPostRequest;
import com.qingmang.interfaces.vo.CommentVO;
import com.qingmang.interfaces.vo.DanmakuVO;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentDanmakuController {

    private final CommentDanmakuService service;

    public CommentDanmakuController(CommentDanmakuService service) {
        this.service = service;
    }

    @GetMapping("/comment/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam Long videoId,
                                                 @RequestParam(required = false) Long cursorId,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(videoId, cursorId, size, AuthContext.currentUserIdOrNull()));
    }

    @PostMapping("/comment")
    public ApiResponse<Map<String, Object>> post(@Valid @RequestBody CommentPostRequest req) {
        Long id = service.post(AuthContext.requireUserId(), req);
        return ApiResponse.ok(Map.of("commentId", id));
    }

    @PostMapping("/comment/{commentId}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long commentId,
                                                 @RequestParam(defaultValue = "false") boolean dislike) {
        return ApiResponse.ok(service.toggleLike(AuthContext.requireUserId(), commentId, dislike));
    }

    @DeleteMapping("/comment/{commentId}")
    public ApiResponse<Void> delete(@PathVariable Long commentId) {
        service.delete(AuthContext.requireUserId(), commentId, false);
        return ApiResponse.ok();
    }

    @GetMapping("/danmaku/list")
    public ApiResponse<List<DanmakuVO>> danmaku(@RequestParam Long videoId,
                                                 @RequestParam(defaultValue = "2000") int limit) {
        return ApiResponse.ok(service.danmaku(videoId, limit));
    }

    @PostMapping("/danmaku")
    public ApiResponse<DanmakuVO> postDanmaku(@Valid @RequestBody DanmakuPostRequest req) {
        return ApiResponse.ok(service.postDanmaku(AuthContext.requireUserId(), req));
    }
}