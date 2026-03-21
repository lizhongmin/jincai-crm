package com.jincai.crm.order.controller;

import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES','FINANCE')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<TravelOrder>> list() {
        return ApiResponse.ok(orderService.listVisible(SecurityUtils.currentUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailView> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/quote")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
    public ApiResponse<OrderQuoteView> quote(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.quote(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
    public ApiResponse<TravelOrder> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.create(request, SecurityUtils.currentUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
    public ApiResponse<TravelOrder> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request,
                                           HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(orderService.update(id, request, httpServletRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
    public ApiResponse<TravelOrder> submit(@PathVariable Long id) {
        return ApiResponse.ok(orderService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<TravelOrder> approve(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest request) {
        return ApiResponse.ok(orderService.approve(id, SecurityUtils.currentUser(), request == null ? null : request.comment()));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<TravelOrder> reject(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest request) {
        return ApiResponse.ok(orderService.reject(id, SecurityUtils.currentUser(), request == null ? null : request.comment()));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<OrderStatusLog>> logs(@PathVariable Long id) {
        return ApiResponse.ok(orderService.logs(id));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES')")
    public ApiResponse<ImportOrderResult> importOrders(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(orderService.importOrders(file, SecurityUtils.currentUser()));
    }
}
