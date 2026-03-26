package com.jincai.crm.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AppUserView(
    String id,
    String username,
    String fullName,
    String phone,
    String employeeNo,
    String email,
    String gender,
    String title,
    String hireDate,
    String emergencyPhone,
    String departmentId,
    String departmentName,
    String departmentPath,
    Boolean enabled,
    List<String> roleIds,
    List<String> roleNames,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

