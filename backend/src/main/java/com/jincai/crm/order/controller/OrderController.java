package com.jincai.crm.order.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.OrderStatusLog;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.service.OrderService;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<TravelOrder>> list() {
        return ApiResponse.ok(orderService.listVisible(SecurityUtils.currentUser()));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<PageResult<TravelOrder>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) Long customerId) {
        return ApiResponse.ok(orderService.pageVisible(SecurityUtils.currentUser(), page, size, keyword, status, customerId));
    }

    @GetMapping("/context-options")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<OrderContextOptionsView> contextOptions() {
        return ApiResponse.ok(orderService.contextOptions(SecurityUtils.currentUser()));
    }

    @GetMapping("/context-options/customers/{customerId}/travelers")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<Traveler>> customerTravelers(@PathVariable String customerId) {
        return ApiResponse.ok(orderService.customerTravelers(customerId, SecurityUtils.currentUser()));
    }

    @GetMapping("/context-options/departures/{departureId}/prices")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<DeparturePrice>> departurePrices(@PathVariable String departureId) {
        return ApiResponse.ok(orderService.departurePrices(departureId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<OrderDetailView> detail(@PathVariable String id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/quote")
    @PreAuthorize("hasAuthority('BTN_ORDER_CREATE')")
    public ApiResponse<OrderQuoteView> quote(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.quote(request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ORDER_CREATE')")
    public ApiResponse<TravelOrder> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.create(request, SecurityUtils.currentUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORDER_EDIT')")
    public ApiResponse<TravelOrder> update(@PathVariable String id, @Valid @RequestBody OrderRequest request,
                                           HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(orderService.update(id, request, httpServletRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORDER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        orderService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('BTN_ORDER_SUBMIT')")
    public ApiResponse<TravelOrder> submit(@PathVariable String id) {
        return ApiResponse.ok(orderService.action(id, new OrderActionRequest(OrderActionType.SUBMIT, "Order submitted", null),
            SecurityUtils.currentUser()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('BTN_ORDER_APPROVE')")
    public ApiResponse<TravelOrder> approve(@PathVariable String id, @RequestBody(required = false) WorkflowActionRequest request) {
        return ApiResponse.ok(orderService.action(id,
            new OrderActionRequest(OrderActionType.APPROVE, request == null ? null : request.comment(), null),
            SecurityUtils.currentUser()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('BTN_ORDER_REJECT')")
    public ApiResponse<TravelOrder> reject(@PathVariable String id, @RequestBody(required = false) WorkflowActionRequest request) {
        return ApiResponse.ok(orderService.action(id,
            new OrderActionRequest(OrderActionType.REJECT, request == null ? null : request.comment(), null),
            SecurityUtils.currentUser()));
    }

    @PostMapping("/{id}/actions")
    @PreAuthorize("hasAnyAuthority('BTN_ORDER_SUBMIT','BTN_ORDER_APPROVE','BTN_ORDER_REJECT','BTN_ORDER_EDIT','BTN_ORDER_DELETE')")
    public ApiResponse<TravelOrder> action(@PathVariable String id, @Valid @RequestBody OrderActionRequest request) {
        return ApiResponse.ok(orderService.action(id, request, SecurityUtils.currentUser()));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<OrderStatusLog>> logs(@PathVariable String id) {
        return ApiResponse.ok(orderService.logs(id));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BTN_ORDER_CREATE')")
    public ApiResponse<ImportOrderResult> importOrders(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(orderService.importOrders(file, SecurityUtils.currentUser()));
    }
}
