package com.jincai.crm.workflow.dto;

import com.jincai.crm.workflow.entity.WorkflowTemplate;
import com.jincai.crm.workflow.entity.WorkflowTemplateNode;

import java.util.List;

public record WorkflowTemplateView(
    WorkflowTemplate template,
    List<WorkflowTemplateNode> nodes
) {
}

