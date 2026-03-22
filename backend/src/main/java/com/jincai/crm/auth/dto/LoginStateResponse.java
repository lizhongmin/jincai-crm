package com.jincai.crm.auth.dto;

public record LoginStateResponse(
    boolean captchaRequired,
    boolean locked,
    long lockSeconds
) {
}

