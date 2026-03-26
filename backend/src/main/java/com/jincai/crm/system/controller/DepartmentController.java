package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.system.dto.DepartmentRequest;
import com.jincai.crm.system.dto.DepartmentTreeView;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<Department>> list() {
        return ApiResponse.ok(departmentService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<DepartmentTreeView>> tree() {
        return ApiResponse.ok(departmentService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_CREATE')")
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_EDIT')")
    public ApiResponse<Department> update(@PathVariable String id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ApiResponse.ok(null);
    }
}
