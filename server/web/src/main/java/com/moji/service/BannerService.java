package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.dto.BannerSaveDto;
import com.moji.dto.SystemOperateDto;
import com.moji.po.Banner;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BannerService {
    List<Banner> listVisible();
    Page<Banner> search(Integer pageNum, String keyword);
    Banner save(Integer id, BannerSaveDto dto);
    void delete(Integer id, SystemOperateDto dto);
    String uploadImage(MultipartFile file);
}
