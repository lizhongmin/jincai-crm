package com.jincai.crm.miniapp.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.miniapp.dto.MiniAppAuthView;
import com.jincai.crm.miniapp.dto.MiniAppBindRequest;
import com.jincai.crm.miniapp.dto.MiniAppLoginRequest;
import com.jincai.crm.miniapp.service.MiniAppAuthService;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/miniapp/auth")
public class MiniAppAuthController {

    private final MiniAppAuthService miniAppAuthService;

    public MiniAppAuthController(MiniAppAuthService miniAppAuthService) {
        this.miniAppAuthService = miniAppAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<MiniAppAuthView> login(@Valid @RequestBody MiniAppLoginRequest request) {
        return ApiResponse.ok(miniAppAuthService.login(request));
    }

    @PostMapping("/bind")
    public ApiResponse<MiniAppAuthView> bind(@Valid @RequestBody MiniAppBindRequest request) {
        return ApiResponse.ok(miniAppAuthService.bind(request));
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MiniAppAuthView> status() {
        return ApiResponse.ok(miniAppAuthService.status(SecurityUtils.currentUser()));
    }
}
