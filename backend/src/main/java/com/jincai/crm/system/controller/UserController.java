package com.jincai.crm.system.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.system.dto.AppUserView;
import com.jincai.crm.system.dto.ResetPasswordRequest;
import com.jincai.crm.system.dto.UserStatusRequest;
import com.jincai.crm.system.dto.UserUpsertRequest;
import com.jincai.crm.system.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<List<AppUserView>> list() {
        log.debug("UserController.list() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<AppUserView> result = userService.list();
            log.debug("UserController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("UserController.list() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORG')")
    public ApiResponse<PageResult<AppUserView>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String departmentId,
                                                     @RequestParam(required = false) String roleId) {
        log.debug("UserController.page() called by user: {}, page: {}, size: {}", SecurityUtils.currentUserId(), page, size);
        try {
            PageResult<AppUserView> result = userService.page(page, size, keyword, departmentId, roleId);
            log.debug("UserController.page() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("UserController.page() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORG_USER_CREATE')")
    public ApiResponse<AppUserView> create(@Valid @RequestBody UserUpsertRequest request) {
        log.info("UserController.create() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            AppUserView result = userService.create(request);
            log.info("UserController.create() succeeded for user: {}, created user ID: {}", SecurityUtils.currentUserId(), result.id());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("UserController.create() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_EDIT')")
    public ApiResponse<AppUserView> update(@PathVariable String id, @Valid @RequestBody UserUpsertRequest request) {
        log.info("UserController.update() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            AppUserView result = userService.update(id, request);
            log.info("UserController.update() succeeded for user: {}, updated user ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("UserController.update() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_STATUS')")
    public ApiResponse<AppUserView> changeStatus(@PathVariable String id, @Valid @RequestBody UserStatusRequest request) {
        log.info("UserController.changeStatus() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            AppUserView result = userService.changeStatus(id, request);
            log.info("UserController.changeStatus() succeeded for user: {}, updated user ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("UserController.changeStatus() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_RESET_PASSWORD')")
    public ApiResponse<Void> resetPassword(@PathVariable String id, @RequestBody(required = false) ResetPasswordRequest request) {
        log.info("UserController.resetPassword() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            userService.resetPassword(id, request);
            log.info("UserController.resetPassword() succeeded for user: {}, reset user ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("UserController.resetPassword() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORG_USER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("UserController.delete() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            userService.delete(id);
            log.info("UserController.delete() succeeded for user: {}, deleted user ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("UserController.delete() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
