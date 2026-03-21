package com.jincai.crm.notification.controller;

import com.jincai.crm.notification.entity.*;
import com.jincai.crm.notification.repository.*;
import com.jincai.crm.notification.service.*;

import com.jincai.crm.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<Notification>> list() {
        return ApiResponse.ok(notificationService.listCurrentUserNotifications());
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok(null);
    }
}

