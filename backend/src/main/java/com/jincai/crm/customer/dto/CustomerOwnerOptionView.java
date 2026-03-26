package com.jincai.crm.customer.dto;

public record CustomerOwnerOptionView(
    String id,
    String username,
    String fullName,
    String departmentId,
    String departmentPath
) {
}
