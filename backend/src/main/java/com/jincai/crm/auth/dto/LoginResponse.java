package com.jincai.crm.auth.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String userId,
    String username,
    String fullName,
    List<String> roles
) {
}
