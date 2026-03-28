package com.jincai.crm.audit.service;

import com.jincai.crm.audit.entity.AuditLog;
import com.jincai.crm.audit.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public List<AuditLog> list(String entityType, String entityId) {
        return repository.findByEntityTypeAndEntityIdAndDeletedFalseOrderByCreatedAtDesc(entityType, entityId);
    }

    public void logDiff(String entityType, String entityId, Map<String, Object> before, Map<String, Object> after, String sourceIp) {
        log.debug("记录审计日志 - 实体类型: {}, 实体ID: {}, 源IP: {}", entityType, entityId, sourceIp);
        try {
            int changeCount = 0;
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
                    changeCount++;
                }
            }
            log.debug("审计日志记录完成 - 实体类型: {}, 实体ID: {}, 变更字段数: {}", entityType, entityId, changeCount);
        } catch (Exception e) {
            log.error("记录审计日志失败 - 实体类型: {}, 实体ID: {}", entityType, entityId, e);
            throw e;
        }
    }

    private String value(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}

