package com.jincai.crm.auth.dto;

import com.jincai.crm.auth.controller.*;
import com.jincai.crm.auth.service.*;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {
}

