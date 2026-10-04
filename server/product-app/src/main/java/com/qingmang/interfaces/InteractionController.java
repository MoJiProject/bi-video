package com.qingmang.interfaces;

import com.qingmang.application.VideoInteractionService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class InteractionController {

    private final VideoInteractionService interaction;

    public InteractionController(VideoInteractionService interaction) {
        this.interaction = interaction;
    }

    @PostMapping("/{videoId}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long videoId) {
        return ApiResponse.ok(interaction.toggleLike(AuthContext.requireUserId(), videoId));
    }

    @PostMapping("/{videoId}/coin")
    public ApiResponse<Map<String, Object>> coin(@PathVariable Long videoId,
                                                 @RequestBody(required = false) CoinBody body) {
        int n = body == null ? 1 : body.getCoinCount();
        return ApiResponse.ok(interaction.throwCoin(AuthContext.requireUserId(), videoId, n));
    }

    @PostMapping("/{videoId}/favorite")
    public ApiResponse<Map<String, Object>> favorite(@PathVariable Long videoId,
                                                     @RequestParam(required = false) Long folderId) {
        return ApiResponse.ok(interaction.toggleFavorite(AuthContext.requireUserId(), videoId, folderId));
    }

    @PostMapping("/{videoId}/watch-later")
    public ApiResponse<Map<String, Object>> watchLater(@PathVariable Long videoId) {
        return ApiResponse.ok(interaction.toggleWatchLater(AuthContext.requireUserId(), videoId));
    }

    @PostMapping("/{videoId}/watch")
    public ApiResponse<Map<String, Object>> watch(@PathVariable Long videoId,
                                                  @RequestParam(defaultValue = "0") int progressSeconds) {
        return ApiResponse.ok(interaction.reportWatch(AuthContext.requireUserId(), videoId, progressSeconds));
    }

    /** 一次拿全互动状态，避免前端为每个按钮各发一次请求。 */
    @GetMapping("/{videoId}/state")
    public ApiResponse<Map<String, Object>> state(@PathVariable Long videoId) {
        return ApiResponse.ok(interaction.stateOf(AuthContext.currentUserIdOrNull(), videoId));
    }

    @Data
    public static class CoinBody {

        @Min(1)
        @Max(2)
        private int coinCount = 1;
    }
}