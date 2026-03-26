package com.jincai.crm.finance.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.service.FinanceService;
import com.jincai.crm.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ApiResponse.ok(financeService.createReceivable(id, request));
    }

    @PutMapping("/receivables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIVABLE_CREATE')")
    public ApiResponse<Receivable> updateReceivable(@PathVariable String id, @Valid @RequestBody ReceivableRequest request) {
        return ApiResponse.ok(financeService.updateReceivable(id, request));
    }

    @DeleteMapping("/receivables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIVABLE_CREATE')")
    public ApiResponse<Void> deleteReceivable(@PathVariable String id) {
        financeService.deleteReceivable(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/receipts")
    @PreAuthorize("hasAuthority('BTN_FINANCE_RECEIPT_CREATE')")
    public ApiResponse<Receipt> createReceipt(@Valid @RequestBody ReceiptRequest request) {
        return ApiResponse.ok(financeService.createReceipt(request));
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Refund> createRefund(@Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(financeService.createRefund(request));
    }

    @PutMapping("/refunds/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Refund> updateRefund(@PathVariable String id, @Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(financeService.updateRefund(id, request));
    }

    @DeleteMapping("/refunds/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REFUND')")
    public ApiResponse<Void> deleteRefund(@PathVariable String id) {
        financeService.deleteRefund(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/payables")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Payable> createPayable(@Valid @RequestBody PayableRequest request) {
        return ApiResponse.ok(financeService.createPayable(request));
    }

    @PutMapping("/payables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Payable> updatePayable(@PathVariable String id, @Valid @RequestBody PayableRequest request) {
        return ApiResponse.ok(financeService.updatePayable(id, request));
    }

    @DeleteMapping("/payables/{id}")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYABLE_CREATE')")
    public ApiResponse<Void> deletePayable(@PathVariable String id) {
        financeService.deletePayable(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('BTN_FINANCE_PAYMENT_CREATE')")
    public ApiResponse<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok(financeService.createPayment(request));
    }

    @PostMapping("/finance/{id}/review")
    @PreAuthorize("hasAuthority('BTN_FINANCE_REVIEW')")
    public ApiResponse<FinanceReview> review(@PathVariable String id, @Valid @RequestBody FinanceReviewRequest request) {
        return ApiResponse.ok(financeService.review(id, request));
    }

    @GetMapping("/finance/orders/options")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<FinanceOrderOptionView>> orderOptions() {
        return ApiResponse.ok(financeService.listOrderOptions(SecurityUtils.currentUser()));
    }

    @GetMapping("/orders/{id}/receivables")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Receivable>> receivables(@PathVariable String id) {
        return ApiResponse.ok(financeService.receivablesByOrder(id));
    }

    @GetMapping("/orders/{id}/payables")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Payable>> payables(@PathVariable String id) {
        return ApiResponse.ok(financeService.payablesByOrder(id));
    }

    @GetMapping("/orders/{id}/refunds")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Refund>> refunds(@PathVariable String id) {
        return ApiResponse.ok(financeService.refundsByOrder(id));
    }

    @GetMapping("/receivables/{id}/receipts")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Receipt>> receipts(@PathVariable String id) {
        return ApiResponse.ok(financeService.receiptsByReceivable(id));
    }

    @GetMapping("/payables/{id}/payments")
    @PreAuthorize("hasAuthority('MENU_FINANCE')")
    public ApiResponse<List<Payment>> payments(@PathVariable String id) {
        return ApiResponse.ok(financeService.paymentsByPayable(id));
    }
}
