package com.qingmang.interfaces;

import cn.dev33.satoken.stp.StpUtil;
import com.qingmang.application.WatchTogetherService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.CreateWatchRoomRequest;
import com.qingmang.interfaces.vo.WatchRoomVO;
import com.qingmang.support.AuthContext;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watch-together")
public class WatchTogetherController {

    private final WatchTogetherService watchTogetherService;

    public WatchTogetherController(WatchTogetherService watchTogetherService) {
        this.watchTogetherService = watchTogetherService;
    }

    @PostMapping("/rooms")
    public ApiResponse<WatchRoomVO> createRoom(@RequestBody CreateWatchRoomRequest request,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        return ApiResponse.ok(watchTogetherService.createRoom(currentUserId(token), request));
    }

    @GetMapping("/history")
    public ApiResponse<List<WatchRoomVO>> history(@RequestHeader(value = "Authorization", required = false) String token) {
        return ApiResponse.ok(watchTogetherService.history(currentUserId(token)));
    }

    private Long currentUserId(String token) {
        Long userId = AuthContext.currentUserIdOrNull();
        if (userId != null) return userId;
        Object loginId = token == null ? null : StpUtil.getLoginIdByToken(token);
        if (loginId == null) throw new SecurityException("登录已失效");
        return Long.valueOf(loginId.toString());
    }
}
