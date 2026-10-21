package com.qingmang.application;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmang.common.exception.BizException;
import com.qingmang.domain.ops.Banner;
import com.qingmang.domain.ops.OperationLog;
import com.qingmang.domain.ops.mapper.BannerMapper;
import com.qingmang.domain.ops.mapper.OperationLogMapper;
import com.qingmang.domain.user.User;
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.interfaces.dto.BannerRequest;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BannerServiceTest {
    private BannerMapper banners;
    private UserMapper users;
    private OperationLogMapper logs;
    private MediaStorageService media;
    private BannerService service;

    @BeforeEach
    void setUp() {
        banners = mock(BannerMapper.class);
        users = mock(UserMapper.class);
        logs = mock(OperationLogMapper.class);
        media = mock(MediaStorageService.class);
        service = new BannerService(banners, users, logs, media);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Banner.class);
    }

    private void admin() {
        when(users.selectById(7L)).thenReturn(new User().setId(7L).setNickname("Admin").setRole(1).setStatus(1));
    }

    private BannerRequest request() {
        BannerRequest request = new BannerRequest();
        request.setImageUrl("/upload/image/7_12345678-1234-1234-1234-123456789abc.png");
        request.setSortOrder(0);
        request.setEnabled(true);
        return request;
    }

    @Test
    void publicQueryFiltersEnabledOrdersAndLimitsToSixWithoutCount() {
        when(banners.selectPage(any(Page.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertTrue(service.visible().isEmpty());
        var page = ArgumentCaptor.forClass(Page.class);
        var query = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(banners).selectPage(page.capture(), query.capture());
        assertEquals(6, page.getValue().getSize());
        assertFalse(page.getValue().searchCount());
        String sql = query.getValue().getSqlSegment();
        assertTrue(sql.contains("enabled"));
        assertTrue(sql.contains("ORDER BY sort_order ASC,id DESC"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(true));
        verifyNoInteractions(users);
    }

    @Test
    void administratorQueryPaginatesAndSearches() {
        admin();
        when(banners.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Banner> page = invocation.getArgument(0);
            page.setTotal(21);
            return page;
        });
        var result = service.search(7L, 2, " offer ");
        assertEquals(21, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertThrows(BizException.class, () -> service.search(7L, 0, ""));
        assertThrows(BizException.class, () -> service.search(7L, 1, "a".repeat(201)));
    }

    @Test
    void rejectsMissingOrdinaryDisabledAndDeletedAdministratorsBeforeMutation() {
        assertThrows(BizException.class, () -> service.requireAdmin(null));
        assertThrows(BizException.class, () -> service.search(7L, 1, ""));
        when(users.selectById(7L)).thenReturn(new User().setRole(0).setStatus(1));
        assertThrows(BizException.class, () -> service.save(7L, null, request(), "ip"));
        when(users.selectById(7L)).thenReturn(new User().setRole(1).setStatus(0));
        assertThrows(BizException.class, () -> service.delete(7L, 1L, "ip"));
        when(users.selectById(7L)).thenReturn(new User().setRole(1).setStatus(1).setDeletedAt(LocalDateTime.now()));
        assertThrows(BizException.class, () -> service.requireAdmin(7L));
        verifyNoInteractions(banners, logs, media);
    }

    @Test
    void createsImageOnlyBannerAndAuditWithServerSideIdentity() {
        admin();
        when(banners.insert(any(Banner.class))).thenAnswer(invocation -> {
            Banner banner = invocation.getArgument(0);
            banner.setId(9L);
            return 1;
        });
        when(logs.insert(any(OperationLog.class))).thenReturn(1);
        Banner banner = service.save(7L, null, request(), "127.0.0.1");
        assertEquals(9L, banner.getId());
        assertEquals("", banner.getContent());
        assertEquals("", banner.getLinkUrl());
        assertNotNull(banner.getCreatedAt());
        verify(media).requireStoredImage(banner.getImageUrl());
        var audit = ArgumentCaptor.forClass(OperationLog.class);
        verify(logs).insert(audit.capture());
        assertEquals(7L, audit.getValue().getOperatorId());
        assertEquals("banner", audit.getValue().getModule());
        assertEquals("addBanner", audit.getValue().getAction());
        assertEquals(9L, audit.getValue().getTargetId());
    }

    @Test
    void contentRequiresLinkBeforeDatabaseWrites() {
        admin();
        BannerRequest request = request();
        request.setContent("Offer");
        assertEquals("填写内容时必须填写链接",
                assertThrows(BizException.class, () -> service.save(7L, null, request, "ip")).getMessage());
        verifyNoInteractions(banners, logs);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/video?videoId=BV1", "/search?keyword=活动", "https://example.com/path?q=1", "http://example.com"})
    void acceptsSafeLinks(String link) {
        admin();
        when(banners.insert(any(Banner.class))).thenReturn(1);
        when(logs.insert(any(OperationLog.class))).thenReturn(1);
        BannerRequest request = request();
        request.setContent(" Offer ");
        request.setLinkUrl(" " + link + " ");
        Banner result = service.save(7L, null, request, "ip");
        assertEquals("Offer", result.getContent());
        assertEquals(link, result.getLinkUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "data:text/html,x", "//evil.example", "/\\evil.example",
            "ftp://example.com", "https://user:pass@example.com", "https:///missing", "/bad%", "/bad\npath"})
    void rejectsUnsafeLinks(String link) {
        admin();
        BannerRequest request = request();
        request.setLinkUrl(link);
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
        verifyNoInteractions(banners, logs);
    }

    @Test
    void updateClearsContentLinkAndCanHideWithoutChangingCreationTime() {
        admin();
        Banner original = new Banner();
        original.setId(1L);
        original.setContent("Old");
        original.setLinkUrl("/old");
        original.setCreatedAt(LocalDateTime.now().minusDays(1));
        var created = original.getCreatedAt();
        when(banners.selectById(1L)).thenReturn(original);
        when(banners.updateById(any(Banner.class))).thenReturn(1);
        when(logs.insert(any(OperationLog.class))).thenReturn(1);
        BannerRequest request = request();
        request.setEnabled(false);
        request.setSortOrder(2);
        var result = service.save(7L, 1L, request, "ip");
        assertEquals("", result.getContent());
        assertEquals("", result.getLinkUrl());
        assertFalse(result.getEnabled());
        assertEquals(2, result.getSortOrder());
        assertEquals(created, result.getCreatedAt());
    }

    @Test
    void deletesExistingBannerAndAudits() {
        admin();
        Banner original = new Banner();
        original.setId(1L);
        when(banners.selectById(1L)).thenReturn(original);
        when(banners.deleteById(1L)).thenReturn(1);
        when(logs.insert(any(OperationLog.class))).thenReturn(1);
        service.delete(7L, 1L, "ip");
        verify(banners).deleteById(1L);
        verify(logs).insert(any(OperationLog.class));
        assertThrows(BizException.class, () -> service.delete(7L, 2L, "ip"));
        verify(banners, never()).deleteById(2L);
    }

    @Test
    void failsExplicitlyOnInvalidFieldsDatabaseOrAuditFailure() {
        admin();
        BannerRequest request = request();
        request.setContent("a".repeat(201));
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
        request.setContent("");
        request.setSortOrder(-1);
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
        request.setSortOrder(0);
        request.setEnabled(null);
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
        request.setEnabled(true);
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
        when(banners.insert(any(Banner.class))).thenReturn(1);
        assertThrows(BizException.class, () -> service.save(7L, null, request, "ip"));
    }
}
