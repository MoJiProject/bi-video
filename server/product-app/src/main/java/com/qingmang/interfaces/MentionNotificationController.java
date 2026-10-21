package com.qingmang.interfaces;

import com.qingmang.application.MentionNotificationService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class MentionNotificationController {
    private final MentionNotificationService service;
    public MentionNotificationController(MentionNotificationService service) { this.service = service; }

    @GetMapping("/mentions")
    public ApiResponse<Map<String, Object>> list(@RequestParam(required = false) Long cursorId,
                                                @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(AuthContext.requireUserId(), cursorId, size));
    }

    @GetMapping("/unread")
    public ApiResponse<Map<String, Object>> unread() {
        return ApiResponse.ok(service.unread(AuthContext.requireUserId()));
    }

    @PostMapping("/mentions/read")
    public ApiResponse<Void> read(@RequestBody List<Long> ids) {
        service.read(AuthContext.requireUserId(), ids);
        return ApiResponse.ok();
    }

    @DeleteMapping("/mentions/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(AuthContext.requireUserId(), id);
        return ApiResponse.ok();
    }
}
