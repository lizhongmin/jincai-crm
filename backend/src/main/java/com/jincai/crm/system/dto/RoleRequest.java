package com.jincai.crm.system.dto;

import com.jincai.crm.common.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(
    @NotBlank String code,
    @NotBlank String name,
    String description,
    @NotNull DataScope dataScope
) {
}

