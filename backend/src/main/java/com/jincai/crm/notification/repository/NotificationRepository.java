package com.jincai.crm.notification.repository;

import com.jincai.crm.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);
}

