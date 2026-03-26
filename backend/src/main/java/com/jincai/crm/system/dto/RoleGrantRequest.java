package com.jincai.crm.system.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RoleGrantRequest(
    @NotEmpty List<String> permissionIds
) {
}

