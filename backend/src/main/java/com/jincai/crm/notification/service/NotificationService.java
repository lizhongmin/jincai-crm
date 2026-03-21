package com.jincai.crm.notification.service;

import com.jincai.crm.notification.controller.*;
import com.jincai.crm.notification.entity.*;
import com.jincai.crm.notification.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.security.SecurityUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> listCurrentUserNotifications() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            return List.of();
        }
        return repository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markRead(Long id) {
        Notification notification = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Notification not found"));
        notification.setReadFlag(true);
        repository.save(notification);
    }
}
