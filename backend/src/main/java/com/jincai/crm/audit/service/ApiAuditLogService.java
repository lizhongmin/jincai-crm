package com.jincai.crm.audit.service;

import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.repository.ApiAuditLogRepository;
import com.jincai.crm.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ApiAuditLogService {

    private final ApiAuditLogRepository repository;

    public ApiAuditLogService(ApiAuditLogRepository repository) {
        this.repository = repository;
    }

    public PageResult<ApiAuditLog> page(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApiAuditLog> pageData = repository.findAll(pageable);
        return new PageResult<>(pageData.getContent(), pageData.getTotalElements(), page, size);
    }
}
