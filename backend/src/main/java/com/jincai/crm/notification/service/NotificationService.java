package com.jincai.crm.notification.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.repository.NotificationRepository;
import com.jincai.crm.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
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
        log.debug("标记通知为已读 - 通知ID: {}", id);
        try {
            Notification notification = repository.findById(id)
                .orElseThrow(() -> new BusinessException("error.notification.notFound"));
            notification.setReadFlag(true);
            repository.save(notification);
            log.debug("通知标记为已读成功 - 通知ID: {}", id);
        } catch (Exception e) {
            log.error("标记通知为已读失败 - 通知ID: {}", id, e);
            throw e;
        }
    }
}

