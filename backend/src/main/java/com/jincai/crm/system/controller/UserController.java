package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.system.dto.AppUserView;
import com.jincai.crm.system.dto.ResetPasswordRequest;
import com.jincai.crm.system.dto.UserStatusRequest;
import com.jincai.crm.system.dto.UserUpsertRequest;
import com.jincai.crm.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<AppUserView>> list() {
        return ApiResponse.ok(userService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<PageResult<AppUserView>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String departmentId,
                                                     @RequestParam(required = false) String roleId) {
        return ApiResponse.ok(userService.page(page, size, keyword, departmentId, roleId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_USER_CREATE')")
    public ApiResponse<AppUserView> create(@Valid @RequestBody UserUpsertRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_EDIT')")
    public ApiResponse<AppUserView> update(@PathVariable String id, @Valid @RequestBody UserUpsertRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_STATUS')")
    public ApiResponse<AppUserView> changeStatus(@PathVariable String id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.ok(userService.changeStatus(id, request));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_RESET_PASSWORD')")
    public ApiResponse<Void> resetPassword(@PathVariable String id, @RequestBody(required = false) ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.ok(null);
    }
}
