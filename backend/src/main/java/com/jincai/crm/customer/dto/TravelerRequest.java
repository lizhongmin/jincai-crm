package com.jincai.crm.customer.dto;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

import jakarta.validation.constraints.NotBlank;

public record TravelerRequest(
    @NotBlank String name,
    String idType,
    String idNo,
    String birthday,
    String phone,
    String emergencyContact,
    String preferences
) {
}

