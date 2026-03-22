package com.jincai.crm.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LoginSecurityPolicyRequest(
    @NotNull @Min(1) Integer captchaAfterFailures,
    @NotNull @Min(1) Integer lockAfterFailures,
    @NotNull @Min(1) Integer lockMinutes,
    @NotNull @Min(60) Integer captchaExpireSeconds,
    @NotNull @Min(6) Integer passwordMinLength,
    @NotNull Boolean passwordRequireUppercase,
    @NotNull Boolean passwordRequireLowercase,
    @NotNull Boolean passwordRequireDigit,
    @NotNull Boolean passwordRequireSpecial
) {
}

