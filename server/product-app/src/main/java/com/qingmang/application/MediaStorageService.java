package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {
    private final Path root;
    public MediaStorageService(@Value("${app.upload-dir:../static/public/upload}") String directory) {
        root = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, String category, Long userId) {
        if (userId == null || !Set.of("avatar", "background", "image", "cover", "video").contains(category))
            throw new BizException(ErrorCode.PARAM_INVALID);
        boolean video = "video".equals(category);
        if (file == null || file.isEmpty() || file.getSize() > (video ? 500L : 10L) * 1024 * 1024)
            throw new BizException(ErrorCode.PARAM_INVALID, video ? "视频不能为空且不能超过 500MB" : "图片不能为空且不能超过 10MB");
        try {
            String extension = video ? videoExtension(file) : imageExtension(file);
            String name = userId + "_" + UUID.randomUUID() + "." + extension;
            Path directory = root.resolve(category);
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, directory.resolve(name));
            }
            return "/upload/" + category + "/" + name;
        } catch (IOException e) {
            throw new BizException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private String imageExtension(MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream(); ImageInputStream image = ImageIO.createImageInputStream(input)) {
            if (image == null) throw new BizException(ErrorCode.PARAM_INVALID, "图片格式不正确");
            var readers = ImageIO.getImageReaders(image);
            if (!readers.hasNext()) throw new BizException(ErrorCode.PARAM_INVALID, "仅支持 PNG、JPEG 和 GIF 图片");
            ImageReader reader = readers.next();
            try {
                reader.setInput(image);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "gif").contains(format) ||
                        reader.getWidth(0) > 16000 || reader.getHeight(0) > 16000)
                    throw new BizException(ErrorCode.PARAM_INVALID, "图片格式或尺寸不正确");
                return "jpeg".equals(format) ? "jpg" : format;
            } finally { reader.dispose(); }
        }
    }

    private String videoExtension(MultipartFile file) throws IOException {
        byte[] header;
        try (InputStream input = file.getInputStream()) { header = input.readNBytes(12); }
        if (header.length >= 8 && "ftyp".equals(new String(header, 4, 4, StandardCharsets.US_ASCII))) return "mp4";
        if (header.length >= 4 && Arrays.equals(Arrays.copyOf(header, 4),
                new byte[]{0x1a, 0x45, (byte) 0xdf, (byte) 0xa3})) return "webm";
        if (header.length >= 4 && "OggS".equals(new String(header, 0, 4, StandardCharsets.US_ASCII))) return "ogg";
        throw new BizException(ErrorCode.PARAM_INVALID, "仅支持 MP4、WebM 和 Ogg 视频");
    }
}
