package com.jincai.crm.workflow.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.workflow.dto.WorkflowContextOptionsView;
import com.jincai.crm.workflow.dto.WorkflowTemplateRequest;
import com.jincai.crm.workflow.dto.WorkflowTemplateView;
import com.jincai.crm.workflow.entity.WorkflowTemplate;
import com.jincai.crm.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow/templates")
@Slf4j
public class WorkflowTemplateController {

    private final WorkflowService workflowService;

    public WorkflowTemplateController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<List<WorkflowTemplateView>> list() {
        log.debug("WorkflowTemplateController.list() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<WorkflowTemplateView> result = workflowService.listTemplates();
            log.debug("WorkflowTemplateController.list() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.list() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<PageResult<WorkflowTemplateView>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        log.debug("WorkflowTemplateController.page() called by user: {}, page: {}, size: {}", SecurityUtils.currentUserId(), page, size);
        try {
            PageResult<WorkflowTemplateView> result = workflowService.pageTemplates(page, size, keyword);
            log.debug("WorkflowTemplateController.page() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.page() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/context-options")
    @PreAuthorize("hasAuthority('MENU_WORKFLOW')")
    public ApiResponse<WorkflowContextOptionsView> contextOptions() {
        log.debug("WorkflowTemplateController.contextOptions() called by user: {}", SecurityUtils.currentUserId());
        try {
            WorkflowContextOptionsView result = workflowService.contextOptions();
            log.debug("WorkflowTemplateController.contextOptions() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.contextOptions() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_CREATE')")
    public ApiResponse<WorkflowTemplate> create(@Valid @RequestBody WorkflowTemplateRequest request) {
        log.info("WorkflowTemplateController.create() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            WorkflowTemplate result = workflowService.saveTemplate(request);
            log.info("WorkflowTemplateController.create() succeeded for user: {}, template ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.create() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_EDIT')")
    public ApiResponse<WorkflowTemplate> update(@PathVariable String id, @Valid @RequestBody WorkflowTemplateRequest request) {
        log.info("WorkflowTemplateController.update() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            WorkflowTemplate result = workflowService.updateTemplate(id, request);
            log.info("WorkflowTemplateController.update() succeeded for user: {}, template ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.update() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_WORKFLOW_EDIT')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("WorkflowTemplateController.delete() called by user: {}, template ID: {}", SecurityUtils.currentUserId(), id);
        try {
            workflowService.deleteTemplate(id);
            log.info("WorkflowTemplateController.delete() succeeded for user: {}, template ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("WorkflowTemplateController.delete() failed for user: {}, template ID: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
