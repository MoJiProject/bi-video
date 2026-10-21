package com.qingmang.interfaces;

import com.qingmang.application.VideoShareService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VideoShareController {
    private final VideoShareService service;
    public VideoShareController(VideoShareService service) { this.service = service; }

    public record Request(@NotEmpty @Size(max = 10) List<@NotNull @Positive Long> userIdList,
                          @Size(max = 500) String content) { }

    @GetMapping("/share/recipients")
    public ApiResponse<Map<String, Object>> recipients(@RequestParam(required = false) Long cursorId) {
        return ApiResponse.ok(service.recipients(AuthContext.requireUserId(), cursorId));
    }

    @PostMapping("/video/{videoId}/share")
    public ApiResponse<Map<String, Object>> share(@PathVariable Long videoId, @Valid @RequestBody Request body) {
        return ApiResponse.ok(service.share(AuthContext.requireUserId(), videoId, body.userIdList(), body.content()));
    }
}
