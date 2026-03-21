package com.jincai.crm.workflow.dto;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkflowNodeRequest(
    @NotNull Integer stepOrder,
    @NotBlank String nodeName,
    @NotBlank String approverRoleCode
) {
}

