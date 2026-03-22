package com.jincai.crm.customer.dto;

public record CustomerOwnerOptionView(
    Long id,
    String username,
    String fullName,
    Long departmentId,
    String departmentPath
) {
}
