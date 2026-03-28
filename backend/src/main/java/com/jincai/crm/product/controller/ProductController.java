package com.jincai.crm.product.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.service.ProductService;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        log.debug("ProductController.routes() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<RouteProduct> result = productService.routes();
            log.debug("ProductController.routes() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.routes() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/routes/page")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<PageResult<RouteProduct>> pageRoutes(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        log.debug("ProductController.pageRoutes() called by user: {}, page: {}, size: {}", SecurityUtils.currentUserId(), page, size);
        try {
            PageResult<RouteProduct> result = productService.pageRoutes(page, size, keyword);
            log.debug("ProductController.pageRoutes() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.pageRoutes() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_CREATE')")
    public ApiResponse<RouteProduct> createRoute(@Valid @RequestBody RouteRequest request) {
        log.info("ProductController.createRoute() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            RouteProduct result = productService.createRoute(request);
            log.info("ProductController.createRoute() succeeded for user: {}, created route ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.createRoute() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<RouteProduct> updateRoute(@PathVariable String id, @Valid @RequestBody RouteRequest request) {
        log.info("ProductController.updateRoute() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            RouteProduct result = productService.updateRoute(id, request);
            log.info("ProductController.updateRoute() succeeded for user: {}, updated route ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.updateRoute() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_DELETE')")
    public ApiResponse<Void> deleteRoute(@PathVariable String id) {
        log.info("ProductController.deleteRoute() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            productService.deleteRoute(id);
            log.info("ProductController.deleteRoute() succeeded for user: {}, deleted route ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("ProductController.deleteRoute() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @GetMapping("/departures")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<Departure>> departures(@RequestParam(value = "routeId", required = false) String routeId) {
        log.debug("ProductController.departures() called by user: {}, routeId: {}", SecurityUtils.currentUserId(), routeId);
        try {
            List<Departure> result = productService.departures(routeId);
            log.debug("ProductController.departures() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.departures() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/departures/page")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<PageResult<Departure>> pageDepartures(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(value = "routeId", required = false) String routeId,
        @RequestParam(required = false) String keyword
    ) {
        log.debug("ProductController.pageDepartures() called by user: {}, page: {}, size: {}, routeId: {}", SecurityUtils.currentUserId(), page, size, routeId);
        try {
            PageResult<Departure> result = productService.pageDepartures(page, size, routeId, keyword);
            log.debug("ProductController.pageDepartures() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.pageDepartures() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PostMapping("/departures")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_CREATE')")
    public ApiResponse<Departure> createDeparture(@Valid @RequestBody DepartureRequest request) {
        log.info("ProductController.createDeparture() called by user: {}, request: {}", SecurityUtils.currentUserId(), request);
        try {
            Departure result = productService.createDeparture(request);
            log.info("ProductController.createDeparture() succeeded for user: {}, created departure ID: {}", SecurityUtils.currentUserId(), result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.createDeparture() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @PutMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<Departure> updateDeparture(@PathVariable String id, @Valid @RequestBody DepartureRequest request) {
        log.info("ProductController.updateDeparture() called by user: {}, id: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            Departure result = productService.updateDeparture(id, request);
            log.info("ProductController.updateDeparture() succeeded for user: {}, updated departure ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.updateDeparture() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @DeleteMapping("/departures/{id}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_DELETE')")
    public ApiResponse<Void> deleteDeparture(@PathVariable String id) {
        log.info("ProductController.deleteDeparture() called by user: {}, id: {}", SecurityUtils.currentUserId(), id);
        try {
            productService.deleteDeparture(id);
            log.info("ProductController.deleteDeparture() succeeded for user: {}, deleted departure ID: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("ProductController.deleteDeparture() failed for user: {}, id: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @GetMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<List<DeparturePrice>> prices(@PathVariable String id) {
        log.debug("ProductController.prices() called by user: {}, departureId: {}", SecurityUtils.currentUserId(), id);
        try {
            List<DeparturePrice> result = productService.prices(id);
            log.debug("ProductController.prices() succeeded for user: {}, departureId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.prices() failed for user: {}, departureId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PostMapping("/departures/{id}/prices")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_CREATE')")
    public ApiResponse<DeparturePrice> addPrice(@PathVariable String id, @Valid @RequestBody DeparturePriceRequest request) {
        log.info("ProductController.addPrice() called by user: {}, departureId: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            DeparturePrice result = productService.addPrice(id, request);
            log.info("ProductController.addPrice() succeeded for user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, result.getId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.addPrice() failed for user: {}, departureId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PutMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_EDIT')")
    public ApiResponse<DeparturePrice> updatePrice(@PathVariable String id, @PathVariable String priceId,
                                                   @Valid @RequestBody DeparturePriceRequest request) {
        log.info("ProductController.updatePrice() called by user: {}, departureId: {}, priceId: {}, request: {}", SecurityUtils.currentUserId(), id, priceId, request);
        try {
            DeparturePrice result = productService.updatePrice(id, priceId, request);
            log.info("ProductController.updatePrice() succeeded for user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, priceId);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.updatePrice() failed for user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, priceId, e);
            throw e;
        }
    }

    @DeleteMapping("/departures/{id}/prices/{priceId}")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_PRICE_DELETE')")
    public ApiResponse<Void> deletePrice(@PathVariable String id, @PathVariable String priceId) {
        log.info("ProductController.deletePrice() called by user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, priceId);
        try {
            productService.deletePrice(id, priceId);
            log.info("ProductController.deletePrice() succeeded for user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, priceId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("ProductController.deletePrice() failed for user: {}, departureId: {}, priceId: {}", SecurityUtils.currentUserId(), id, priceId, e);
            throw e;
        }
    }

    @GetMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<OrderPolicyView> routePolicy(@PathVariable String id) {
        log.debug("ProductController.routePolicy() called by user: {}, routeId: {}", SecurityUtils.currentUserId(), id);
        try {
            OrderPolicyView result = productService.routePolicy(id);
            log.debug("ProductController.routePolicy() succeeded for user: {}, routeId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.routePolicy() failed for user: {}, routeId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PutMapping("/routes/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_ROUTE_EDIT')")
    public ApiResponse<OrderPolicyView> updateRoutePolicy(@PathVariable String id,
                                                          @RequestBody OrderPolicyRequest request) {
        log.info("ProductController.updateRoutePolicy() called by user: {}, routeId: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            OrderPolicyView result = productService.updateRoutePolicy(id, request);
            log.info("ProductController.updateRoutePolicy() succeeded for user: {}, routeId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.updateRoutePolicy() failed for user: {}, routeId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @GetMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('MENU_PRODUCT')")
    public ApiResponse<DepartureOrderPolicyView> departurePolicy(@PathVariable String id) {
        log.debug("ProductController.departurePolicy() called by user: {}, departureId: {}", SecurityUtils.currentUserId(), id);
        try {
            DepartureOrderPolicyView result = productService.departurePolicy(id);
            log.debug("ProductController.departurePolicy() succeeded for user: {}, departureId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.departurePolicy() failed for user: {}, departureId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }

    @PutMapping("/departures/{id}/order-policy")
    @PreAuthorize("hasAuthority('BTN_PRODUCT_DEPARTURE_EDIT')")
    public ApiResponse<DepartureOrderPolicyView> updateDeparturePolicy(@PathVariable String id,
                                                                       @RequestBody OrderPolicyRequest request) {
        log.info("ProductController.updateDeparturePolicy() called by user: {}, departureId: {}, request: {}", SecurityUtils.currentUserId(), id, request);
        try {
            DepartureOrderPolicyView result = productService.updateDeparturePolicy(id, request);
            log.info("ProductController.updateDeparturePolicy() succeeded for user: {}, departureId: {}", SecurityUtils.currentUserId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ProductController.updateDeparturePolicy() failed for user: {}, departureId: {}", SecurityUtils.currentUserId(), id, e);
            throw e;
        }
    }
}
