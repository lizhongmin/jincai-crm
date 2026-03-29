package com.jincai.crm.audit.controller;

import com.jincai.crm.audit.entity.ApiAuditLog;
import com.jincai.crm.audit.entity.AuditLog;
import com.jincai.crm.audit.service.ApiAuditLogService;
import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audits")
@Slf4j
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
        log.debug("AuditController.list() called by user: {}, entityType: {}, entityId: {}", SecurityUtils.currentUserId(), entityType, entityId);
        try {
            List<AuditLog> result = auditLogService.list(entityType, entityId);
            log.debug("AuditController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("AuditController.list() failed for user: {}, entityType: {}, entityId: {}", SecurityUtils.currentUserId(), entityType, entityId, e);
            throw e;
        }
    }

    @GetMapping("/api-logs")
    @PreAuthorize("hasAuthority('MENU_SYSTEM_AUDIT')")
    public ApiResponse<com.jincai.crm.common.PageResult<ApiAuditLog>> apiLogsPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        log.debug("AuditController.apiLogsPage() called by user: {}, page: {}, size: {}, keyword: {}, startTime: {}, endTime: {}",
                SecurityUtils.currentUserId(), page, size, keyword, startTime, endTime);
        try {
            com.jincai.crm.common.PageResult<ApiAuditLog> result = apiAuditLogService.page(page, size, keyword, startTime, endTime);
            log.debug("AuditController.apiLogsPage() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("AuditController.apiLogsPage() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }
}
