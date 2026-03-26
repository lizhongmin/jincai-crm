package com.jincai.crm.customer.dto;


import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
    @NotBlank String name,
    @NotBlank String phone,
    String customerType,
    String source,
    String intentionLevel,
    String status,
    String level,
    String wechat,
    String email,
    String city,
    String tags,
    String remark,
    Long ownerUserId,
    Long ownerDeptId
) {
}

