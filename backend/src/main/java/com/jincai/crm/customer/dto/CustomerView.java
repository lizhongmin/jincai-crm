package com.jincai.crm.customer.dto;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

public record CustomerView(
    Long id,
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
    Long ownerUserId,
    String ownerUserName,
    Long ownerDeptId,
    String ownerDeptName,
    Long createdBy,
    Long updatedBy,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
}
