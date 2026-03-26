package com.jincai.crm.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UserUpsertRequest(
    String id,
    @NotBlank String username,
    String password,
    @NotBlank String fullName,
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "{validation.user.phone.invalid}")
    String phone,
    String employeeNo,
    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "{validation.user.email.invalid}")
    String email,
    String gender,
    String title,
    String hireDate,
    String emergencyPhone,
    @NotNull String departmentId,
    @NotNull Boolean enabled,
    List<String> roleIds
) {
}

