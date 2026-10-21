package com.qingmang.application;

import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoChunkStorage {
    private static final int CHUNK_SIZE = 4 * 1024 * 1024;
    private static final int MAX_CHUNKS = 4096;
    private final Object[] locks = IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();
    private final Path root;
    private final MediaStorageService media;

    public VideoChunkStorage(@Value("${app.upload-dir:../static/public/upload}") String directory,
                              MediaStorageService media) {
        root = Path.of(directory).toAbsolutePath().normalize().resolve(".chunks");
        this.media = media;
    }

    public void store(Long userId, String uploadId, int index, int total, MultipartFile file) {
        Path directory = directory(userId, uploadId);
        validate(total);
        if (index < 0 || index >= total || file == null || file.isEmpty() || file.getSize() > CHUNK_SIZE)
            throw new BizException(ErrorCode.PARAM_INVALID, "视频分片参数不正确");
        synchronized (lock(userId, uploadId)) {
            try {
                Files.createDirectories(directory);
                Path manifest = directory.resolve("total");
                if (Files.exists(manifest) && !Files.readString(manifest).equals(Integer.toString(total)))
                    throw new BizException(ErrorCode.PARAM_INVALID, "视频分片总数不一致");
                Files.writeString(manifest, Integer.toString(total));
                try (var input = file.getInputStream()) {
                    Files.copy(input, directory.resolve(index + ".part"), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) { throw new BizException(ErrorCode.UPLOAD_FAILED, "保存视频分片失败"); }
        }
    }

    public String merge(Long userId, String uploadId, int total) {
        Path directory = directory(userId, uploadId);
        validate(total);
        synchronized (lock(userId, uploadId)) {
            try {
                if (!Files.isRegularFile(directory.resolve("total"))
                        || !Files.readString(directory.resolve("total")).equals(Integer.toString(total)))
                    throw new BizException(ErrorCode.PARAM_INVALID, "视频分片不完整");
                for (int i = 0; i < total; i++) {
                    Path part = directory.resolve(i + ".part");
                    if (!Files.isRegularFile(part) || Files.size(part) <= 0 || Files.size(part) > CHUNK_SIZE
                            || (i < total - 1 && Files.size(part) != CHUNK_SIZE))
                        throw new BizException(ErrorCode.PARAM_INVALID, "视频分片不完整，请重新上传");
                }
                Path merged = directory.resolve("merged");
                try (OutputStream output = Files.newOutputStream(merged)) {
                    for (int i = 0; i < total; i++) Files.copy(directory.resolve(i + ".part"), output);
                }
                String url = media.storeMergedVideo(merged, userId);
                try { clean(userId, uploadId); }
                catch (BizException cleanup) {
                    try { media.deleteOwnedVideo(url, userId); }
                    catch (BizException deletion) { cleanup.addSuppressed(deletion); }
                    throw cleanup;
                }
                return url;
            } catch (IOException e) { throw new BizException(ErrorCode.UPLOAD_FAILED, "合并视频分片失败"); }
        }
    }

    public void clean(Long userId, String uploadId) {
        Path directory = directory(userId, uploadId);
        synchronized (lock(userId, uploadId)) {
            if (!Files.exists(directory)) return;
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            } catch (IOException e) { throw new BizException(ErrorCode.UPLOAD_FAILED, "清理视频分片失败"); }
        }
    }

    private Path directory(Long userId, String uploadId) {
        if (userId == null || uploadId == null || !uploadId.matches("[a-zA-Z0-9_-]{16,80}"))
            throw new BizException(ErrorCode.PARAM_INVALID, "上传标识不正确");
        return root.resolve(userId.toString()).resolve(uploadId);
    }

    private Object lock(Long userId, String uploadId) {
        return locks[Math.floorMod((userId + ":" + uploadId).hashCode(), locks.length)];
    }

    private void validate(int total) {
        if (total < 1 || total > MAX_CHUNKS) throw new BizException(ErrorCode.PARAM_INVALID, "视频不能超过16GB");
    }
}
