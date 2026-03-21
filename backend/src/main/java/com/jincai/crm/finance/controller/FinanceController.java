package com.jincai.crm.finance.controller;

import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.repository.*;
import com.jincai.crm.finance.service.*;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/orders/{id}/receivables")
    public ApiResponse<Receivable> createReceivable(@PathVariable Long id, @Valid @RequestBody ReceivableRequest request) {
        return ApiResponse.ok(financeService.createReceivable(id, request));
    }

    @PutMapping("/receivables/{id}")
    public ApiResponse<Receivable> updateReceivable(@PathVariable Long id, @Valid @RequestBody ReceivableRequest request) {
        return ApiResponse.ok(financeService.updateReceivable(id, request));
    }

    @DeleteMapping("/receivables/{id}")
    public ApiResponse<Void> deleteReceivable(@PathVariable Long id) {
        financeService.deleteReceivable(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/receipts")
    public ApiResponse<Receipt> createReceipt(@Valid @RequestBody ReceiptRequest request) {
        return ApiResponse.ok(financeService.createReceipt(request));
    }

    @PostMapping("/refunds")
    public ApiResponse<Refund> createRefund(@Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(financeService.createRefund(request));
    }

    @PutMapping("/refunds/{id}")
    public ApiResponse<Refund> updateRefund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(financeService.updateRefund(id, request));
    }

    @DeleteMapping("/refunds/{id}")
    public ApiResponse<Void> deleteRefund(@PathVariable Long id) {
        financeService.deleteRefund(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/payables")
    public ApiResponse<Payable> createPayable(@Valid @RequestBody PayableRequest request) {
        return ApiResponse.ok(financeService.createPayable(request));
    }

    @PutMapping("/payables/{id}")
    public ApiResponse<Payable> updatePayable(@PathVariable Long id, @Valid @RequestBody PayableRequest request) {
        return ApiResponse.ok(financeService.updatePayable(id, request));
    }

    @DeleteMapping("/payables/{id}")
    public ApiResponse<Void> deletePayable(@PathVariable Long id) {
        financeService.deletePayable(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/payments")
    public ApiResponse<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok(financeService.createPayment(request));
    }

    @PostMapping("/finance/{id}/review")
    public ApiResponse<FinanceReview> review(@PathVariable Long id, @Valid @RequestBody FinanceReviewRequest request) {
        return ApiResponse.ok(financeService.review(id, request));
    }

    @GetMapping("/orders/{id}/receivables")
    public ApiResponse<List<Receivable>> receivables(@PathVariable Long id) {
        return ApiResponse.ok(financeService.receivablesByOrder(id));
    }

    @GetMapping("/orders/{id}/payables")
    public ApiResponse<List<Payable>> payables(@PathVariable Long id) {
        return ApiResponse.ok(financeService.payablesByOrder(id));
    }

    @GetMapping("/orders/{id}/refunds")
    public ApiResponse<List<Refund>> refunds(@PathVariable Long id) {
        return ApiResponse.ok(financeService.refundsByOrder(id));
    }

    @GetMapping("/receivables/{id}/receipts")
    public ApiResponse<List<Receipt>> receipts(@PathVariable Long id) {
        return ApiResponse.ok(financeService.receiptsByReceivable(id));
    }

    @GetMapping("/payables/{id}/payments")
    public ApiResponse<List<Payment>> payments(@PathVariable Long id) {
        return ApiResponse.ok(financeService.paymentsByPayable(id));
    }
}

