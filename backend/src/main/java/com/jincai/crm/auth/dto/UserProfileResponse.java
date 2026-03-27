package com.jincai.crm.auth.dto;

import com.jincai.crm.common.DataScope;
import java.util.List;

public record UserProfileResponse(
    String userId,
    String username,
    String departmentId,
    DataScope dataScope,
    List<String> roleIds,
    List<String> roles
) {
}
