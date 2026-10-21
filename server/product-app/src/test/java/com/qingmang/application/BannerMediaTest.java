package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class BannerMediaTest {
    @TempDir Path root;

    @Test
    void uploadedImageIsPersistedAndCanBeUsedForBanner() throws Exception {
        MediaStorageService media = new MediaStorageService(root.toString());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", bytes);
        String url = media.store(new MockMultipartFile("file", "banner.png", "image/png", bytes.toByteArray()), "image", 7L);
        assertDoesNotThrow(() -> media.requireStoredImage(url));
        assertNotNull(ImageIO.read(root.resolve(url.substring("/upload/".length())).toFile()));
    }

    @Test
    void rejectsMissingNonImageExternalAndTraversalPaths() {
        MediaStorageService media = new MediaStorageService(root.toString());
        for (String url : new String[]{null, "", "https://example.com/x.png", "/upload/image/../x.png",
                "/upload/image/7_12345678-1234-1234-1234-123456789abc.png", "/upload/video/7_file.mp4"}) {
            assertThrows(BizException.class, () -> media.requireStoredImage(url));
        }
    }

    @Test
    void rejectsOversizeUndecodableAndSvgUploads() {
        MediaStorageService media = new MediaStorageService(root.toString());
        assertThrows(BizException.class, () -> media.store(
                new MockMultipartFile("file", "x.png", "image/png", new byte[10 * 1024 * 1024 + 1]), "image", 7L));
        assertThrows(BizException.class, () -> media.store(
                new MockMultipartFile("file", "x.png", "image/png", new byte[]{1}), "image", 7L));
        assertThrows(BizException.class, () -> media.store(
                new MockMultipartFile("file", "x.svg", "image/svg+xml", "<svg/>".getBytes()), "image", 7L));
    }
}
