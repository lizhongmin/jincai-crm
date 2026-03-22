package com.jincai.crm.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderActionRequest(
    @NotNull OrderActionType action,
    String comment,
    String targetRoleCode
) {
}

