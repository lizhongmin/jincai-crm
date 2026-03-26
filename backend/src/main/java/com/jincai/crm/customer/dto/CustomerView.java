package com.jincai.crm.customer.dto;

public record CustomerView(
    String id,
    String name,
    String phone,
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
    String ownerUserId,
    String ownerUserName,
    String ownerDeptId,
    String ownerDeptName,
    String createdBy,
    String updatedBy,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
}
