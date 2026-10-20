package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.BannerSaveDto;
import com.moji.dto.SystemOperateDto;
import com.moji.exception.BaseException;
import com.moji.po.Banner;
import com.moji.service.BannerService;
import com.moji.service.SystemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/banner")
public class SystemBannerController {
    private final BannerService bannerService;
    private final SystemService systemService;

    public SystemBannerController(BannerService bannerService, SystemService systemService) {
        this.bannerService = bannerService;
        this.systemService = systemService;
    }

    @GetMapping
    public R<Page<Banner>> search(@RequestParam Integer operatorId,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "") String keyword,
                                  @RequestHeader(value = "Authorization", required = false) String token) {
        checkAdmin(operatorId, token);
        return R.success(bannerService.search(pageNum, keyword));
    }

    @PostMapping
    public R<Banner> add(@RequestBody BannerSaveDto dto, HttpServletRequest request,
                          @RequestHeader(value = "Authorization", required = false) String token) {
        checkAdmin(dto.getOperatorId(), token);
        dto.setIp(request.getRemoteAddr());
        return R.success(bannerService.save(null, dto));
    }

    @PutMapping("/{id}")
    public R<Banner> update(@PathVariable Integer id, @RequestBody BannerSaveDto dto, HttpServletRequest request,
                             @RequestHeader(value = "Authorization", required = false) String token) {
        checkAdmin(dto.getOperatorId(), token);
        dto.setIp(request.getRemoteAddr());
        return R.success(bannerService.save(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id, @RequestParam Integer operatorId, HttpServletRequest request,
                             @RequestHeader(value = "Authorization", required = false) String token) {
        checkAdmin(operatorId, token);
        SystemOperateDto dto = new SystemOperateDto();
        dto.setOperatorId(operatorId);
        dto.setIp(request.getRemoteAddr());
        bannerService.delete(id, dto);
        return R.success("删除成功");
    }

    @PostMapping("/image")
    public R<String> uploadImage(@RequestParam Integer operatorId, @RequestParam("file") MultipartFile file,
                                  @RequestHeader(value = "Authorization", required = false) String token) {
        checkAdmin(operatorId, token);
        return R.success(bannerService.uploadImage(file));
    }

    private void checkAdmin(Integer operatorId, String token) {
        if (!systemService.checkAdmin(operatorId, token))
            throw new BaseException("仅管理员可管理Banner");
    }
}
