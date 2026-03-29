package com.jincai.crm.miniapp.dto;

import jakarta.validation.constraints.NotBlank;

public record MiniAppLoginRequest(
    @NotBlank String code
) {
}
