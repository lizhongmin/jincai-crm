package com.jincai.crm.org.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.org.dto.AppUserView;
import com.jincai.crm.org.dto.ResetPasswordRequest;
import com.jincai.crm.org.dto.UserStatusRequest;
import com.jincai.crm.org.dto.UserUpsertRequest;
import com.jincai.crm.org.service.UserService;
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
@RequestMapping("/users")
@PreAuthorize("hasAnyRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<AppUserView>> list() {
        return ApiResponse.ok(userService.list());
    }

    @PostMapping
    public ApiResponse<AppUserView> create(@Valid @RequestBody UserUpsertRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AppUserView> update(@PathVariable Long id, @Valid @RequestBody UserUpsertRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AppUserView> changeStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.ok(userService.changeStatus(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok(null);
    }
}

