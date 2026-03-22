package com.jincai.crm.auth.controller;

import com.jincai.crm.auth.dto.*;
import com.jincai.crm.auth.service.*;

import com.jincai.crm.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(authService.me());
    }

    @GetMapping("/login-state")
    public ApiResponse<LoginStateResponse> loginState(@RequestParam(required = false) String username) {
        return ApiResponse.ok(authService.loginState(username));
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha(@RequestParam String username) {
        return ApiResponse.ok(authService.captcha(username));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok("common.security.password.changed", null);
    }
}
