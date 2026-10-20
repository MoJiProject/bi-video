package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.FilePathEnum;
import com.moji.dto.BannerSaveDto;
import com.moji.dto.SystemOperateDto;
import com.moji.exception.BaseException;
import com.moji.mapper.BannerMapper;
import com.moji.po.Banner;
import com.moji.service.BannerService;
import com.moji.service.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BannerServiceImpl implements BannerService {
    private static final String IMAGE_PREFIX = "/api/banner/image/";
    private static final Pattern IMAGE_URL = Pattern.compile(
            "^/api/banner/image/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.webp$");
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private final BannerMapper bannerMapper;
    private final SystemLogService systemLogService;

    public BannerServiceImpl(BannerMapper bannerMapper, SystemLogService systemLogService) {
        this.bannerMapper = bannerMapper;
        this.systemLogService = systemLogService;
    }

    @Override
    public List<Banner> listVisible() {
        Page<Banner> page = new Page<>(1, 6, false);
        return bannerMapper.selectPage(page, ordered().eq(Banner::getEnabled, 1)).getRecords();
    }

    @Override
    public Page<Banner> search(Integer pageNum, String keyword) {
        if (pageNum == null || pageNum < 1 || pageNum > 1000000)
            throw new BaseException("页码无效");
        String value = keyword == null ? "" : keyword.trim();
        if (value.length() > 200)
            throw new BaseException("搜索内容不能超过200个字符");
        return bannerMapper.selectPage(new Page<>(pageNum, 10),
                ordered().like(!value.isEmpty(), Banner::getContent, value));
    }

    private LambdaQueryWrapper<Banner> ordered() {
        return new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder).orderByDesc(Banner::getId);
    }

    @Override
    @Transactional
    public Banner save(Integer id, BannerSaveDto dto) {
        Banner banner = id == null ? new Banner() : requireBanner(id);
        validate(dto);
        banner.setImageUrl(dto.getImageUrl().trim());
        banner.setContent(dto.getContent() == null ? "" : dto.getContent().trim());
        banner.setLinkUrl(dto.getLinkUrl() == null ? "" : dto.getLinkUrl().trim());
        banner.setSortOrder(dto.getSortOrder());
        banner.setEnabled(dto.getEnabled());
        banner.setUpdateTime(LocalDateTime.now());
        int changed;
        if (id == null) {
            banner.setCreateTime(banner.getUpdateTime());
            changed = bannerMapper.insert(banner);
        } else {
            changed = bannerMapper.updateById(banner);
        }
        if (changed != 1)
            throw new BaseException("保存Banner失败，请刷新后重试");
        systemLogService.write(dto, "banner", id == null ? "addBanner" : "putBanner", "banner",
                banner.getId(), banner.getContent(), id == null ? "新增Banner" : "修改Banner", 1);
        return banner;
    }

    private void validate(BannerSaveDto dto) {
        String imageUrl = dto.getImageUrl() == null ? "" : dto.getImageUrl().trim();
        if (!IMAGE_URL.matcher(imageUrl).matches())
            throw new BaseException("请上传Banner图片");
        Path image = Path.of(FilePathEnum.UPLOAD_BANNER.getPath(), imageUrl.substring(IMAGE_PREFIX.length()));
        if (!Files.isRegularFile(image))
            throw new BaseException("Banner图片不存在，请重新上传");
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        String link = dto.getLinkUrl() == null ? "" : dto.getLinkUrl().trim();
        if (content.length() > 200)
            throw new BaseException("内容不能超过200个字符");
        if (!content.isEmpty() && link.isEmpty())
            throw new BaseException("填写内容时必须填写链接");
        if (link.length() > 2000 || (!link.isEmpty() && !validLink(link)))
            throw new BaseException("链接必须是站内路径或有效的HTTP/HTTPS地址");
        if (dto.getSortOrder() == null || dto.getSortOrder() < 0 || dto.getSortOrder() > 999999)
            throw new BaseException("排序必须是0到999999之间的整数");
        if (dto.getEnabled() == null || (dto.getEnabled() != 0 && dto.getEnabled() != 1))
            throw new BaseException("展示状态无效");
    }

    private boolean validLink(String value) {
        if (value.contains("\\") || value.chars().anyMatch(Character::isISOControl))
            return false;
        try {
            URI uri = new URI(value);
            if (value.startsWith("/") && !value.startsWith("//"))
                return uri.getScheme() == null && uri.getRawAuthority() == null;
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void delete(Integer id, SystemOperateDto dto) {
        Banner banner = requireBanner(id);
        if (bannerMapper.deleteById(id) != 1)
            throw new BaseException("删除Banner失败，请刷新后重试");
        systemLogService.write(dto, "banner", "deleteBanner", "banner",
                id, banner.getContent(), "删除Banner", 1);
    }

    private Banner requireBanner(Integer id) {
        if (id == null || id <= 0)
            throw new BaseException("Banner编号无效");
        Banner banner = bannerMapper.selectById(id);
        if (banner == null)
            throw new BaseException("Banner不存在或已被删除");
        return banner;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 10 * 1024 * 1024)
            throw new BaseException("请选择不超过10MB的图片");
        if (file.getContentType() == null || !IMAGE_TYPES.contains(file.getContentType()))
            throw new BaseException("仅支持JPG、PNG、GIF和WebP图片");
        if (FilePathEnum.canUpload())
            throw new BaseException("服务器存储空间不足，请联系管理员");
        String name = UUID.randomUUID().toString();
        try {
            FilePathEnum.saveAsWebp(file.getBytes(), Path.of(FilePathEnum.UPLOAD_BANNER.getPath()).toFile(), name);
        } catch (IOException e) {
            log.error("上传Banner图片失败", e);
            throw new BaseException("图片上传失败，请检查图片格式或重试");
        }
        return IMAGE_PREFIX + name + ".webp";
    }
}
