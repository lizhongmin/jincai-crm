package com.jincai.crm.notification.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.service.NotificationService;
import com.jincai.crm.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> list() {
        log.debug("NotificationController.list() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<Notification> result = notificationService.listCurrentUserNotifications();
            log.debug("NotificationController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("NotificationController.list() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> read(@PathVariable String id) {
        log.info("NotificationController.read() called by user: {}, notificationId: {}", SecurityUtils.currentUserId(), id);
        try {
            notificationService.markRead(id);
            log.info("NotificationController.read() succeeded for user: {}, notificationId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("NotificationController.read() failed for user: {}, notificationId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
