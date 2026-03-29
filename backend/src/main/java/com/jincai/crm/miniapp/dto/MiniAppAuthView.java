package com.jincai.crm.miniapp.dto;

import java.util.List;

public record MiniAppAuthView(
    boolean bound,
    String token,
    String userId,
    String username,
    String fullName,
    List<String> roles
) {

    public static MiniAppAuthView unbound() {
        return new MiniAppAuthView(false, null, null, null, null, List.of());
    }
}
