package com.jincai.crm.notification.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.repository.NotificationRepository;
import com.jincai.crm.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> listCurrentUserNotifications() {
        String userId = SecurityUtils.currentUserId();
        if (userId == null) {
            return List.of();
        }
        return repository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markRead(String id) {
        Notification notification = repository.findById(id)
            .orElseThrow(() -> new BusinessException("error.notification.notFound"));
        notification.setReadFlag(true);
        repository.save(notification);
    }
}

