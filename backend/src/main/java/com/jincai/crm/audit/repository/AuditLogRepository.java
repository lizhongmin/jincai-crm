package com.jincai.crm.audit.repository;

import com.jincai.crm.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByEntityTypeAndEntityIdAndDeletedFalseOrderByCreatedAtDesc(String entityType, String entityId);
}

