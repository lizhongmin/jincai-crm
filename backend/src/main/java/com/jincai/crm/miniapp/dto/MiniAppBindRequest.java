package com.jincai.crm.miniapp.dto;

import jakarta.validation.constraints.NotBlank;

public record MiniAppBindRequest(
    @NotBlank String code,
    @NotBlank String username,
    @NotBlank String password
) {
}
