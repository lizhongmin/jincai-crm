package com.jincai.crm.workflow.controller;

import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import com.jincai.crm.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow/templates")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
public class WorkflowTemplateController {

    private final WorkflowService workflowService;

    public WorkflowTemplateController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowTemplateView>> list() {
        return ApiResponse.ok(workflowService.listTemplates());
    }

    @PostMapping
    public ApiResponse<WorkflowTemplate> create(@Valid @RequestBody WorkflowTemplateRequest request) {
        return ApiResponse.ok(workflowService.saveTemplate(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowTemplate> update(@PathVariable Long id, @Valid @RequestBody WorkflowTemplateRequest request) {
        return ApiResponse.ok(workflowService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workflowService.deleteTemplate(id);
        return ApiResponse.ok(null);
    }
}
