package com.qingmang.interfaces;

import com.qingmang.application.AuthService;
import com.qingmang.application.MediaStorageService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaStorageService media;
    private final AuthService auth;
    public MediaController(MediaStorageService media, AuthService auth) { this.media = media; this.auth = auth; }
    @PostMapping("/{category}")
    public ApiResponse<Map<String, String>> upload(@PathVariable String category, @RequestParam MultipartFile file) {
        Long userId = AuthContext.requireUserId();
        String url = media.store(file, category, userId);
        if ("avatar".equals(category) || "background".equals(category)) auth.updateMedia(userId, category, url);
        return ApiResponse.ok(Map.of("url", url));
    }
}
