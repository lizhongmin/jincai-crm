package com.jincai.crm.audit.repository;

import com.jincai.crm.audit.controller.*;
import com.jincai.crm.audit.entity.*;
import com.jincai.crm.audit.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdAndDeletedFalseOrderByCreatedAtDesc(String entityType, Long entityId);
}

