package com.moji.controller;

import com.moji.FilePathEnum;
import com.moji.handler.GlobalExceptionHandler;
import com.moji.po.Banner;
import com.moji.service.BannerService;
import com.moji.service.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BannerControllerTest {
    @TempDir Path root;
    @Mock BannerService banners;
    @Mock SystemService admins;
    private MockMvc mvc;

    @BeforeEach
    void prepare() {
        FilePathEnum.init(new MockEnvironment().withProperty("file.upload.root", root.toString()));
        mvc = MockMvcBuilders.standaloneSetup(new BannerController(banners), new SystemBannerController(banners, admins))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void anonymousUserCanReadHomeBanners() throws Exception {
        Banner banner = new Banner();
        banner.setId(1);
        banner.setImageUrl("/api/banner/image/test.webp");
        when(banners.listVisible()).thenReturn(List.of(banner));
        mvc.perform(get("/banner")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data[0].id").value(1));
        verifyNoInteractions(admins);
    }

    @Test
    void nonAdminCannotListSaveDeleteOrUpload() throws Exception {
        mvc.perform(get("/system/banner").param("operatorId", "2"))
                .andExpect(jsonPath("$.code").value(0));
        mvc.perform(post("/system/banner").contentType("application/json").content("{\"operatorId\":2}"))
                .andExpect(jsonPath("$.code").value(0));
        mvc.perform(put("/system/banner/1").contentType("application/json").content("{\"operatorId\":2}"))
                .andExpect(jsonPath("$.code").value(0));
        mvc.perform(delete("/system/banner/1").param("operatorId", "2"))
                .andExpect(jsonPath("$.code").value(0));
        mvc.perform(multipart("/system/banner/image").file(
                        new MockMultipartFile("file", "x.png", "image/png", new byte[]{1})).param("operatorId", "2"))
                .andExpect(jsonPath("$.code").value(0));
        verifyNoInteractions(banners);
    }

    @Test
    void adminSaveChecksIdentityAndReturnsCreatedBanner() throws Exception {
        when(admins.checkAdmin(1, "admin-token")).thenReturn(true);
        Banner banner = new Banner();
        banner.setId(3);
        when(banners.save(isNull(), any())).thenReturn(banner);
        mvc.perform(post("/system/banner").header("Authorization", "admin-token")
                        .contentType("application/json").content("{\"operatorId\":1,\"imageUrl\":\"image\"}"))
                .andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data.id").value(3));
        verify(admins).checkAdmin(1, "admin-token");
        verify(banners).save(isNull(), argThat(dto -> dto.getOperatorId() == 1 && dto.getIp() != null));
    }

    @Test
    void imageEndpointReturnsRawImageAndRejectsOtherNames() throws Exception {
        String name = "12345678-1234-1234-1234-123456789abc.webp";
        Files.createDirectories(root.resolve("banner"));
        Files.write(root.resolve("banner").resolve(name), new byte[]{1, 2, 3});
        mvc.perform(get("/banner/image/" + name)).andExpect(status().isOk())
                .andExpect(content().contentType("image/webp")).andExpect(content().bytes(new byte[]{1, 2, 3}));
        mvc.perform(get("/banner/image/other.webp")).andExpect(status().isNotFound());
        mvc.perform(get("/banner/image/12345678-1234-1234-1234-123456789abd.webp"))
                .andExpect(status().isNotFound());
    }
}
