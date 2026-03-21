package com.jincai.crm.auth.dto;

import com.jincai.crm.auth.controller.*;
import com.jincai.crm.auth.service.*;

import java.util.List;

public record LoginResponse(
    String token,
    Long userId,
    String username,
    String fullName,
    List<String> roles
) {
}

