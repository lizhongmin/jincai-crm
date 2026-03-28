package com.jincai.crm.finance.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.service.FinanceService;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/orders/{id}/receivables")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIVABLE_CREATE')")
    public ApiResponse<Receivable> createReceivable(@PathVariable String id, @Valid @RequestBody ReceivableRequest request) {
        log.info("创建应收款项 - 用户ID: {}, 订单ID: {}, 项目名称: {}",
                SecurityUtils.currentUserId(), id, request.itemName());
        try {
            Receivable result = financeService.createReceivable(id, request);
            log.info("成功创建应收款项 - 应收ID: {}, 订单ID: {}", result.getId(), id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建应收款项失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/receivables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIVABLE_CREATE')")
    public ApiResponse<Receivable> updateReceivable(@PathVariable String id, @Valid @RequestBody ReceivableRequest request) {
        log.info("更新应收款项 - 用户ID: {}, 应收ID: {}, 项目名称: {}",
                SecurityUtils.currentUserId(), id, request.itemName());
        try {
            Receivable result = financeService.updateReceivable(id, request);
            log.info("成功更新应收款项 - 应收ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新应收款项失败 - 应收ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/receivables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIVABLE_CREATE')")
    public ApiResponse<Void> deleteReceivable(@PathVariable String id) {
        log.info("删除应收款项 - 用户ID: {}, 应收ID: {}", SecurityUtils.currentUserId(), id);
        try {
            financeService.deleteReceivable(id);
            log.info("成功删除应收款项 - 应收ID: {}", id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除应收款项失败 - 应收ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/receipts")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIPT_CREATE')")
    public ApiResponse<Receipt> createReceipt(@Valid @RequestBody ReceiptRequest request) {
        log.info("创建收款记录 - 用户ID: {}, 应收ID: {}, 金额: {}",
                SecurityUtils.currentUserId(), request.receivableId(), request.amount());
        try {
            Receipt result = financeService.createReceipt(request);
            log.info("成功创建收款记录 - 收款ID: {}, 应收ID: {}", result.getId(), request.receivableId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建收款记录失败 - 应收ID: {}", request.receivableId(), e);
            throw e;
        }
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Refund> createRefund( @Valid @RequestBody RefundRequest request) {
        log.info("创建退款记录 - 用户ID: {}, 订单ID: {}, 金额: {}",
            SecurityUtils.currentUserId(), request.orderId(), request.amount());
        try {
            Refund result = financeService.createRefund(request);
            log.info("成功创建退款记录 - 退款ID: {}, 订单ID: {}", result.getId(), request.orderId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建退款记录失败 - 订单ID: {}", request.orderId(), e);
            throw e;
        }
    }

    @PutMapping("/refunds/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Refund> updateRefund(@PathVariable String id, @Valid @RequestBody RefundRequest request) {
        log.info("更新退款记录 - 用户ID: {}, 退款ID: {}, 金额: {}",
                SecurityUtils.currentUserId(), id, request.amount());
        try {
            Refund result = financeService.updateRefund(id, request);
            log.info("成功更新退款记录 - 退款ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新退款记录失败 - 退款ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/refunds/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Void> deleteRefund(@PathVariable String id) {
        log.info("删除退款记录 - 用户ID: {}, 退款ID: {}", SecurityUtils.currentUserId(), id);
        try {
            financeService.deleteRefund(id);
            log.info("成功删除退款记录 - 退款ID: {}", id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除退款记录失败 - 退款ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/payables")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Payable> createPayable(@Valid @RequestBody PayableRequest request) {
        log.info("创建应付款项 - 用户ID: {}, 订单ID: {}, 项目名称: {}",
                SecurityUtils.currentUserId(), request.orderId(), request.itemName());
        try {
            Payable result = financeService.createPayable(request);
            log.info("成功创建应付款项 - 应付ID: {}, 订单ID: {}", result.getId(), request.orderId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建应付款项失败 - 订单ID: {}", request.orderId(), e);
            throw e;
        }
    }

    @PutMapping("/payables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Payable> updatePayable(@PathVariable String id, @Valid @RequestBody PayableRequest request) {
        log.info("更新应付款项 - 用户ID: {}, 应付ID: {}, 项目名称: {}",
                SecurityUtils.currentUserId(), id, request.itemName());
        try {
            Payable result = financeService.updatePayable(id, request);
            log.info("成功更新应付款项 - 应付ID: {}", id);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("更新应付款项失败 - 应付ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/payables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Void> deletePayable(@PathVariable String id) {
        log.info("删除应付款项 - 用户ID: {}, 应付ID: {}", SecurityUtils.currentUserId(), id);
        try {
            financeService.deletePayable(id);
            log.info("成功删除应付款项 - 应付ID: {}", id);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            log.error("删除应付款项失败 - 应付ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYMENT_CREATE')")
    public ApiResponse<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("创建付款记录 - 用户ID: {}, 应付ID: {}, 金额: {}",
                SecurityUtils.currentUserId(), request.payableId(), request.amount());
        try {
            Payment result = financeService.createPayment(request);
            log.info("成功创建付款记录 - 付款ID: {}, 应付ID: {}", result.getId(), request.payableId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("创建付款记录失败 - 应付ID: {}", request.payableId(), e);
            throw e;
        }
    }

    @PostMapping("/finance/{id}/review")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REVIEW')")
    public ApiResponse<FinanceReview> review(@PathVariable String id, @Valid @RequestBody FinanceReviewRequest request) {
        log.info("财务审核操作 - 用户ID: {}, 目标ID: {}, 审核类型: {}, 是否通过: {}",
                SecurityUtils.currentUserId(), id, request.targetType(), request.approved());
        try {
            FinanceReview result = financeService.review(id, request);
            log.info("成功完成财务审核 - 审核ID: {}, 目标ID: {}, 审核类型: {}", result.getId(), id, request.targetType());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("财务审核失败 - 目标ID: {}, 审核类型: {}", id, request.targetType(), e);
            throw e;
        }
    }

    @GetMapping("/finance/orders/options")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<FinanceOrderOptionView>> orderOptions() {
        log.info("请求财务订单选项列表 - 用户ID: {}", SecurityUtils.currentUserId());
        try {
            List<FinanceOrderOptionView> result = financeService.listOrderOptions(SecurityUtils.currentUser());
            log.info("成功获取财务订单选项列表 - 返回 {} 条记录", result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取财务订单选项列表失败", e);
            throw e;
        }
    }

    @GetMapping("/orders/{id}/receivables")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Receivable>> receivables(@PathVariable String id) {
        log.info("请求订单应收款项列表 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<Receivable> result = financeService.receivablesByOrder(id);
            log.info("成功获取订单应收款项列表 - 订单ID: {}, 返回 {} 条记录", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单应收款项列表失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/orders/{id}/payables")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Payable>> payables(@PathVariable String id) {
        log.info("请求订单应付款项列表 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<Payable> result = financeService.payablesByOrder(id);
            log.info("成功获取订单应付款项列表 - 订单ID: {}, 返回 {} 条记录", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单应付款项列表失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/orders/{id}/refunds")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Refund>> refunds(@PathVariable String id) {
        log.info("请求订单退款列表 - 用户ID: {}, 订单ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<Refund> result = financeService.refundsByOrder(id);
            log.info("成功获取订单退款列表 - 订单ID: {}, 返回 {} 条记录", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取订单退款列表失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/receivables/{id}/receipts")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Receipt>> receipts(@PathVariable String id) {
        log.info("请求应收款项收款记录列表 - 用户ID: {}, 应收ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<Receipt> result = financeService.receiptsByReceivable(id);
            log.info("成功获取应收款项收款记录列表 - 应收ID: {}, 返回 {} 条记录", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取应收款项收款记录列表失败 - 应收ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/payables/{id}/payments")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Payment>> payments(@PathVariable String id) {
        log.info("请求应付款项付款记录列表 - 用户ID: {}, 应付ID: {}", SecurityUtils.currentUserId(), id);
        try {
            List<Payment> result = financeService.paymentsByPayable(id);
            log.info("成功获取应付款项付款记录列表 - 应付ID: {}, 返回 {} 条记录", id, result.size());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取应付款项付款记录列表失败 - 应付ID: {}", id, e);
            throw e;
        }
    }
}
