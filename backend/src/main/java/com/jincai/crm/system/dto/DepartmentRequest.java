package com.jincai.crm.system.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
    @NotBlank String name,
    String parentId,
    String leaderUserId
) {
}

