package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.system.dto.LoginSecurityPolicyRequest;
import com.jincai.crm.system.dto.LoginSecurityPolicyView;
import com.jincai.crm.system.service.LoginSecurityPolicyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security")
@Slf4j
public class LoginSecurityPolicyController {

    private final LoginSecurityPolicyService service;

    public LoginSecurityPolicyController(LoginSecurityPolicyService service) {
        this.service = service;
    }

    @GetMapping("/login-policy")
    @PreAuthorize("hasAuthority('MENU_SECURITY')")
    public ApiResponse<LoginSecurityPolicyView> getPolicy() {
        log.debug("LoginSecurityPolicyController.getPolicy() called by user: {}", SecurityUtils.currentUserId());
        try {
            LoginSecurityPolicyView result = service.getPolicy();
            log.debug("LoginSecurityPolicyController.getPolicy() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("LoginSecurityPolicyController.getPolicy() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/login-policy")
    @PreAuthorize("hasAuthority('BTN_SECURITY_POLICY_EDIT')")
    public ApiResponse<LoginSecurityPolicyView> updatePolicy(@Valid @RequestBody LoginSecurityPolicyRequest request) {
        log.info("LoginSecurityPolicyController.updatePolicy() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            LoginSecurityPolicyView result = service.updatePolicy(request);
            log.info("LoginSecurityPolicyController.updatePolicy() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("LoginSecurityPolicyController.updatePolicy() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }
}
