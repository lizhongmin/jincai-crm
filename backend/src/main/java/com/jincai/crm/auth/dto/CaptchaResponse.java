package com.jincai.crm.auth.dto;

public record CaptchaResponse(
    String captchaId,
    String imageBase64,
    long expireSeconds
) {
}

