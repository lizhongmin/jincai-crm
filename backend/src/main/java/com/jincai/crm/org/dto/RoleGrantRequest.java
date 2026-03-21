package com.jincai.crm.org.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RoleGrantRequest(
    @NotEmpty List<Long> permissionIds
) {
}

