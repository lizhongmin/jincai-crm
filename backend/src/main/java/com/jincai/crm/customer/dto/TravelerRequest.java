package com.jincai.crm.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TravelerRequest(
    @NotBlank String name,
    String idType,
    String idNo,
    String birthday,
    String gender,
    String ethnicity,
    String nationality,
    String address,
    String phone,
    String emergencyContact,
    String emergencyPhone,
    String preferences,
    List<@Valid TravelerDocumentRequest> documents
) {
}
