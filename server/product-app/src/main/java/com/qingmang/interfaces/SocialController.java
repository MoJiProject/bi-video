package com.qingmang.interfaces;

import com.qingmang.application.SocialService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.interfaces.dto.PostRequest;
import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
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
public class SocialController {

    private final SocialService social;

    public SocialController(SocialService social) {
        this.social = social;
    }

    @PostMapping("/user/{userId}/follow")
    public ApiResponse<Map<String, Object>> follow(@PathVariable Long userId) {
        Long me = AuthContext.requireUserId();
        boolean following = social.toggleFollow(me, userId);
        Map<String, Object> data = new HashMap<>(2);
        data.put("userId", userId);
        data.put("following", following);
        return ApiResponse.ok(data);
    }

    @PutMapping("/user/{userId}/follow")
    public ApiResponse<Void> addFollow(@PathVariable Long userId) {
        social.setFollowing(AuthContext.requireUserId(), userId, true);
        return ApiResponse.ok();
    }

    @DeleteMapping("/user/{userId}/follow")
    public ApiResponse<Void> removeFollow(@PathVariable Long userId) {
        social.setFollowing(AuthContext.requireUserId(), userId, false);
        return ApiResponse.ok();
    }

    /** targetUserId 为空看公开动态流。 */
    @GetMapping("/post/list")
    public ApiResponse<Map<String, Object>> posts(@RequestParam(required = false) Long targetUserId,
                                                 @RequestParam(required = false) Long cursorId,
                                                 @RequestParam(defaultValue = "20") int size) {
        Long me = AuthContext.requireUserId();
        List<PostVO> rows = social.posts(me, targetUserId, cursorId, size);
        return ApiResponse.ok(cursorPage(rows, size));
    }

    @PostMapping("/post")
    public ApiResponse<Map<String, Object>> post(@Valid @RequestBody PostRequest body) {
        Long id = social.post(AuthContext.requireUserId(), body);
        return ApiResponse.ok(Map.of("postId", id));
    }

    @DeleteMapping("/post/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        social.deletePost(AuthContext.requireUserId(), postId, false);
        return ApiResponse.ok();
    }

    /** type=follow 我关注的人；type=fans 关注我的人。 */
    @GetMapping("/relation/list")
    public ApiResponse<Map<String, Object>> relations(@RequestParam(defaultValue = "follow") String type,
                                                     @RequestParam(required = false) Long userId,
                                                     @RequestParam(required = false) Long cursorId,
                                                     @RequestParam(defaultValue = "20") int size) {
        Long me = AuthContext.requireUserId();
        Long other = "fans".equalsIgnoreCase(type) ? userId : null;
        List<UserBriefVO> rows = social.follows(me, other, cursorId, size);
        return ApiResponse.ok(cursorPage(rows, size));
    }

    @GetMapping("/privateMessage/selectNotReadNumber")
    public ApiResponse<Integer> privateMessageUnreadCount(@RequestParam Long userId) {
        Long me = AuthContext.requireUserId();
        if (!me.equals(userId)) {
            throw new BizException(ErrorCode.NO_PERMISSION);
        }
        return ApiResponse.ok(social.unreadPrivateMessageCount(userId));
    }

    private static Map<String, Object> cursorPage(List<?> rows, int size) {
        boolean hasMore = rows.size() > size;
        List<?> page = hasMore ? rows.subList(0, size) : rows;
        Map<String, Object> data = new HashMap<>(2);
        data.put("records", page);
        data.put("hasMore", hasMore);
        return data;
    }
}