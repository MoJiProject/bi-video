package com.qingmang.interfaces;

import com.qingmang.application.NotificationCenterService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NotificationCenterController {
    private final NotificationCenterService service;
    public NotificationCenterController(NotificationCenterService service) { this.service = service; }

    public record Settings(@NotNull Integer messageWarn, @NotNull Integer replyMessageWarn,
                           @NotNull Integer atMessageWarn, @NotNull Integer likeMessageWarn) {}

    @PutMapping("/user/putMessageSettings")
    public ApiResponse<Void> settings(@Valid @RequestBody Settings request) {
        service.settings(AuthContext.requireUserId(), request.messageWarn(), request.replyMessageWarn(),
                request.atMessageWarn(), request.likeMessageWarn());
        return ApiResponse.ok();
    }

    @GetMapping("/comment/selectReplayComment")
    public ApiResponse<List<Map<String, Object>>> replies(@RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(service.replies(AuthContext.requireUserId(), pageNum));
    }

    @GetMapping("/comment/selectAt")
    public ApiResponse<List<Map<String, Object>>> mentions(@RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(service.mentions(AuthContext.requireUserId(), pageNum));
    }

    @GetMapping("/comment/selectLike")
    public ApiResponse<List<Map<String, Object>>> likes(@RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(service.likes(AuthContext.requireUserId(), pageNum));
    }

    @GetMapping("/comment/selectLikeUser")
    public ApiResponse<List<Map<String, Object>>> likeUsers(@RequestParam Long id, @RequestParam int type,
                                                           @RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(service.likeUsers(AuthContext.requireUserId(), id, type, pageNum));
    }

    @PutMapping("/privateMessage/putMessageNumber")
    public ApiResponse<Void> read(@RequestParam int messageMenu) {
        service.read(AuthContext.requireUserId(), messageMenu);
        return ApiResponse.ok();
    }

    @DeleteMapping("/comment/deleteCommentNotification")
    public ApiResponse<Void> delete(@RequestParam Long commentId, @RequestParam int messageMenu,
                                    @RequestParam(defaultValue = "0") int type) {
        service.delete(AuthContext.requireUserId(), commentId, messageMenu, type);
        return ApiResponse.ok();
    }

    @PutMapping("/comment/dndLikeNotification")
    public ApiResponse<Map<String, Boolean>> mute(@RequestParam Long id, @RequestParam int type) {
        return ApiResponse.ok(Map.of("muted", service.toggleMute(AuthContext.requireUserId(), id, type)));
    }
}
