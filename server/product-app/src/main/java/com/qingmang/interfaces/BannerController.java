package com.qingmang.interfaces;

import com.qingmang.application.BannerService;
import com.qingmang.application.MediaStorageService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.domain.ops.Banner;
import com.qingmang.interfaces.dto.BannerRequest;
import com.qingmang.support.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BannerController {
    private final BannerService banners;
    private final MediaStorageService media;

    public BannerController(BannerService banners, MediaStorageService media) {
        this.banners = banners;
        this.media = media;
    }

    @GetMapping("/banner")
    public ApiResponse<List<Banner>> visible() {
        return ApiResponse.ok(banners.visible());
    }

    @GetMapping("/system/banner")
    public ApiResponse<PageResult<Banner>> search(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.ok(banners.search(AuthContext.requireUserId(), pageNum, keyword));
    }

    @PostMapping("/system/banner")
    public ApiResponse<Banner> create(@Valid @RequestBody BannerRequest request, HttpServletRequest http) {
        return ApiResponse.ok(banners.save(AuthContext.requireUserId(), null, request, http.getRemoteAddr()));
    }

    @PutMapping("/system/banner/{id}")
    public ApiResponse<Banner> update(@PathVariable Long id, @Valid @RequestBody BannerRequest request,
                                       HttpServletRequest http) {
        return ApiResponse.ok(banners.save(AuthContext.requireUserId(), id, request, http.getRemoteAddr()));
    }

    @DeleteMapping("/system/banner/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        banners.delete(AuthContext.requireUserId(), id, http.getRemoteAddr());
        return ApiResponse.ok();
    }

    @PostMapping("/system/banner/image")
    public ApiResponse<Map<String, String>> upload(@RequestParam MultipartFile file) {
        Long userId = AuthContext.requireUserId();
        banners.requireAdmin(userId);
        return ApiResponse.ok(Map.of("url", media.store(file, "image", userId)));
    }
}
