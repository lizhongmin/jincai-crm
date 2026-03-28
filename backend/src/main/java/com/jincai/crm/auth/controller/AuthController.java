package com.jincai.crm.auth.controller;

import com.jincai.crm.auth.dto.*;
import com.jincai.crm.auth.service.AuthService;
import com.jincai.crm.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求 - 用户名: {}", request.username());
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.ok(authService.me());
    }

    @GetMapping("/login-state")
    public ApiResponse<LoginStateResponse> loginState(@RequestParam(required = false) String username) {
        return ApiResponse.ok(authService.loginState(username));
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha(@RequestParam String username) {
        log.debug("获取验证码 - 用户名: {}", username);
        return ApiResponse.ok(authService.captcha(username));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求");
        authService.changePassword(request);
        return ApiResponse.ok("common.security.password.changed", null);
    }
}
