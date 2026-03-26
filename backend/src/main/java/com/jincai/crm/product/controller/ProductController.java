package com.jincai.crm.product.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/routes/page")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<PageResult<RouteProduct>> pageRoutes(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(productService.pageRoutes(page, size, keyword));
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_CREATE')")
    public ApiResponse<RouteProduct> createRoute(@Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.createRoute(request));
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<RouteProduct> updateRoute(@PathVariable String id, @Valid @RequestBody RouteRequest request) {
        return ApiResponse.ok(productService.updateRoute(id, request));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_DELETE')")
    public ApiResponse<Void> deleteRoute(@PathVariable String id) {
        productService.deleteRoute(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<Departure>> departures(@RequestParam(value = "routeId", required = false) String routeId) {
        return ApiResponse.ok(productService.departures(routeId));
    }

    @GetMapping("/departures/page")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<PageResult<Departure>> pageDepartures(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(value = "routeId", required = false) String routeId,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(productService.pageDepartures(page, size, routeId, keyword));
    }

    @PostMapping("/departures")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_CREATE')")
    public ApiResponse<Departure> createDeparture(@Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.createDeparture(request));
    }

    @PutMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<Departure> updateDeparture(@PathVariable String id, @Valid @RequestBody DepartureRequest request) {
        return ApiResponse.ok(productService.updateDeparture(id, request));
    }

    @DeleteMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_DELETE')")
    public ApiResponse<Void> deleteDeparture(@PathVariable String id) {
        productService.deleteDeparture(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<DeparturePrice>> prices(@PathVariable String id) {
        return ApiResponse.ok(productService.prices(id));
    }

    @PostMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_CREATE')")
    public ApiResponse<DeparturePrice> addPrice(@PathVariable String id, @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.addPrice(id, request));
    }

    @PutMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_EDIT')")
    public ApiResponse<DeparturePrice> updatePrice(@PathVariable String id, @PathVariable String priceId,
                                                   @Valid @RequestBody DeparturePriceRequest request) {
        return ApiResponse.ok(productService.updatePrice(id, priceId, request));
    }

    @DeleteMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_DELETE')")
    public ApiResponse<Void> deletePrice(@PathVariable String id, @PathVariable String priceId) {
        productService.deletePrice(id, priceId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<OrderPolicyView> routePolicy(@PathVariable String id) {
        return ApiResponse.ok(productService.routePolicy(id));
    }

    @PutMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<OrderPolicyView> updateRoutePolicy(@PathVariable String id,
                                                          @RequestBody OrderPolicyRequest request) {
        return ApiResponse.ok(productService.updateRoutePolicy(id, request));
    }

    @GetMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<DepartureOrderPolicyView> departurePolicy(@PathVariable String id) {
        return ApiResponse.ok(productService.departurePolicy(id));
    }

    @PutMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<DepartureOrderPolicyView> updateDeparturePolicy(@PathVariable String id,
                                                                       @RequestBody OrderPolicyRequest request) {
        return ApiResponse.ok(productService.updateDeparturePolicy(id, request));
    }
}
