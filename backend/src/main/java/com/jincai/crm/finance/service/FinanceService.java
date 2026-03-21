package com.jincai.crm.finance.service;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.order.entity.OrderStatus;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.repository.TravelOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    private final TravelOrderRepository orderRepository;
    private final ReceivableRepository receivableRepository;
    private final ReceiptRepository receiptRepository;
    private final RefundRepository refundRepository;
    private final PayableRepository payableRepository;
    private final PaymentRepository paymentRepository;
    private final FinanceReviewRepository reviewRepository;

    public FinanceService(TravelOrderRepository orderRepository, ReceivableRepository receivableRepository,
                          ReceiptRepository receiptRepository, RefundRepository refundRepository,
                          PayableRepository payableRepository, PaymentRepository paymentRepository,
                          FinanceReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.receivableRepository = receivableRepository;
        this.receiptRepository = receiptRepository;
        this.refundRepository = refundRepository;
        this.payableRepository = payableRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Receivable createReceivable(Long orderId, ReceivableRequest request) {
        TravelOrder order = loadApprovedOrder(orderId);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Receivable amount must be greater than 0");

        Receivable receivable = new Receivable();
        receivable.setOrderId(orderId);
        receivable.setItemName(request.itemName());
        receivable.setAmount(amount);
        Receivable saved = receivableRepository.save(receivable);
        promoteFinanceStatus(order);
        return saved;
    }

    @Transactional
    public Receivable updateReceivable(Long receivableId, ReceivableRequest request) {
        Receivable receivable = receivableRepository.findById(receivableId)
            .orElseThrow(() -> new BusinessException("Receivable not found"));
        ensureReceivableEditable(receivable);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Receivable amount must be greater than 0");
        if (amount.compareTo(receivable.getReceived()) < 0) {
            throw new BusinessException("Receivable amount cannot be less than already received amount");
        }
        receivable.setItemName(request.itemName());
        receivable.setAmount(amount);
        return receivableRepository.save(receivable);
    }

    @Transactional
    public void deleteReceivable(Long receivableId) {
        Receivable receivable = receivableRepository.findById(receivableId)
            .orElseThrow(() -> new BusinessException("Receivable not found"));
        ensureReceivableEditable(receivable);
        receivable.setDeleted(true);
        receivableRepository.save(receivable);
    }

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Receivable receivable = receivableRepository.findById(request.receivableId())
            .orElseThrow(() -> new BusinessException("Receivable not found"));
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Receipt amount must be greater than 0");

        BigDecimal outstanding = receivable.getAmount().subtract(receivable.getReceived());
        if (amount.compareTo(outstanding) > 0) {
            throw new BusinessException("Receipt amount exceeds receivable outstanding");
        }

        Receipt receipt = new Receipt();
        receipt.setReceivableId(request.receivableId());
        receipt.setAmount(amount);
        receipt.setRemark(request.remark());
        return receiptRepository.save(receipt);
    }

    @Transactional
    public Refund createRefund(RefundRequest request) {
        loadApprovedOrder(request.orderId());
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Refund amount must be greater than 0");

        Refund refund = new Refund();
        refund.setOrderId(request.orderId());
        refund.setAmount(amount);
        refund.setReason(request.reason());
        return refundRepository.save(refund);
    }

    @Transactional
    public Refund updateRefund(Long refundId, RefundRequest request) {
        Refund refund = refundRepository.findById(refundId).orElseThrow(() -> new BusinessException("Refund not found"));
        ensureRefundEditable(refund);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Refund amount must be greater than 0");
        refund.setAmount(amount);
        refund.setReason(request.reason());
        return refundRepository.save(refund);
    }

    @Transactional
    public void deleteRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId).orElseThrow(() -> new BusinessException("Refund not found"));
        ensureRefundEditable(refund);
        refund.setDeleted(true);
        refundRepository.save(refund);
    }

    @Transactional
    public Payable createPayable(PayableRequest request) {
        TravelOrder order = loadApprovedOrder(request.orderId());
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Payable amount must be greater than 0");

        Payable payable = new Payable();
        payable.setOrderId(request.orderId());
        payable.setItemName(request.itemName());
        payable.setAmount(amount);
        Payable saved = payableRepository.save(payable);
        promoteFinanceStatus(order);
        return saved;
    }

    @Transactional
    public Payable updatePayable(Long payableId, PayableRequest request) {
        Payable payable = payableRepository.findById(payableId)
            .orElseThrow(() -> new BusinessException("Payable not found"));
        ensurePayableEditable(payable);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Payable amount must be greater than 0");
        if (amount.compareTo(payable.getPaid()) < 0) {
            throw new BusinessException("Payable amount cannot be less than already paid amount");
        }
        payable.setItemName(request.itemName());
        payable.setAmount(amount);
        return payableRepository.save(payable);
    }

    @Transactional
    public void deletePayable(Long payableId) {
        Payable payable = payableRepository.findById(payableId)
            .orElseThrow(() -> new BusinessException("Payable not found"));
        ensurePayableEditable(payable);
        payable.setDeleted(true);
        payableRepository.save(payable);
    }

    @Transactional
    public Payment createPayment(PaymentRequest request) {
        Payable payable = payableRepository.findById(request.payableId())
            .orElseThrow(() -> new BusinessException("Payable not found"));
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "Payment amount must be greater than 0");

        BigDecimal outstanding = payable.getAmount().subtract(payable.getPaid());
        if (amount.compareTo(outstanding) > 0) {
            throw new BusinessException("Payment amount exceeds payable outstanding");
        }

        Payment payment = new Payment();
        payment.setPayableId(request.payableId());
        payment.setAmount(amount);
        payment.setRemark(request.remark());
        return paymentRepository.save(payment);
    }

    @Transactional
    public FinanceReview review(Long targetId, FinanceReviewRequest request) {
        String targetType = request.targetType().toUpperCase();
        FinanceReview review = reviewRepository.findByTargetTypeAndTargetIdAndDeletedFalse(targetType, targetId)
            .orElseGet(FinanceReview::new);
        review.setTargetType(targetType);
        review.setTargetId(targetId);
        review.setStatus(Boolean.TRUE.equals(request.approved()) ? "APPROVED" : "REJECTED");
        review.setComment(request.comment());
        FinanceReview saved = reviewRepository.save(review);
        syncFinanceObjectStatus(targetType, targetId, Boolean.TRUE.equals(request.approved()));
        return saved;
    }

    public List<Receivable> receivablesByOrder(Long orderId) {
        return receivableRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Payable> payablesByOrder(Long orderId) {
        return payableRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Refund> refundsByOrder(Long orderId) {
        return refundRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Receipt> receiptsByReceivable(Long receivableId) {
        return receiptRepository.findByReceivableIdAndDeletedFalse(receivableId);
    }

    public List<Payment> paymentsByPayable(Long payableId) {
        return paymentRepository.findByPayableIdAndDeletedFalse(payableId);
    }

    private void syncFinanceObjectStatus(String targetType, Long targetId, boolean approved) {
        if ("RECEIPT".equals(targetType)) {
            Receipt receipt = receiptRepository.findById(targetId).orElseThrow(() -> new BusinessException("Receipt not found"));
            receipt.setStatus(approved ? "APPROVED" : "REJECTED");
            receiptRepository.save(receipt);
            if (approved) {
                Receivable receivable = receivableRepository.findById(receipt.getReceivableId())
                    .orElseThrow(() -> new BusinessException("Receivable not found"));
                BigDecimal outstanding = receivable.getAmount().subtract(receivable.getReceived());
                if (receipt.getAmount().compareTo(outstanding) > 0) {
                    throw new BusinessException("Receipt amount exceeds receivable outstanding");
                }
                receivable.setReceived(receivable.getReceived().add(receipt.getAmount()));
                if (receivable.getReceived().compareTo(receivable.getAmount()) >= 0) {
                    receivable.setStatus("CLOSED");
                }
                receivableRepository.save(receivable);
                settleOrderByReceivable(receivable.getOrderId());
            }
            return;
        }
        if ("PAYMENT".equals(targetType)) {
            Payment payment = paymentRepository.findById(targetId).orElseThrow(() -> new BusinessException("Payment not found"));
            payment.setStatus(approved ? "APPROVED" : "REJECTED");
            paymentRepository.save(payment);
            if (approved) {
                Payable payable = payableRepository.findById(payment.getPayableId())
                    .orElseThrow(() -> new BusinessException("Payable not found"));
                BigDecimal outstanding = payable.getAmount().subtract(payable.getPaid());
                if (payment.getAmount().compareTo(outstanding) > 0) {
                    throw new BusinessException("Payment amount exceeds payable outstanding");
                }
                payable.setPaid(payable.getPaid().add(payment.getAmount()));
                if (payable.getPaid().compareTo(payable.getAmount()) >= 0) {
                    payable.setStatus("CLOSED");
                }
                payableRepository.save(payable);
                settleOrderByPayable(payable.getOrderId());
            }
            return;
        }
        if ("REFUND".equals(targetType)) {
            Refund refund = refundRepository.findById(targetId).orElseThrow(() -> new BusinessException("Refund not found"));
            refund.setStatus(approved ? "APPROVED" : "REJECTED");
            refundRepository.save(refund);
            if (approved) {
                TravelOrder order = orderRepository.findById(refund.getOrderId())
                    .orElseThrow(() -> new BusinessException("Order not found"));
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    order.setStatus(OrderStatus.FINANCE_IN_PROGRESS);
                    orderRepository.save(order);
                }
            }
        }
    }

    private TravelOrder loadApprovedOrder(Long orderId) {
        TravelOrder order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.FINANCE_IN_PROGRESS
            && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Order must be approved before finance operations");
        }
        return order;
    }

    private void promoteFinanceStatus(TravelOrder order) {
        if (order.getStatus() == OrderStatus.APPROVED) {
            order.setStatus(OrderStatus.FINANCE_IN_PROGRESS);
            orderRepository.save(order);
        }
    }

    private void settleOrderByReceivable(Long orderId) {
        TravelOrder order = orderRepository.findById(orderId).orElseThrow();
        List<Receivable> receivables = receivableRepository.findByOrderIdAndDeletedFalse(orderId);
        boolean allClosed = !receivables.isEmpty() && receivables.stream().allMatch(r -> "CLOSED".equals(r.getStatus()));
        if (allClosed) {
            tryCompleteOrder(order);
        }
    }

    private void settleOrderByPayable(Long orderId) {
        TravelOrder order = orderRepository.findById(orderId).orElseThrow();
        List<Payable> payables = payableRepository.findByOrderIdAndDeletedFalse(orderId);
        boolean allClosed = payables.isEmpty() || payables.stream().allMatch(p -> "CLOSED".equals(p.getStatus()));
        if (allClosed) {
            tryCompleteOrder(order);
        }
    }

    private void tryCompleteOrder(TravelOrder order) {
        List<Receivable> receivables = receivableRepository.findByOrderIdAndDeletedFalse(order.getId());
        List<Payable> payables = payableRepository.findByOrderIdAndDeletedFalse(order.getId());
        boolean receivableDone = !receivables.isEmpty() && receivables.stream().allMatch(r -> "CLOSED".equals(r.getStatus()));
        boolean payableDone = payables.isEmpty() || payables.stream().allMatch(p -> "CLOSED".equals(p.getStatus()));
        if (receivableDone && payableDone) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("Amount is required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void requirePositive(BigDecimal amount, String message) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(message);
        }
    }

    private void ensureReceivableEditable(Receivable receivable) {
        if (receivable.getReceived().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Receivable with received amount cannot be edited or deleted");
        }
        if (!receiptRepository.findByReceivableIdAndDeletedFalse(receivable.getId()).isEmpty()) {
            throw new BusinessException("Receivable with receipt records cannot be edited or deleted");
        }
    }

    private void ensurePayableEditable(Payable payable) {
        if (payable.getPaid().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Payable with paid amount cannot be edited or deleted");
        }
        if (!paymentRepository.findByPayableIdAndDeletedFalse(payable.getId()).isEmpty()) {
            throw new BusinessException("Payable with payment records cannot be edited or deleted");
        }
    }

    private void ensureRefundEditable(Refund refund) {
        if ("APPROVED".equalsIgnoreCase(refund.getStatus())) {
            throw new BusinessException("Approved refund cannot be edited or deleted");
        }
    }
}

