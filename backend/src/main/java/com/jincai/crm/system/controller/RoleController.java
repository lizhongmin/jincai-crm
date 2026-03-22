package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.system.dto.RoleGrantRequest;
import com.jincai.crm.system.dto.RoleRequest;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<Set<Long>> permissionIds(@PathVariable Long id) {
        return ApiResponse.ok(roleService.permissionIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_CREATE')")
    public ApiResponse<Role> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_EDIT')")
    public ApiResponse<Role> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/grant")
    @PreAuthorize("hasAuthority('BTN_ORG_ROLE_GRANT')")
    public ApiResponse<Void> grant(@PathVariable Long id, @Valid @RequestBody RoleGrantRequest request) {
        roleService.grant(id, request);
        return ApiResponse.ok(null);
    }
}
