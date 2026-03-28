package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.system.dto.RoleGrantRequest;
import com.jincai.crm.system.dto.RoleRequest;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.service.RoleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/roles")
@Slf4j
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<Role>> list() {
        log.debug("RoleController.list() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<Role> result = roleService.list();
            log.debug("RoleController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("RoleController.list() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<PageResult<Role>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword) {
        log.debug("RoleController.page() called by user: {}, page: {}, size: {}", SecurityUtils.currentUserId(), page, size);
        try {
            PageResult<Role> result = roleService.page(page, size, keyword);
            log.debug("RoleController.page() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("RoleController.page() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Set<String>> permissionIds(@PathVariable String id) {
        log.debug("RoleController.permissionIds() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            Set<String> result = roleService.permissionIds(id);
            log.debug("RoleController.permissionIds() succeeded for user: {}, id: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("RoleController.permissionIds() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_CREATE')")
    public ApiResponse<Role> create(@Valid @RequestBody RoleRequest request) {
        log.info("RoleController.create() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            Role result = roleService.create(request);
            log.info("RoleController.create() succeeded for user: {}, created role ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("RoleController.create() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_EDIT')")
    public ApiResponse<Role> update(@PathVariable String id, @Valid @RequestBody RoleRequest request) {
        log.info("RoleController.update() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            Role result = roleService.update(id, request);
            log.info("RoleController.update() succeeded for user: {}, updated role ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("RoleController.update() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("RoleController.delete() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            roleService.delete(id);
            log.info("RoleController.delete() succeeded for user: {}, deleted role ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("RoleController.delete() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/grant")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_GRANT')")
    public ApiResponse<Void> grant(@PathVariable String id, @Valid @RequestBody RoleGrantRequest request) {
        log.info("RoleController.grant() called by user: {}, roleId: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            roleService.grant(id, request);
            log.info("RoleController.grant() succeeded for user: {}, roleId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("RoleController.grant() failed for user: {}, roleId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
