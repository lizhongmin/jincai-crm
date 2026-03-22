package com.jincai.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record TravelerDocumentRequest(
    @NotBlank String docType,
    @NotBlank String docNo
) {
}
