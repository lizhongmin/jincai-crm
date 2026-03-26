package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.system.dto.MenuPermissionView;
import com.jincai.crm.system.dto.PermissionTreeGroupView;
import com.jincai.crm.system.dto.PermissionTreeView;
import com.jincai.crm.system.dto.PermissionRequest;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.service.PermissionService;
import com.jincai.crm.security.SecurityUtils;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MenuPermissionView>> menus() {
        return ApiResponse.ok(permissionService.menus(SecurityUtils.currentUser()).stream().map(MenuPermissionView::from).toList());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<Permission>> list() {
        return ApiResponse.ok(permissionService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<PermissionTreeGroupView>> tree() {
        return ApiResponse.ok(permissionService.tree());
    }

    @GetMapping("/tree-view")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<PermissionTreeView>> treeView() {
        return ApiResponse.ok(permissionService.treeView());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Permission> create(@Valid @RequestBody PermissionRequest request) {
        return ApiResponse.ok(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Permission> update(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        return ApiResponse.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.ok(null);
    }
}
