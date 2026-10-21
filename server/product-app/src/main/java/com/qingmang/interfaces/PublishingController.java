package com.qingmang.interfaces;

import com.qingmang.application.AuthService;
import com.qingmang.application.MediaStorageService;
import com.qingmang.application.PublishingService;
import com.qingmang.application.VideoChunkStorage;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.PublishingRequest;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class PublishingController {
    private final PublishingService publishing;
    private final VideoChunkStorage chunks;
    private final MediaStorageService media;
    private final AuthService auth;

    public PublishingController(PublishingService publishing, VideoChunkStorage chunks,
                                 MediaStorageService media, AuthService auth) {
        this.publishing = publishing;
        this.chunks = chunks;
        this.media = media;
        this.auth = auth;
    }

    @PostMapping("/upload/uploadVideoChunk")
    public ApiResponse<Integer> chunk(@RequestParam MultipartFile file, @RequestParam String uploadId,
                                      @RequestParam int chunkIndex, @RequestParam int totalChunks) {
        Long actor = AuthContext.requireUserId();
        auth.requireActiveUser(actor);
        chunks.store(actor, uploadId, chunkIndex, totalChunks, file);
        return ApiResponse.ok(chunkIndex);
    }

    @PostMapping("/upload/mergeVideoChunks")
    public ApiResponse<String> merge(@RequestParam String uploadId, @RequestParam int totalChunks) {
        Long actor = AuthContext.requireUserId();
        auth.requireActiveUser(actor);
        return ApiResponse.ok(chunks.merge(actor, uploadId, totalChunks));
    }

    @DeleteMapping("/upload/cleanVideoUpload")
    public ApiResponse<Void> clean(@RequestParam(required = false) String uploadId,
                                   @RequestParam(required = false) String videoName) {
        Long actor = AuthContext.requireUserId();
        auth.requireActiveUser(actor);
        if (uploadId != null && !uploadId.isBlank()) chunks.clean(actor, uploadId);
        if (videoName != null && !videoName.isBlank() && !publishing.inUse(videoName))
            media.deleteOwnedVideo(videoName, actor);
        return ApiResponse.ok();
    }

    @PostMapping("/upload/uploadVideo")
    public ApiResponse<String> publish(@Valid @ModelAttribute PublishingRequest request,
                                       @RequestParam(required = false) MultipartFile file,
                                       @RequestParam(required = false) String videoName,
                                       @RequestParam(value = "tag[]", required = false) List<String> tags) {
        publishing.save(AuthContext.requireUserId(), request, file, videoName, tags, false);
        return ApiResponse.ok("上传成功");
    }

    @PutMapping("/upload/updateVideo")
    public ApiResponse<String> edit(@Valid @ModelAttribute PublishingRequest request,
                                    @RequestParam(required = false) MultipartFile file,
                                    @RequestParam(required = false) String videoName,
                                    @RequestParam(value = "tag[]", required = false) List<String> tags) {
        publishing.save(AuthContext.requireUserId(), request, file, videoName, tags, true);
        return ApiResponse.ok("修改成功");
    }

    @GetMapping("/video/getVideoInfo")
    public ApiResponse<Map<String, Object>> detail(@RequestParam Long videoId) {
        return ApiResponse.ok(publishing.detail(AuthContext.requireUserId(), videoId));
    }

    @GetMapping("/video/userUploadVideo")
    public ApiResponse<Map<String, Object>> list(@RequestParam(required = false) String videoTitle,
                                                @RequestParam(required = false) String subZoneKey,
                                                @RequestParam(required = false) String sortWay,
                                                @RequestParam(defaultValue = "-1") int videoStatus,
                                                @RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(publishing.list(AuthContext.requireUserId(), videoTitle, subZoneKey, sortWay, videoStatus, pageNum));
    }

    @DeleteMapping("/video/deleteVideo")
    public ApiResponse<String> delete(@RequestParam Long videoId) {
        publishing.delete(AuthContext.requireUserId(), videoId);
        return ApiResponse.ok("删除成功");
    }

    @PostMapping("/video/selectVideoData")
    public ApiResponse<Map<String, Object>> statistics() {
        return ApiResponse.ok(publishing.statistics(AuthContext.requireUserId()));
    }
}
