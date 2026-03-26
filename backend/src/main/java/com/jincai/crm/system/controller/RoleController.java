package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.system.dto.RoleGrantRequest;
import com.jincai.crm.system.dto.RoleRequest;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<Role>> list() {
        return ApiResponse.ok(roleService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<PageResult<Role>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(roleService.page(page, size, keyword));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Set<String>> permissionIds(@PathVariable String id) {
        return ApiResponse.ok(roleService.permissionIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_CREATE')")
    public ApiResponse<Role> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_EDIT')")
    public ApiResponse<Role> update(@PathVariable String id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/grant")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_GRANT')")
    public ApiResponse<Void> grant(@PathVariable String id, @Valid @RequestBody RoleGrantRequest request) {
        roleService.grant(id, request);
        return ApiResponse.ok(null);
    }
}
