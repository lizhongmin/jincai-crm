package com.jincai.crm.audit.controller;

import com.jincai.crm.audit.entity.*;
import com.jincai.crm.audit.repository.*;
import com.jincai.crm.audit.service.*;

import com.jincai.crm.common.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audits")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','FINANCE')")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLog>> list(@RequestParam("entityType") String entityType,
                                            @RequestParam("entityId") Long entityId) {
        return ApiResponse.ok(auditLogService.list(entityType, entityId));
    }
}
