package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.system.dto.MenuPermissionView;
import com.jincai.crm.system.dto.PermissionRequest;
import com.jincai.crm.system.dto.PermissionTreeGroupView;
import com.jincai.crm.system.dto.PermissionTreeView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.service.PermissionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/menus")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MenuPermissionView>> menus() {
        log.debug("PermissionController.menus() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<MenuPermissionView> result = permissionService.menus(SecurityUtils.currentUser()).stream().map(MenuPermissionView::from).toList();
            log.debug("PermissionController.menus() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.menus() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<Permission>> list() {
        log.debug("PermissionController.list() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<Permission> result = permissionService.list();
            log.debug("PermissionController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.list() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<PermissionTreeGroupView>> tree() {
        log.debug("PermissionController.tree() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<PermissionTreeGroupView> result = permissionService.tree();
            log.debug("PermissionController.tree() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.tree() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/tree-view")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<PermissionTreeView>> treeView() {
        log.debug("PermissionController.treeView() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<PermissionTreeView> result = permissionService.treeView();
            log.debug("PermissionController.treeView() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.treeView() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Permission> create(@Valid @RequestBody PermissionRequest request) {
        log.info("PermissionController.create() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            Permission result = permissionService.create(request);
            log.info("PermissionController.create() succeeded for user: {}, created permission ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.create() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Permission> update(@PathVariable String id, @Valid @RequestBody PermissionRequest request) {
        log.info("PermissionController.update() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            Permission result = permissionService.update(id, request);
            log.info("PermissionController.update() succeeded for user: {}, updated permission ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("PermissionController.update() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("PermissionController.delete() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            permissionService.delete(id);
            log.info("PermissionController.delete() succeeded for user: {}, deleted permission ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("PermissionController.delete() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
