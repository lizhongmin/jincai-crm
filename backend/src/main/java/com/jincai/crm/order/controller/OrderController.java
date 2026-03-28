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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
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
        log.info("请求订单列表 - 用户ID: {}", SecurityUtils.currentUserId());
        try {
            List<TravelOrder> result = orderService.listVisible(SecurityUtils.currentUser());
            log.info("成功获取订单列表 - 返回 {} 条记录", result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            throw e;
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<PageResult<TravelOrder>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) Long customerId) {
        log.info("请求订单分页列表 - 用户ID: {}, 页码: {}, 大小: {}, 关键词: {}, 状态: {}, 客户ID: {}",
                SecurityUtils.currentUserId(), page, size, keyword, status, customerId);
        try {
            PageResult<TravelOrder> result = orderService.pageVisible(SecurityUtils.currentUser(), page, size, keyword, status, customerId);
            log.info("成功获取订单分页列表 - 第{}页, 共{}条记录", result.getPage(), result.getTotal());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单分页列表失败 - 页码: {}, 大小: {}", page, size, e);
            throw e;
        }
    }

    @GetMapping("/context-options")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<OrderContextOptionsView> contextOptions() {
        return ApiResponse.ok(orderService.contextOptions(SecurityUtils.currentUser()));
    }

    @GetMapping("/context-options/routes/{routeId}/departures")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<com.jincai.crm.product.entity.Departure>> routeDepartures(@PathVariable String routeId) {
        return ApiResponse.ok(orderService.routeDepartures(routeId));
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
        log.info("创建订单请求 - 用户ID: {}, 订单号: {}, 客户ID: {}",
                SecurityUtils.currentUserId(), request.getOrderNo(), request.customerId());
        try {
            TravelOrder result = orderService.create(request, SecurityUtils.currentUser());
            log.info("成功创建订单 - 订单ID: {}, 订单号: {}", result.getId(), result.getOrderNo());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建订单失败 - 订单号: {}", request.getOrderNo(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORDER_EDIT')")
    public ApiResponse<TravelOrder> update(@PathVariable String id, @Valid @RequestBody OrderRequest request,
                                           HttpServletRequest httpServletRequest) {
        log.info("更新订单请求 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            TravelOrder result = orderService.update(id, request, httpServletRequest);
            log.info("成功更新订单 - 订单ID: {}, 订单号: {}", id, result.getOrderNo());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ORDER_DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除订单请求 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            orderService.delete(id);
            log.info("成功删除订单 - 订单ID: {}", id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('BTN_ORDER_SUBMIT')")
    public ApiResponse<TravelOrder> submit(@PathVariable String id) {
        log.info("提交订单请求 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            TravelOrder result = orderService.action(id,
                new OrderActionRequest(OrderActionType.SUBMIT, "Order submitted", null),
                SecurityUtils.currentUser());
            log.info("成功提交订单 - 订单ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("提交订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('BTN_ORDER_APPROVE')")
    public ApiResponse<TravelOrder> approve(@PathVariable String id, @RequestBody(required = false) WorkflowActionRequest request) {
        log.info("审批订单请求 - 用户ID: {}, 订单ID: {}, 审批备注: {}",
                SecurityUtils.currentUserId(), id, request == null ? null : request.comment());
        try {
            TravelOrder result = orderService.action(id,
                new OrderActionRequest(OrderActionType.APPROVE, request == null ? null : request.comment(), null),
                SecurityUtils.currentUser());
            log.info("成功审批订单(通过) - 订单ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("审批订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('BTN_ORDER_REJECT')")
    public ApiResponse<TravelOrder> reject(@PathVariable String id, @RequestBody(required = false) WorkflowActionRequest request) {
        log.info("驳回订单请求 - 用户ID: {}, 订单ID: {}, 驳回备注: {}",
                SecurityUtils.currentUserId(), id, request == null ? null : request.comment());
        try {
            TravelOrder result = orderService.action(id,
                new OrderActionRequest(OrderActionType.REJECT, request == null ? null : request.comment(), null),
                SecurityUtils.currentUser());
            log.info("成功驳回订单 - 订单ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("驳回订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/actions")
    @PreAuthorize("hasAnyAuthority('BTN_ORDER_SUBMIT','BTN_ORDER_APPROVE','BTN_ORDER_REJECT','BTN_ORDER_EDIT','BTN_ORDER_DELETE')")
    public ApiResponse<TravelOrder> action(@PathVariable String id, @Valid @RequestBody OrderActionRequest request) {
        log.info("订单操作请求 - 用户ID: {}, 订单ID: {}, 操作类型: {}",
                SecurityUtils.currentUserId(), id, request.actionType());
        try {
            TravelOrder result = orderService.action(id, request, SecurityUtils.currentUser());
            log.info("成功执行订单操作 - 订单ID: {}, 操作类型: {}", id, request.actionType());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("执行订单操作失败 - 订单ID: {}, 操作类型: {}", id, request.actionType(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('MENU_ORDER')")
    public ApiResponse<List<OrderStatusLog>> logs(@PathVariable String id) {
        log.info("请求订单状态日志 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<OrderStatusLog> result = orderService.logs(id);
            log.info("成功获取订单状态日志 - 订单ID: {}, 记录数: {}", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单状态日志失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BTN_ORDER_CREATE')")
    public ApiResponse<ImportOrderResult> importOrders(@RequestParam("file") MultipartFile file) {
        log.info("导入订单请求 - 用户ID: {}, 文件名: {}, 文件大小: {}",
                SecurityUtils.currentUserId(), file.getOriginalFilename(), file.getSize());
        try {
            ImportOrderResult result = orderService.importOrders(file, SecurityUtils.currentUser());
            log.info("成功导入订单 - 成功: {} 条, 失败: {} 条", result.getSuccessCount(), result.getFailures().size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("导入订单失败 - 文件名: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }
}
