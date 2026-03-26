package com.jincai.crm.audit.controller;

import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.entity.AuditLog;
import com.jincai.crm.audit.service.ApiAuditLogService;
import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audits")
public class AuditController {

    private final AuditLogService auditLogService;
    private final ApiAuditLogService apiAuditLogService;

    public AuditController(AuditLogService auditLogService, ApiAuditLogService apiAuditLogService) {
        this.auditLogService = auditLogService;
        this.apiAuditLogService = apiAuditLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BTN_AUDIT_VIEW')")
    public ApiResponse<List<AuditLog>> list(@RequestParam("entityType") String entityType,
                                            @RequestParam("entityId") String entityId) {
        return ApiResponse.ok(auditLogService.list(entityType, entityId));
    }

    @GetMapping("/api-logs")
    @PreAuthorize("hasAuthority('MENU_SYSTEM_AUDIT')")
    public ApiResponse<com.jincai.crm.common.PageResult<ApiAuditLog>> apiLogsPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.ok(apiAuditLogService.page(page, size));
    }
}
