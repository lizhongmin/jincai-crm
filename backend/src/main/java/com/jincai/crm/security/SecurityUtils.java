package com.jincai.crm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    public static String currentUserId() {
        LoginUser user = currentUser();
        if (user == null || user.getUserId() == null) {
            return null;
        }
        try {
            return user.getUserId();
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

