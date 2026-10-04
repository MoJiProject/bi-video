package com.qingmang.interfaces;

import com.qingmang.application.AuthService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.LoginRequest;
import com.qingmang.interfaces.dto.RegisterRequest;
import com.qingmang.interfaces.vo.UserProfileVO;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return authService.logout();
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> me() {
        return ApiResponse.ok(authService.currentProfile());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<UserProfileVO> profile(@PathVariable Long userId) {
        return ApiResponse.ok(authService.profile(userId));
    }

    /** 只用于探活，不返回业务数据。 */
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        Long uid = AuthContext.currentUserIdOrNull();
        return ApiResponse.ok(uid == null ? "anonymous" : String.valueOf(uid));
    }
}