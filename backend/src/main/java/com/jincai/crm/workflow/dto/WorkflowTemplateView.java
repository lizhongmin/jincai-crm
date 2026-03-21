package com.jincai.crm.workflow.dto;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import java.util.List;

public record WorkflowTemplateView(
    WorkflowTemplate template,
    List<WorkflowTemplateNode> nodes
) {
}

