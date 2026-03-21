package com.jincai.crm.notification.repository;

import com.jincai.crm.notification.controller.*;
import com.jincai.crm.notification.entity.*;
import com.jincai.crm.notification.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);
}

