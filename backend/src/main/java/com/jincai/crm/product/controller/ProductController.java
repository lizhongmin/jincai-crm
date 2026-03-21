package com.jincai.crm.product.controller;

import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

import com.jincai.crm.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/routes")
    public ApiResponse<List<RouteProduct>> routes() {
        return ApiResponse.ok(productService.routes());
    }

    @PostMapping("/routes")
    public ApiResponse<RouteProduct> createRoute(@Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.createRoute(request));
    }

    @PutMapping("/routes/{id}")
    public ApiResponse<RouteProduct> updateRoute(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.updateRoute(id, request));
    }

    @DeleteMapping("/routes/{id}")
    public ApiResponse<Void> deleteRoute(@PathVariable Long id) {
        productService.deleteRoute(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures")
    public ApiResponse<List<Departure>> departures(@RequestParam(value = "routeId", required = false) Long routeId) {
        return ApiResponse.ok(productService.departures(routeId));
    }

    @PostMapping("/departures")
    public ApiResponse<Departure> createDeparture(@Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.createDeparture(request));
    }

    @PutMapping("/departures/{id}")
    public ApiResponse<Departure> updateDeparture(@PathVariable Long id, @Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.updateDeparture(id, request));
    }

    @DeleteMapping("/departures/{id}")
    public ApiResponse<Void> deleteDeparture(@PathVariable Long id) {
        productService.deleteDeparture(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures/{id}/prices")
    public ApiResponse<List<DeparturePrice>> prices(@PathVariable Long id) {
        return ApiResponse.ok(productService.prices(id));
    }

    @PostMapping("/departures/{id}/prices")
    public ApiResponse<DeparturePrice> addPrice(@PathVariable Long id, @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.addPrice(id, request));
    }

    @PutMapping("/departures/{id}/prices/{priceId}")
    public ApiResponse<DeparturePrice> updatePrice(@PathVariable Long id, @PathVariable Long priceId,
                                                   @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.updatePrice(id, priceId, request));
    }

    @DeleteMapping("/departures/{id}/prices/{priceId}")
    public ApiResponse<Void> deletePrice(@PathVariable Long id, @PathVariable Long priceId) {
        productService.deletePrice(id, priceId);
        return ApiResponse.ok(null);
    }
}

