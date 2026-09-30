package com.moji.watch;

import cn.dev33.satoken.stp.StpUtil;
import com.moji.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/watch-together")
public class WatchRoomController {

    private final WatchRoomService watchRoomService;

    public WatchRoomController(WatchRoomService watchRoomService) {
        this.watchRoomService = watchRoomService;
    }

    @PostMapping("/rooms")
    public R<WatchRoom> createRoom(@RequestBody CreateWatchRoomRequest request,
                                   @RequestHeader("Authorization") String token) {
        return R.success(watchRoomService.createRoom(getUserId(token), request));
    }

    @GetMapping("/history")
    public R<List<WatchRoom>> history(@RequestHeader("Authorization") String token) {
        return R.success(watchRoomService.history(getUserId(token)));
    }

    private Integer getUserId(String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) throw new SecurityException("登录已失效");
        return Integer.valueOf(loginId.toString());
    }
}
