package com.jincai.crm.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkflowNodeRequest(
    @NotNull Integer stepOrder,
    @NotBlank String nodeName,
    @NotBlank String approverRoleCode
) {
}

