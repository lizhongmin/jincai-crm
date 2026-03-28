package com.jincai.crm.audit.service;

import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.repository.ApiAuditLogRepository;
import com.jincai.crm.common.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApiAuditLogService {

    private final ApiAuditLogRepository repository;

    public ApiAuditLogService(ApiAuditLogRepository repository) {
        this.repository = repository;
    }

    public PageResult<ApiAuditLog> page(int page, int size) {
        log.debug("分页查询API审计日志 - 页码: {}, 大小: {}", page, size);
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ApiAuditLog> pageData = repository.findAll(pageable);
            log.debug("API审计日志查询完成 - 总记录: {}", pageData.getTotalElements());
            return new PageResult<>(pageData.getContent(), pageData.getTotalElements(), page, size);
        } catch (Exception e) {
            log.error("查询API审计日志失败 - 页码: {}, 大小: {}", page, size, e);
            throw e;
        }
    }
}
