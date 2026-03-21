package com.jincai.crm.org.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.org.dto.DepartmentRequest;
import com.jincai.crm.org.dto.DepartmentTreeView;
import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/departments")
@PreAuthorize("hasAnyRole('ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<List<Department>> list() {
        return ApiResponse.ok(departmentService.list());
    }

    @GetMapping("/tree")
    public ApiResponse<List<DepartmentTreeView>> tree() {
        return ApiResponse.ok(departmentService.tree());
    }

    @PostMapping
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok(null);
    }
}

