package com.qingmang.interfaces;

import com.qingmang.application.UserContentService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.interfaces.dto.FolderRequest;
import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserContentController {

    private final UserContentService service;

    public UserContentController(UserContentService service) {
        this.service = service;
    }

    /* ---------------- 收藏夹 ---------------- */

    @GetMapping("/folder/list")
    public ApiResponse<List<FolderVO>> folders() {
        return ApiResponse.ok(service.folders(AuthContext.requireUserId()));
    }

    @GetMapping("/folder/videos")
    public ApiResponse<PageResult<VideoListItemVO>> folderVideos(
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.folderVideos(AuthContext.requireUserId(), folderId, pageNum, pageSize));
    }

    @PostMapping("/folder")
    public ApiResponse<FolderVO> createFolder(@Valid @RequestBody FolderRequest body) {
        return ApiResponse.ok(service.createFolder(AuthContext.requireUserId(),
                body.getName(), body.getDescription(), body.getVisibility()));
    }

    @PutMapping("/folder/{folderId}")
    public ApiResponse<Void> renameFolder(@PathVariable Long folderId, @Valid @RequestBody FolderRequest body) {
        service.renameFolder(AuthContext.requireUserId(), folderId, body.getName(), body.getDescription());
        return ApiResponse.ok();
    }

    @DeleteMapping("/folder/{folderId}")
    public ApiResponse<Void> deleteFolder(@PathVariable Long folderId) {
        service.deleteFolder(AuthContext.requireUserId(), folderId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/folder/{folderId}/video/{videoId}")
    public ApiResponse<Void> removeFromFolder(@PathVariable Long folderId, @PathVariable Long videoId) {
        service.removeFromFolder(AuthContext.requireUserId(), folderId, videoId);
        return ApiResponse.ok();
    }

    /* ---------------- 待看清单 / 观看记录 ---------------- */

    @GetMapping("/watch-later/list")
    public ApiResponse<Map<String, Object>> watchLater(@RequestParam(required = false) Long cursorId,
                                                       @RequestParam(defaultValue = "20") int size) {
        List<VideoListItemVO> rows = service.watchLater(AuthContext.requireUserId(), cursorId, size);
        return ApiResponse.ok(cursorPage(rows, size));
    }

    @DeleteMapping("/watch-later")
    public ApiResponse<Void> clearWatchLater() {
        service.clearWatchLater(AuthContext.requireUserId());
        return ApiResponse.ok();
    }

    @GetMapping("/history/list")
    public ApiResponse<Map<String, Object>> history(@RequestParam(required = false) Long cursorId,
                                                    @RequestParam(defaultValue = "20") int size) {
        List<VideoListItemVO> rows = service.history(AuthContext.requireUserId(), cursorId, size);
        return ApiResponse.ok(cursorPage(rows, size));
    }

    @DeleteMapping("/history/{videoId}")
    public ApiResponse<Void> deleteHistory(@PathVariable Long videoId) {
        service.deleteHistory(AuthContext.requireUserId(), videoId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/history")
    public ApiResponse<Void> clearHistory() {
        service.clearHistory(AuthContext.requireUserId());
        return ApiResponse.ok();
    }

    /* ---------------- 投稿 ---------------- */

    @GetMapping("/user/{userId}/videos")
    public ApiResponse<Map<String, Object>> userVideos(@PathVariable Long userId,
                                                        @RequestParam(required = false) Long cursorId,
                                                        @RequestParam(defaultValue = "20") int size) {
        List<VideoListItemVO> rows = service.userVideos(userId, AuthContext.currentUserIdOrNull(), cursorId, size);
        return ApiResponse.ok(cursorPage(rows, size));
    }

    /** 游标分页统一出参：多查一条判断还有没有下一页。 */
    private static Map<String, Object> cursorPage(List<VideoListItemVO> rows, int size) {
        boolean hasMore = rows.size() > size;
        List<VideoListItemVO> page = hasMore ? rows.subList(0, size) : rows;
        Map<String, Object> data = new HashMap<>(3);
        data.put("records", page);
        data.put("hasMore", hasMore);
        data.put("cursorId", page.isEmpty() ? null : page.get(page.size() - 1).getId());
        return data;
    }
}