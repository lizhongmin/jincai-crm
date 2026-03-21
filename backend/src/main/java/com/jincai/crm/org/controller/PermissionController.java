package com.jincai.crm.org.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.org.dto.PermissionTreeGroupView;
import com.jincai.crm.org.entity.Permission;
import com.jincai.crm.org.service.PermissionService;
import com.jincai.crm.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/menus")
    public ApiResponse<List<Permission>> menus() {
        return ApiResponse.ok(permissionService.menus(SecurityUtils.currentUser()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<Permission>> list() {
        return ApiResponse.ok(permissionService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<PermissionTreeGroupView>> tree() {
        return ApiResponse.ok(permissionService.tree());
    }
}

