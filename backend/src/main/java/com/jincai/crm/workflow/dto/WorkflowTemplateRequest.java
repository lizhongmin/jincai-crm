package com.jincai.crm.workflow.dto;

import com.jincai.crm.workflow.controller.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;
import com.jincai.crm.workflow.service.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record WorkflowTemplateRequest(
    @NotBlank String name,
    @NotBlank String orderType,
    @NotBlank String productCategory,
    Long routeId,
    Long departureId,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    Boolean active,
    @Valid List<WorkflowNodeRequest> nodes
) {
}
