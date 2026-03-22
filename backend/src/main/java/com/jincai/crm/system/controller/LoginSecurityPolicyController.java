package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.system.dto.LoginSecurityPolicyRequest;
import com.jincai.crm.system.dto.LoginSecurityPolicyView;
import com.jincai.crm.system.service.LoginSecurityPolicyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
public class LoginSecurityPolicyController {

    private final LoginSecurityPolicyService service;

    public LoginSecurityPolicyController(LoginSecurityPolicyService service) {
        this.service = service;
    }

    @GetMapping("/login-policy")
    @PreAuthorize("hasAuthority('MENU_SECURITY')")
    public ApiResponse<LoginSecurityPolicyView> getPolicy() {
        return ApiResponse.ok(service.getPolicy());
    }

    @PutMapping("/login-policy")
    @PreAuthorize("hasAuthority('BTN_SECURITY_POLICY_EDIT')")
    public ApiResponse<LoginSecurityPolicyView> updatePolicy(@Valid @RequestBody LoginSecurityPolicyRequest request) {
        return ApiResponse.ok(service.updatePolicy(request));
    }
}
