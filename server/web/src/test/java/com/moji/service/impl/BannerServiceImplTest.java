package com.moji.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.FilePathEnum;
import com.moji.dto.BannerSaveDto;
import com.moji.dto.SystemOperateDto;
import com.moji.exception.BaseException;
import com.moji.mapper.BannerMapper;
import com.moji.po.Banner;
import com.moji.service.SystemLogService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {
    private static final String FILE_NAME = "12345678-1234-1234-1234-123456789abc.webp";
    @TempDir Path root;
    @Mock BannerMapper mapper;
    @Mock SystemLogService logs;
    @InjectMocks BannerServiceImpl service;
    @Captor ArgumentCaptor<Page<Banner>> pageCaptor;
    @Captor ArgumentCaptor<LambdaQueryWrapper<Banner>> queryCaptor;

    @BeforeEach
    void prepare() throws Exception {
        FilePathEnum.init(new MockEnvironment().withProperty("file.upload.root", root.toString())
                .withProperty("file.upload.safety-capacity", "0"));
        Files.createDirectories(root.resolve("banner"));
        Files.write(root.resolve("banner").resolve(FILE_NAME), new byte[]{1});
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Banner.class);
    }

    private BannerSaveDto dto() {
        BannerSaveDto dto = new BannerSaveDto();
        dto.setOperatorId(1);
        dto.setImageUrl("/api/banner/image/" + FILE_NAME);
        dto.setSortOrder(0);
        dto.setEnabled(1);
        return dto;
    }

    @Test
    void publicListFiltersEnabledOrdersAndLimitsToSix() {
        when(mapper.selectPage(ArgumentMatchers.<Page<Banner>>any(), any())).thenAnswer(invocation -> {
            Page<Banner> page = invocation.getArgument(0);
            page.setRecords(List.of(new Banner()));
            return page;
        });
        assertEquals(1, service.listVisible().size());
        verify(mapper).selectPage(pageCaptor.capture(), queryCaptor.capture());
        assertEquals(6, pageCaptor.getValue().getSize());
        assertFalse(pageCaptor.getValue().searchCount());
        String sql = queryCaptor.getValue().getSqlSegment();
        assertTrue(sql.contains("enabled"));
        assertTrue(sql.contains("ORDER BY sort_order ASC,id DESC"));
        assertTrue(queryCaptor.getValue().getParamNameValuePairs().containsValue(1));
    }

    @Test
    void createsImageOnlyBannerAndWritesAuditLog() {
        when(mapper.insert(any(Banner.class))).thenAnswer(invocation -> {
            Banner banner = invocation.getArgument(0);
            banner.setId(9);
            return 1;
        });
        Banner result = service.save(null, dto());
        assertEquals(9, result.getId());
        assertEquals("", result.getContent());
        assertEquals("", result.getLinkUrl());
        assertNotNull(result.getCreateTime());
        verify(logs).write(any(), eq("banner"), eq("addBanner"), eq("banner"),
                eq(9), eq(""), eq("新增Banner"), eq(1));
    }

    @Test
    void contentRequiresLink() {
        BannerSaveDto dto = dto();
        dto.setContent("活动内容");
        assertEquals("填写内容时必须填写链接",
                assertThrows(BaseException.class, () -> service.save(null, dto)).getMessage());
        verifyNoInteractions(mapper, logs);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/video?videoId=123", "/search?keyword=测试", "https://example.com/path?q=1", "http://example.com"})
    void acceptsValidLinksAndTrimsContent(String link) {
        when(mapper.insert(any(Banner.class))).thenReturn(1);
        BannerSaveDto dto = dto();
        dto.setContent("  活动内容  ");
        dto.setLinkUrl("  " + link + "  ");
        Banner result = service.save(null, dto);
        assertEquals("活动内容", result.getContent());
        assertEquals(link, result.getLinkUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "data:text/html,x", "//evil.example",
            "/\\evil.example", "ftp://example.com", "https://user:pass@example.com", "https:///missing", "/bad%"})
    void rejectsUnsafeOrMalformedLinks(String link) {
        BannerSaveDto dto = dto();
        dto.setLinkUrl(link);
        assertThrows(BaseException.class, () -> service.save(null, dto));
        verifyNoInteractions(mapper);
    }

    @Test
    void rejectsMissingImageAndOutOfRangeFields() {
        BannerSaveDto dto = dto();
        dto.setImageUrl("/api/banner/image/12345678-1234-1234-1234-123456789abd.webp");
        assertThrows(BaseException.class, () -> service.save(null, dto));
        dto.setImageUrl("/api/banner/image/" + FILE_NAME);
        dto.setContent("x".repeat(201));
        assertThrows(BaseException.class, () -> service.save(null, dto));
        dto.setContent("");
        dto.setSortOrder(-1);
        assertThrows(BaseException.class, () -> service.save(null, dto));
        dto.setSortOrder(0);
        dto.setEnabled(2);
        assertThrows(BaseException.class, () -> service.save(null, dto));
    }

    @Test
    void updateClearsOptionalFieldsAndCanHideBanner() {
        Banner existing = new Banner();
        existing.setId(4);
        existing.setContent("旧内容");
        existing.setLinkUrl("/old");
        when(mapper.selectById(4)).thenReturn(existing);
        when(mapper.updateById(any(Banner.class))).thenReturn(1);
        BannerSaveDto dto = dto();
        dto.setEnabled(0);
        Banner result = service.save(4, dto);
        assertEquals("", result.getContent());
        assertEquals("", result.getLinkUrl());
        assertEquals(0, result.getEnabled());
        verify(mapper).updateById(existing);
    }

    @Test
    void deletesExistingBannerAndRejectsMissingBanner() {
        Banner existing = new Banner();
        existing.setId(4);
        when(mapper.selectById(4)).thenReturn(existing);
        when(mapper.deleteById(4)).thenReturn(1);
        service.delete(4, new SystemOperateDto());
        verify(logs).write(any(), eq("banner"), eq("deleteBanner"), eq("banner"), eq(4), isNull(), anyString(), eq(1));
        assertThrows(BaseException.class, () -> service.delete(5, new SystemOperateDto()));
        verify(mapper, never()).deleteById(5);
    }

    @Test
    void rejectsInvalidPageAndFailedDatabaseWrite() {
        assertThrows(BaseException.class, () -> service.search(0, ""));
        assertThrows(BaseException.class, () -> service.search(1, "x".repeat(201)));
        assertThrows(BaseException.class, () -> service.save(null, dto()));
        verifyNoInteractions(logs);
    }

    @Test
    void uploadsDecodedImageAsWebp() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", bytes);
        String url = service.uploadImage(new MockMultipartFile("file", "banner.png", "image/png", bytes.toByteArray()));
        assertTrue(url.startsWith("/api/banner/image/"));
        Path saved = root.resolve("banner").resolve(url.substring(url.lastIndexOf('/') + 1));
        assertTrue(Files.size(saved) > 0);
        assertNotNull(ImageIO.read(saved.toFile()));
    }

    @Test
    void rejectsInvalidUploads() {
        assertThrows(BaseException.class, () -> service.uploadImage(new MockMultipartFile("file", new byte[0])));
        assertThrows(BaseException.class, () -> service.uploadImage(
                new MockMultipartFile("file", "x.svg", "image/svg+xml", new byte[]{1})));
        assertThrows(BaseException.class, () -> service.uploadImage(
                new MockMultipartFile("file", "x.png", "image/png", new byte[10 * 1024 * 1024 + 1])));
        assertThrows(BaseException.class, () -> service.uploadImage(
                new MockMultipartFile("file", "x.png", "image/png", new byte[]{1})));
    }
}
