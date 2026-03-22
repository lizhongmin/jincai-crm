package com.jincai.crm.system.dto;

public record LoginSecurityPolicyView(
    Integer captchaAfterFailures,
    Integer lockAfterFailures,
    Integer lockMinutes,
    Integer captchaExpireSeconds,
    Integer passwordMinLength,
    Boolean passwordRequireUppercase,
    Boolean passwordRequireLowercase,
    Boolean passwordRequireDigit,
    Boolean passwordRequireSpecial
) {
}

