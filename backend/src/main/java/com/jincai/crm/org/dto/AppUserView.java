package com.jincai.crm.org.dto;

import com.jincai.crm.common.DataScope;
import java.time.LocalDateTime;
import java.util.List;

public record AppUserView(
    Long id,
    String username,
    String fullName,
    String phone,
    String employeeNo,
    String email,
    String gender,
    String title,
    String hireDate,
    String emergencyPhone,
    Long departmentId,
    String departmentName,
    String departmentPath,
    DataScope dataScope,
    Boolean enabled,
    List<Long> roleIds,
    List<String> roleNames,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

