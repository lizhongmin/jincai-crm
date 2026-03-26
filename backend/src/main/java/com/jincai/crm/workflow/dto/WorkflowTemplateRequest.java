package com.jincai.crm.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public record WorkflowTemplateRequest(
    @NotBlank String name,
    @NotBlank String orderType,
    @NotBlank String productCategory,
    String routeId,
    String departureId,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    Boolean active,
    @Valid List<WorkflowNodeRequest> nodes
) {
}
