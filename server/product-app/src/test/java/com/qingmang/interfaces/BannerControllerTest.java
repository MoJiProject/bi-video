package com.qingmang.interfaces;

import com.qingmang.application.BannerService;
import com.qingmang.application.MediaStorageService;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.common.exception.GlobalExceptionHandler;
import com.qingmang.domain.ops.Banner;
import com.qingmang.interfaces.dto.BannerRequest;
import com.qingmang.support.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BannerControllerTest {
    private static final String BODY = """
            {"imageUrl":"/upload/image/banner.png","content":"","linkUrl":"","sortOrder":0,"enabled":true,"operatorId":1}
            """;
    private BannerService banners;
    private MediaStorageService media;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        banners = mock(BannerService.class);
        media = mock(MediaStorageService.class);
        mvc = MockMvcBuilders.standaloneSetup(new BannerController(banners, media))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void anonymousHomeReadReturnsPlainProductResponse() throws Exception {
        when(banners.visible()).thenReturn(List.of(new Banner()));
        mvc.perform(get("/api/banner")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.payload").doesNotExist());
    }

    @Test
    void anonymousCannotListCreateUpdateDeleteOrUpload() throws Exception {
        try (var auth = mockStatic(AuthContext.class)) {
            auth.when(AuthContext::requireUserId).thenThrow(new BizException(ErrorCode.NOT_LOGIN));
            mvc.perform(get("/api/system/banner").param("operatorId", "1")).andExpect(jsonPath("$.code").value(20005));
            mvc.perform(post("/api/system/banner").contentType("application/json").content(BODY))
                    .andExpect(jsonPath("$.code").value(20005));
            mvc.perform(put("/api/system/banner/1").contentType("application/json").content(BODY))
                    .andExpect(jsonPath("$.code").value(20005));
            mvc.perform(delete("/api/system/banner/1")).andExpect(jsonPath("$.code").value(20005));
            mvc.perform(multipart("/api/system/banner/image").file(
                            new MockMultipartFile("file", "x.png", "image/png", new byte[]{1})))
                    .andExpect(jsonPath("$.code").value(20005));
        }
        verifyNoInteractions(banners, media);
    }

    @Test
    void authenticatedRoutesUseSessionIdentityRatherThanSuppliedOperatorId() throws Exception {
        try (var auth = mockStatic(AuthContext.class)) {
            auth.when(AuthContext::requireUserId).thenReturn(7L);
            when(banners.search(7L, 2, "offer")).thenReturn(PageResult.empty(2, 10));
            when(banners.save(eq(7L), isNull(), any(), anyString())).thenReturn(new Banner());
            when(banners.save(eq(7L), eq(9L), any(), anyString())).thenReturn(new Banner());
            mvc.perform(get("/api/system/banner").param("operatorId", "1").param("pageNum", "2").param("keyword", "offer"))
                    .andExpect(jsonPath("$.data.pageNum").value(2));
            mvc.perform(post("/api/system/banner").contentType("application/json").content(BODY))
                    .andExpect(jsonPath("$.code").value(1));
            mvc.perform(put("/api/system/banner/9").contentType("application/json").content(BODY))
                    .andExpect(jsonPath("$.code").value(1));
            mvc.perform(delete("/api/system/banner/9").param("operatorId", "1")).andExpect(jsonPath("$.code").value(1));
        }
        var request = ArgumentCaptor.forClass(BannerRequest.class);
        verify(banners).save(eq(7L), isNull(), request.capture(), anyString());
        assertEquals(0, request.getValue().getSortOrder());
        verify(banners).delete(eq(7L), eq(9L), anyString());
    }

    @Test
    void uploadRequiresAdminBeforeStoringFileAndReturnsUrl() throws Exception {
        try (var auth = mockStatic(AuthContext.class)) {
            auth.when(AuthContext::requireUserId).thenReturn(7L);
            var file = new MockMultipartFile("file", "x.png", "image/png", new byte[]{1});
            doThrow(new BizException(ErrorCode.NO_PERMISSION)).when(banners).requireAdmin(7L);
            mvc.perform(multipart("/api/system/banner/image").file(file)).andExpect(jsonPath("$.code").value(20006));
            verifyNoInteractions(media);
            doReturn(null).when(banners).requireAdmin(7L);
            when(media.store(any(), eq("image"), eq(7L))).thenReturn("/upload/image/test.png");
            mvc.perform(multipart("/api/system/banner/image").file(file))
                    .andExpect(jsonPath("$.data.url").value("/upload/image/test.png"));
        }
    }

    @Test
    void invalidRequestsAreRejectedBeforeSaving() throws Exception {
        mvc.perform(post("/api/system/banner").contentType("application/json")
                        .content("{\"imageUrl\":\"\",\"sortOrder\":-1,\"enabled\":true}"))
                .andExpect(jsonPath("$.code").value(10001));
        mvc.perform(post("/api/system/banner").contentType("application/json")
                        .content("{\"imageUrl\":\"image\",\"sortOrder\":0}"))
                .andExpect(jsonPath("$.code").value(10001));
        verifyNoInteractions(banners);
    }
}
