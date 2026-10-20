package com.moji.controller;

import com.moji.FilePathEnum;
import com.moji.R;
import com.moji.po.Banner;
import com.moji.service.BannerService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/banner")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public R<List<Banner>> list() {
        return R.success(bannerService.listVisible());
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> image(@PathVariable String fileName) {
        if (!fileName.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.webp"))
            return ResponseEntity.notFound().build();
        Resource image = new FileSystemResource(Path.of(FilePathEnum.UPLOAD_BANNER.getPath(), fileName));
        if (!image.isReadable())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS)).body(image);
    }
}
