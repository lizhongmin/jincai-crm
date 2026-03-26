package com.jincai.crm.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
    String code,
    @NotBlank String name,
    @NotBlank String category,
    String departureCity,
    String destinationCity,
    @Min(1) Integer durationDays,
    @Min(0) Integer durationNights,
    String transportation,
    String hotelStandard,
    String highlights,
    String feeIncludes,
    String feeExcludes,
    String bookingNotice,
    String description
) {
}
