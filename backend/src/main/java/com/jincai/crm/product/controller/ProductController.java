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
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/routes")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<RouteProduct>> routes() {
        return ApiResponse.ok(productService.routes());
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_CREATE')")
    public ApiResponse<RouteProduct> createRoute(@Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.createRoute(request));
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<RouteProduct> updateRoute(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.updateRoute(id, request));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_DELETE')")
    public ApiResponse<Void> deleteRoute(@PathVariable Long id) {
        productService.deleteRoute(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<Departure>> departures(@RequestParam(value = "routeId", required = false) Long routeId) {
        return ApiResponse.ok(productService.departures(routeId));
    }

    @PostMapping("/departures")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_CREATE')")
    public ApiResponse<Departure> createDeparture(@Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.createDeparture(request));
    }

    @PutMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<Departure> updateDeparture(@PathVariable Long id, @Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.updateDeparture(id, request));
    }

    @DeleteMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_DELETE')")
    public ApiResponse<Void> deleteDeparture(@PathVariable Long id) {
        productService.deleteDeparture(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<DeparturePrice>> prices(@PathVariable Long id) {
        return ApiResponse.ok(productService.prices(id));
    }

    @PostMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_CREATE')")
    public ApiResponse<DeparturePrice> addPrice(@PathVariable Long id, @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.addPrice(id, request));
    }

    @PutMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_EDIT')")
    public ApiResponse<DeparturePrice> updatePrice(@PathVariable Long id, @PathVariable Long priceId,
                                                   @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.updatePrice(id, priceId, request));
    }

    @DeleteMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_DELETE')")
    public ApiResponse<Void> deletePrice(@PathVariable Long id, @PathVariable Long priceId) {
        productService.deletePrice(id, priceId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<OrderPolicyView> routePolicy(@PathVariable Long id) {
        return ApiResponse.ok(productService.routePolicy(id));
    }

    @PutMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<OrderPolicyView> updateRoutePolicy(@PathVariable Long id,
                                                          @RequestBody OrderPolicyRequest request) {
        return ApiResponse.ok(productService.updateRoutePolicy(id, request));
    }

    @GetMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<DepartureOrderPolicyView> departurePolicy(@PathVariable Long id) {
        return ApiResponse.ok(productService.departurePolicy(id));
    }

    @PutMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<DepartureOrderPolicyView> updateDeparturePolicy(@PathVariable Long id,
                                                                       @RequestBody OrderPolicyRequest request) {
        return ApiResponse.ok(productService.updateDeparturePolicy(id, request));
    }
}
