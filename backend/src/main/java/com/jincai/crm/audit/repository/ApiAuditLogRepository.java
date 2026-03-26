package com.jincai.crm.audit.repository;

import com.jincai.crm.audit.entity.ApiAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, String> {
}
