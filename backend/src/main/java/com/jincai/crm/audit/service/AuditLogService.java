package com.jincai.crm.audit.service;

import com.jincai.crm.audit.controller.*;
import com.jincai.crm.audit.entity.*;
import com.jincai.crm.audit.repository.*;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public List<AuditLog> list(String entityType, Long entityId) {
        return repository.findByEntityTypeAndEntityIdAndDeletedFalseOrderByCreatedAtDesc(entityType, entityId);
    }

    public void logDiff(String entityType, Long entityId, Map<String, Object> before, Map<String, Object> after, String sourceIp) {
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            String key = entry.getKey();
            String beforeValue = value(before.get(key));
            String afterValue = value(entry.getValue());
            if (!java.util.Objects.equals(beforeValue, afterValue)) {
                AuditLog log = new AuditLog();
                log.setEntityType(entityType);
                log.setEntityId(entityId);
                log.setFieldName(key);
                log.setBeforeValue(beforeValue);
                log.setAfterValue(afterValue);
                log.setSourceIp(sourceIp);
                repository.save(log);
            }
        }
    }

    private String value(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}

