package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.system.dto.DepartmentRequest;
import com.jincai.crm.system.dto.DepartmentTreeView;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        log.debug("获取部门列表");
        return ApiResponse.ok(departmentService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<DepartmentTreeView>> tree() {
        log.debug("获取部门树");
        return ApiResponse.ok(departmentService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_CREATE')")
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentRequest request) {
        log.info("创建部门 - 名称: {}, 父部门ID: {}", request.name(), request.parentId());
        return ApiResponse.ok(departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_EDIT')")
    public ApiResponse<Department> update(@PathVariable String id, @Valid @RequestBody DepartmentRequest request) {
        log.info("更新部门 - ID: {}, 名称: {}", id, request.name());
        return ApiResponse.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_DEPARTMENT_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除部门 - ID: {}", id);
        departmentService.delete(id);
        return ApiResponse.ok(null);
    }
}
