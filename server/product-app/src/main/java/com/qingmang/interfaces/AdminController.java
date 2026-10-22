package com.qingmang.interfaces;

import com.qingmang.application.AdminManagementService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.AdminCommand;
import com.qingmang.interfaces.dto.AdminQuery;
import com.qingmang.support.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system")
public class AdminController {
    private final AdminManagementService admin;

    public AdminController(AdminManagementService admin) { this.admin = admin; }

    @GetMapping("/overview/getOverview")
    public ApiResponse<?> overview() {
        return ApiResponse.ok(admin.overview(AuthContext.requireUserId()));
    }

    @PostMapping("/user/searchUsers")
    public ApiResponse<?> users(@RequestBody AdminQuery query) { return search("user", query); }

    @GetMapping("/user/searchBanList")
    public ApiResponse<?> bans(@ModelAttribute AdminQuery query) { return search("ban", query); }

    @PostMapping("/content/searchComments")
    public ApiResponse<?> comments(@RequestBody AdminQuery query) { return search("comment", query); }

    @PostMapping("/content/searchDynamics")
    public ApiResponse<?> posts(@RequestBody AdminQuery query) { return search("dynamic", query); }

    @PostMapping("/content/searchMessages")
    public ApiResponse<?> messages(@RequestBody AdminQuery query) { return search("message", query); }

    @PostMapping("/keyWord/searchKeyWords")
    public ApiResponse<?> keywords(@RequestBody AdminQuery query) { return search("keyWord", query); }

    @PostMapping("/log/searchLogs")
    public ApiResponse<?> logs(@RequestBody AdminQuery query) { return search("log", query); }

    @PostMapping("/video/searchVideos")
    public ApiResponse<?> videos(@RequestBody AdminQuery query) { return search("video", query); }

    @GetMapping("/video/getSubZoneKeys")
    public ApiResponse<?> categories() {
        return ApiResponse.ok(admin.categories(AuthContext.requireUserId()));
    }

    @PutMapping("/user/putAdmin")
    public ApiResponse<?> role(@RequestParam Long targetId, @RequestBody AdminCommand command,
                               HttpServletRequest http) {
        return write("user", "putAdmin", targetId, command, null, null, null, http);
    }

    @PostMapping("/user/banUser")
    public ApiResponse<?> ban(@RequestParam Long targetId, @RequestBody AdminCommand command,
                              HttpServletRequest http) {
        return write("user", "banUser", targetId, command, null, null, null, http);
    }

    @PostMapping("/user/unbanUser")
    public ApiResponse<?> unban(@RequestParam Long targetId, @RequestBody AdminCommand command,
                                HttpServletRequest http) {
        return write("user", "unbanUser", targetId, command, null, null, null, http);
    }

    @PostMapping("/content/deleteComment")
    public ApiResponse<?> deleteComments(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("comment", "deleteComment", null, command, null, null, null, http);
    }

    @PostMapping("/content/deleteDynamic")
    public ApiResponse<?> deletePosts(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("dynamic", "deleteDynamic", null, command, null, null, null, http);
    }

    @PostMapping("/content/deleteMessage")
    public ApiResponse<?> deleteMessages(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("message", "deleteMessage", null, command, null, null, null, http);
    }

    @PostMapping("/content/sendPrivateMessage")
    public ApiResponse<?> send(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("message", "sendPrivateMessage", null, command, null, null, null, http);
    }

    @PostMapping("/content/switchCommentOffShelf")
    public ApiResponse<?> hideComment(@RequestParam Integer offShelf, @RequestBody AdminCommand command,
                                      HttpServletRequest http) {
        return write("comment", "switchCommentOffShelf", null, command, null, null, offShelf, http);
    }

    @PostMapping("/content/switchDynamicOffShelf")
    public ApiResponse<?> hidePost(@RequestParam Integer offShelf, @RequestBody AdminCommand command,
                                   HttpServletRequest http) {
        return write("dynamic", "switchDynamicOffShelf", null, command, null, null, offShelf, http);
    }

    @PostMapping("/keyWord/addKeyWord")
    public ApiResponse<?> addKeyword(@RequestParam String word, @RequestBody AdminCommand command,
                                     HttpServletRequest http) {
        return write("keyWord", "addKeyWord", null, command, null, word, null, http);
    }

    @PutMapping("/keyWord/putKeyWord")
    public ApiResponse<?> renameKeyword(@RequestParam String keyWordId, @RequestParam String word,
                                        @RequestBody AdminCommand command, HttpServletRequest http) {
        return write("keyWord", "putKeyWord", null, command, keyWordId, word, null, http);
    }

    @PutMapping("/keyWord/putKeyWordCount")
    public ApiResponse<?> keywordCount(@RequestParam String keyWordId, @RequestParam Long count,
                                       @RequestBody AdminCommand command, HttpServletRequest http) {
        return write("keyWord", "putKeyWordCount", null, command, keyWordId, null, count, http);
    }

    @PostMapping("/keyWord/deleteKeyWord")
    public ApiResponse<?> deleteKeywords(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("keyWord", "deleteKeyWord", null, command, null, null, null, http);
    }

    @PostMapping("/video/deleteVideo")
    public ApiResponse<?> deleteVideos(@RequestBody AdminCommand command, HttpServletRequest http) {
        return write("video", "deleteVideo", null, command, null, null, null, http);
    }

    @PostMapping("/video/examineVideo")
    public ApiResponse<?> approve(@RequestParam Long videoId, @RequestBody AdminCommand command,
                                  HttpServletRequest http) {
        return write("video", "examineVideo", videoId, command, null, null, null, http);
    }

    @PostMapping("/video/rejectVideo")
    public ApiResponse<?> reject(@RequestParam Long videoId, @RequestBody AdminCommand command,
                                 HttpServletRequest http) {
        return write("video", "rejectVideo", videoId, command, null, null, null, http);
    }

    @PostMapping("/video/takeDownVideo")
    public ApiResponse<?> takeDown(@RequestParam Long videoId, @RequestBody AdminCommand command,
                                   HttpServletRequest http) {
        return write("video", "takeDownVideo", videoId, command, null, null, null, http);
    }

    @PutMapping("/video/restoreVideo")
    public ApiResponse<?> restore(@RequestParam Long videoId, @RequestBody AdminCommand command,
                                  HttpServletRequest http) {
        return write("video", "restoreVideo", videoId, command, null, null, null, http);
    }

    private ApiResponse<?> search(String module, AdminQuery query) {
        return ApiResponse.ok(admin.search(AuthContext.requireUserId(), module, query));
    }

    private ApiResponse<?> write(String module, String action, Long target, AdminCommand command,
                                  String keyword, String word, Number value, HttpServletRequest http) {
        return ApiResponse.ok(admin.write(AuthContext.currentUserIdOrNull(), module, action, target, command,
                keyword, word, value, http.getRemoteAddr()));
    }
}
