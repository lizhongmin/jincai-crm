package com.jincai.crm.system.dto;

import com.jincai.crm.common.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record UserUpsertRequest(
    Long id,
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
    @NotNull Long departmentId,
    @NotNull DataScope dataScope,
    @NotNull Boolean enabled,
    List<Long> roleIds
) {
}

