package com.jincai.crm.workflow.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.workflow.dto.WorkflowContextOptionsView;
import com.jincai.crm.workflow.dto.WorkflowTemplateRequest;
import com.jincai.crm.workflow.dto.WorkflowTemplateView;
import com.jincai.crm.workflow.entity.WorkflowTemplate;
import com.jincai.crm.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow/templates")
public class WorkflowTemplateController {

    private final WorkflowService workflowService;

    public WorkflowTemplateController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<List<WorkflowTemplateView>> list() {
        return ApiResponse.ok(workflowService.listTemplates());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<PageResult<WorkflowTemplateView>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(workflowService.pageTemplates(page, size, keyword));
    }

    @GetMapping("/context-options")
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<WorkflowContextOptionsView> contextOptions() {
        return ApiResponse.ok(workflowService.contextOptions());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_CREATE')")
    public ApiResponse<WorkflowTemplate> create(@Valid @RequestBody WorkflowTemplateRequest request) {
        return ApiResponse.ok(workflowService.saveTemplate(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_EDIT')")
    public ApiResponse<WorkflowTemplate> update(@PathVariable String id, @Valid @RequestBody WorkflowTemplateRequest request) {
        return ApiResponse.ok(workflowService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_EDIT')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        workflowService.deleteTemplate(id);
        return ApiResponse.ok(null);
    }
}
