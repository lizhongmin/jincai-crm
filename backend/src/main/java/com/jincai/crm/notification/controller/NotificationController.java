package com.jincai.crm.notification.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> list() {
        return ApiResponse.ok(notificationService.listCurrentUserNotifications());
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> read(@PathVariable String id) {
        notificationService.markRead(id);
        return ApiResponse.ok(null);
    }
}
