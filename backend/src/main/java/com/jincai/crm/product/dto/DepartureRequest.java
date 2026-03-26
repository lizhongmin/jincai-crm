package com.jincai.crm.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartureRequest(
    @NotNull String routeId,
    String code,
    @NotBlank String startDate,
    @NotBlank String endDate,
    @NotNull @Min(0) Integer stock,
    String registrationDeadline,
    @Min(0) Integer minGroupSize,
    @Min(0) Integer maxGroupSize,
    String status,
    String gatheringPlace,
    String departureNotice
) {
}
