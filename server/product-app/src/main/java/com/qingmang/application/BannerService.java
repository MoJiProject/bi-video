package com.qingmang.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.ops.Banner;
import com.qingmang.domain.ops.OperationLog;
import com.qingmang.domain.ops.mapper.BannerMapper;
import com.qingmang.domain.ops.mapper.OperationLogMapper;
import com.qingmang.domain.user.User;
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.interfaces.dto.BannerRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerService {
    private final BannerMapper banners;
    private final UserMapper users;
    private final OperationLogMapper logs;
    private final MediaStorageService media;

    public BannerService(BannerMapper banners, UserMapper users, OperationLogMapper logs, MediaStorageService media) {
        this.banners = banners;
        this.users = users;
        this.logs = logs;
        this.media = media;
    }

    public User requireAdmin(Long userId) {
        if (userId == null) throw new BizException(ErrorCode.NOT_LOGIN);
        User user = users.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getRole())
                || !Integer.valueOf(1).equals(user.getStatus()) || user.getDeletedAt() != null)
            throw new BizException(ErrorCode.NO_PERMISSION);
        return user;
    }

    public List<Banner> visible() {
        return banners.selectPage(new Page<Banner>(1, 6, false),
                ordered().eq(Banner::getEnabled, true)).getRecords();
    }

    public PageResult<Banner> search(Long userId, int pageNum, String keyword) {
        requireAdmin(userId);
        String value = trim(keyword);
        if (pageNum < 1 || pageNum > 1000000 || value.length() > 200)
            throw new BizException(ErrorCode.PARAM_INVALID);
        Page<Banner> page = banners.selectPage(new Page<>(pageNum, 10),
                ordered().like(!value.isEmpty(), Banner::getContent, value));
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, 10);
    }

    private LambdaQueryWrapper<Banner> ordered() {
        return new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder).orderByDesc(Banner::getId);
    }

    @Transactional
    public Banner save(Long userId, Long id, BannerRequest request, String ip) {
        User admin = requireAdmin(userId);
        Banner banner = id == null ? new Banner() : requireBanner(id);
        String content = trim(request.getContent());
        String link = trim(request.getLinkUrl());
        if (content.length() > 200 || link.length() > 2000)
            throw new BizException(ErrorCode.PARAM_INVALID, "内容或链接过长");
        if (!content.isEmpty() && link.isEmpty())
            throw new BizException(ErrorCode.PARAM_INVALID, "填写内容时必须填写链接");
        if (!link.isEmpty() && !validLink(link))
            throw new BizException(ErrorCode.PARAM_INVALID, "链接必须是站内路径或有效的HTTP/HTTPS地址");
        if (request.getSortOrder() == null || request.getSortOrder() < 0 || request.getSortOrder() > 999999
                || request.getEnabled() == null)
            throw new BizException(ErrorCode.PARAM_INVALID);
        String image = trim(request.getImageUrl());
        media.requireStoredImage(image);
        banner.setImageUrl(image);
        banner.setContent(content);
        banner.setLinkUrl(link);
        banner.setSortOrder(request.getSortOrder());
        banner.setEnabled(request.getEnabled());
        banner.setUpdatedAt(LocalDateTime.now());
        int changed;
        if (id == null) {
            banner.setCreatedAt(banner.getUpdatedAt());
            changed = banners.insert(banner);
        } else {
            changed = banners.updateById(banner);
        }
        if (changed != 1) throw new BizException(ErrorCode.DB_ERROR);
        audit(admin, banner, id == null ? "addBanner" : "putBanner", ip);
        return banner;
    }

    @Transactional
    public void delete(Long userId, Long id, String ip) {
        User admin = requireAdmin(userId);
        Banner banner = requireBanner(id);
        if (banners.deleteById(id) != 1) throw new BizException(ErrorCode.DB_ERROR);
        audit(admin, banner, "deleteBanner", ip);
    }

    private Banner requireBanner(Long id) {
        if (id == null || id <= 0) throw new BizException(ErrorCode.PARAM_INVALID);
        Banner banner = banners.selectById(id);
        if (banner == null) throw new BizException(ErrorCode.NOT_FOUND, "Banner不存在或已被删除");
        return banner;
    }

    private void audit(User admin, Banner banner, String action, String ip) {
        OperationLog entry = new OperationLog().setOperatorId(admin.getId()).setOperatorName(admin.getNickname())
                .setModule("banner").setAction(action).setTargetType("banner").setTargetId(banner.getId())
                .setDetail(banner.getContent()).setSuccess(true).setIp(ip).setCreatedAt(LocalDateTime.now());
        if (logs.insert(entry) != 1) throw new BizException(ErrorCode.DB_ERROR, "保存操作日志失败");
    }

    private boolean validLink(String value) {
        if (value.contains("\\") || value.chars().anyMatch(c -> Character.isISOControl(c) || Character.isWhitespace(c)))
            return false;
        try {
            URI uri = new URI(value);
            if (value.startsWith("/") && !value.startsWith("//"))
                return uri.getScheme() == null && uri.getRawAuthority() == null;
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
}
