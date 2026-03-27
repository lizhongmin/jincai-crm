package com.jincai.crm.system.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String type,   // MENU | BUTTON
    String menuPath,
    String parentId,
    Integer sortOrder
) {}
