package com.jincai.crm.finance.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.entity.*;
import com.jincai.crm.finance.repository.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.security.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class FinanceService {

    private final TravelOrderRepository orderRepository;
    private final ReceivableRepository receivableRepository;
    private final ReceiptRepository receiptRepository;
    private final RefundRepository refundRepository;
    private final PayableRepository payableRepository;
    private final PaymentRepository paymentRepository;
    private final FinanceReviewRepository reviewRepository;
    private final DepartureRepository departureRepository;
    private final DataScopeResolver dataScopeResolver;

    public FinanceService(TravelOrderRepository orderRepository, ReceivableRepository receivableRepository,
                          ReceiptRepository receiptRepository, RefundRepository refundRepository,
                          PayableRepository payableRepository, PaymentRepository paymentRepository,
                          FinanceReviewRepository reviewRepository, DepartureRepository departureRepository,
                          DataScopeResolver dataScopeResolver) {
        this.orderRepository = orderRepository;
        this.receivableRepository = receivableRepository;
        this.receiptRepository = receiptRepository;
        this.refundRepository = refundRepository;
        this.payableRepository = payableRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.departureRepository = departureRepository;
        this.dataScopeResolver = dataScopeResolver;
    }

    @Transactional
    public Receivable createReceivable(String orderId, ReceivableRequest request) {
        TravelOrder order = loadApprovedOrder(orderId);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.receivable.amountPositive");

        Receivable receivable = new Receivable();
        receivable.setOrderId(orderId);
        receivable.setItemName(request.itemName());
        receivable.setAmount(amount);
        Receivable saved = receivableRepository.save(receivable);
        refreshOrderFinanceState(orderId, false);
        return saved;
    }

    @Transactional
    public Receivable updateReceivable(String receivableId, ReceivableRequest request) {
        Receivable receivable = receivableRepository.findById(receivableId)
            .orElseThrow(() -> new BusinessException("error.finance.receivable.notFound"));
        ensureReceivableEditable(receivable);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.receivable.amountPositive");
        if (amount.compareTo(receivable.getReceived()) < 0) {
            throw new BusinessException("error.finance.receivable.amountLessThanReceived");
        }
        receivable.setItemName(request.itemName());
        receivable.setAmount(amount);
        return receivableRepository.save(receivable);
    }

    @Transactional
    public void deleteReceivable(String receivableId) {
        Receivable receivable = receivableRepository.findById(receivableId)
            .orElseThrow(() -> new BusinessException("error.finance.receivable.notFound"));
        ensureReceivableEditable(receivable);
        receivable.setDeleted(true);
        receivableRepository.save(receivable);
    }

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Receivable receivable = receivableRepository.findById(request.receivableId())
            .orElseThrow(() -> new BusinessException("error.finance.receivable.notFound"));
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.receipt.amountPositive");

        BigDecimal outstanding = receivable.getAmount().subtract(receivable.getReceived());
        if (amount.compareTo(outstanding) > 0) {
            throw new BusinessException("error.finance.receipt.amountExceedsOutstanding");
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
        requirePositive(amount, "error.finance.refund.amountPositive");

        Refund refund = new Refund();
        refund.setOrderId(request.orderId());
        refund.setAmount(amount);
        refund.setReason(request.reason());
        return refundRepository.save(refund);
    }

    @Transactional
    public Refund updateRefund(String refundId, RefundRequest request) {
        Refund refund = refundRepository.findById(refundId).orElseThrow(() -> new BusinessException("error.finance.refund.notFound"));
        ensureRefundEditable(refund);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.refund.amountPositive");
        refund.setAmount(amount);
        refund.setReason(request.reason());
        return refundRepository.save(refund);
    }

    @Transactional
    public void deleteRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId).orElseThrow(() -> new BusinessException("error.finance.refund.notFound"));
        ensureRefundEditable(refund);
        refund.setDeleted(true);
        refundRepository.save(refund);
    }

    @Transactional
    public Payable createPayable(PayableRequest request) {
        TravelOrder order = loadApprovedOrder(request.orderId());
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.payable.amountPositive");

        Payable payable = new Payable();
        payable.setOrderId(request.orderId());
        payable.setItemName(request.itemName());
        payable.setAmount(amount);
        Payable saved = payableRepository.save(payable);
        refreshOrderFinanceState(order.getId(), false);
        return saved;
    }

    @Transactional
    public Payable updatePayable(String payableId, PayableRequest request) {
        Payable payable = payableRepository.findById(payableId)
            .orElseThrow(() -> new BusinessException("error.finance.payable.notFound"));
        ensurePayableEditable(payable);
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.payable.amountPositive");
        if (amount.compareTo(payable.getPaid()) < 0) {
            throw new BusinessException("error.finance.payable.amountLessThanPaid");
        }
        payable.setItemName(request.itemName());
        payable.setAmount(amount);
        return payableRepository.save(payable);
    }

    @Transactional
    public void deletePayable(String payableId) {
        Payable payable = payableRepository.findById(payableId)
            .orElseThrow(() -> new BusinessException("error.finance.payable.notFound"));
        ensurePayableEditable(payable);
        payable.setDeleted(true);
        payableRepository.save(payable);
    }

    @Transactional
    public Payment createPayment(PaymentRequest request) {
        Payable payable = payableRepository.findById(request.payableId())
            .orElseThrow(() -> new BusinessException("error.finance.payable.notFound"));
        BigDecimal amount = normalizeAmount(request.amount());
        requirePositive(amount, "error.finance.payment.amountPositive");

        BigDecimal outstanding = payable.getAmount().subtract(payable.getPaid());
        if (amount.compareTo(outstanding) > 0) {
            throw new BusinessException("error.finance.payment.amountExceedsOutstanding");
        }

        Payment payment = new Payment();
        payment.setPayableId(request.payableId());
        payment.setAmount(amount);
        payment.setRemark(request.remark());
        return paymentRepository.save(payment);
    }

    @Transactional
    public FinanceReview review(String targetId, FinanceReviewRequest request) {
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

    public List<Receivable> receivablesByOrder(String orderId) {
        return receivableRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Payable> payablesByOrder(String orderId) {
        return payableRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Refund> refundsByOrder(String orderId) {
        return refundRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public List<Receipt> receiptsByReceivable(String receivableId) {
        return receiptRepository.findByReceivableIdAndDeletedFalse(receivableId);
    }

    public List<Payment> paymentsByPayable(String payableId) {
        return paymentRepository.findByPayableIdAndDeletedFalse(payableId);
    }

    public List<FinanceOrderOptionView> listOrderOptions(LoginUser user) {
        return listVisibleOrders(user).stream()
            .filter(order -> isFinanceOperable(order.getStatus()))
            .map(order -> new FinanceOrderOptionView(
                order.getId(),
                order.getOrderNo(),
                order.getStatus().name(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency()
            ))
            .toList();
    }

    private void syncFinanceObjectStatus(String targetType, String targetId, boolean approved) {
        if ("RECEIPT".equals(targetType)) {
            Receipt receipt = receiptRepository.findById(targetId).orElseThrow(() -> new BusinessException("error.finance.receipt.notFound"));
            receipt.setStatus(approved ? "APPROVED" : "REJECTED");
            receiptRepository.save(receipt);
            if (approved) {
                Receivable receivable = receivableRepository.findById(receipt.getReceivableId())
                    .orElseThrow(() -> new BusinessException("error.finance.receivable.notFound"));
                BigDecimal outstanding = receivable.getAmount().subtract(receivable.getReceived());
                if (receipt.getAmount().compareTo(outstanding) > 0) {
                    throw new BusinessException("error.finance.receipt.amountExceedsOutstanding");
                }
                receivable.setReceived(receivable.getReceived().add(receipt.getAmount()));
                if (receivable.getReceived().compareTo(receivable.getAmount()) >= 0) {
                    receivable.setStatus("CLOSED");
                }
                receivableRepository.save(receivable);
                refreshOrderFinanceState(receivable.getOrderId(), false);
            }
            return;
        }
        if ("PAYMENT".equals(targetType)) {
            Payment payment = paymentRepository.findById(targetId).orElseThrow(() -> new BusinessException("error.finance.payment.notFound"));
            payment.setStatus(approved ? "APPROVED" : "REJECTED");
            paymentRepository.save(payment);
            if (approved) {
                Payable payable = payableRepository.findById(payment.getPayableId())
                    .orElseThrow(() -> new BusinessException("error.finance.payable.notFound"));
                BigDecimal outstanding = payable.getAmount().subtract(payable.getPaid());
                if (payment.getAmount().compareTo(outstanding) > 0) {
                    throw new BusinessException("error.finance.payment.amountExceedsOutstanding");
                }
                payable.setPaid(payable.getPaid().add(payment.getAmount()));
                if (payable.getPaid().compareTo(payable.getAmount()) >= 0) {
                    payable.setStatus("CLOSED");
                }
                payableRepository.save(payable);
                refreshOrderFinanceState(payable.getOrderId(), false);
            }
            return;
        }
        if ("REFUND".equals(targetType)) {
            Refund refund = refundRepository.findById(targetId).orElseThrow(() -> new BusinessException("error.finance.refund.notFound"));
            refund.setStatus(approved ? "APPROVED" : "REJECTED");
            refundRepository.save(refund);
            if (approved) {
                refreshOrderFinanceState(refund.getOrderId(), true);
            }
        }
    }

    private TravelOrder loadApprovedOrder(String orderId) {
        TravelOrder order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("error.order.notFound"));
        if (!isFinanceOperable(order.getStatus())) {
            throw new BusinessException("error.finance.order.mustBeApproved");
        }
        return order;
    }

    private boolean isFinanceOperable(OrderStatus status) {
        return status == OrderStatus.APPROVED
            || status == OrderStatus.IN_TRAVEL
            || status == OrderStatus.TRAVEL_FINISHED
            || status == OrderStatus.SETTLING
            || status == OrderStatus.COMPLETED;
    }

    private List<TravelOrder> listVisibleOrders(LoginUser user) {
        if (user == null) {
            return List.of();
        }
        if (user.getDataScope() == DataScope.ALL) {
            return orderRepository.findByDeletedFalse();
        }
        if (user.getDataScope() == DataScope.SELF) {
            return orderRepository.findBySalesUserIdAndDeletedFalse(user.getUserId());
        }
        if (user.getDataScope() == DataScope.DEPARTMENT) {
            return orderRepository.findBySalesDeptIdAndDeletedFalse(user.getDepartmentId());
        }
        Set<String> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findBySalesDeptIdInAndDeletedFalse(departmentIds);
    }

    private void refreshOrderFinanceState(String orderId, boolean approvedRefund) {
        TravelOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("error.order.notFound"));
        List<Receivable> receivables = receivableRepository.findByOrderIdAndDeletedFalse(orderId);
        List<Payable> payables = payableRepository.findByOrderIdAndDeletedFalse(orderId);
        List<Refund> refunds = refundRepository.findByOrderIdAndDeletedFalse(orderId);

        BigDecimal receivableAmount = receivables.stream()
            .map(Receivable::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receivedAmount = receivables.stream()
            .map(Receivable::getReceived)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal approvedRefundAmount = refunds.stream()
            .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netReceived = receivedAmount.subtract(approvedRefundAmount);
        if (netReceived.compareTo(BigDecimal.ZERO) < 0) {
            netReceived = BigDecimal.ZERO;
        }

        if (approvedRefund) {
            order.setPaymentStatus(netReceived.compareTo(BigDecimal.ZERO) == 0 ? PaymentStatus.REFUNDED : PaymentStatus.REFUNDING);
        } else if (receivableAmount.compareTo(BigDecimal.ZERO) <= 0 || netReceived.compareTo(BigDecimal.ZERO) <= 0) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        } else if (netReceived.compareTo(receivableAmount) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        boolean hasPositiveCollection = netReceived.compareTo(BigDecimal.ZERO) > 0;
        if (order.getLockPolicy() == OrderLockPolicy.ON_DEPOSIT
            && hasPositiveCollection
            && order.getInventoryStatus() != InventoryStatus.LOCKED
            && order.getStatus() != OrderStatus.CANCELED) {
            lockInventory(order);
        }

        boolean receivableDone = !receivables.isEmpty() && receivables.stream().allMatch(r -> "CLOSED".equalsIgnoreCase(r.getStatus()));
        boolean payableDone = payables.isEmpty() || payables.stream().allMatch(p -> "CLOSED".equalsIgnoreCase(p.getStatus()));
        boolean hasFinanceProgress = receivables.stream().anyMatch(r -> r.getReceived().compareTo(BigDecimal.ZERO) > 0 || "CLOSED".equalsIgnoreCase(r.getStatus()))
            || payables.stream().anyMatch(p -> p.getPaid().compareTo(BigDecimal.ZERO) > 0 || "CLOSED".equalsIgnoreCase(p.getStatus()));

        if (approvedRefund) {
            order.setSettlementStatus(SettlementStatus.PARTIAL);
        } else if (receivableDone && payableDone) {
            order.setSettlementStatus(SettlementStatus.SETTLED);
        } else if (hasFinanceProgress) {
            order.setSettlementStatus(SettlementStatus.PARTIAL);
        } else {
            order.setSettlementStatus(SettlementStatus.UNSETTLED);
        }

        if (order.getSettlementStatus() == SettlementStatus.SETTLED) {
            if (order.getStatus() == OrderStatus.TRAVEL_FINISHED || order.getStatus() == OrderStatus.SETTLING) {
                order.setStatus(OrderStatus.COMPLETED);
                if (order.getCompletedAt() == null) {
                    order.setCompletedAt(java.time.LocalDateTime.now());
                }
            }
        } else {
            if (order.getStatus() == OrderStatus.TRAVEL_FINISHED || order.getStatus() == OrderStatus.COMPLETED) {
                order.setStatus(OrderStatus.SETTLING);
            }
        }

        orderRepository.save(order);
    }

    private void lockInventory(TravelOrder order) {
        if (order.getInventoryStatus() == InventoryStatus.LOCKED) {
            return;
        }
        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        Integer stock = departure.getStock() == null ? 0 : departure.getStock();
        if (stock < order.getTravelerCount()) {
            throw new BusinessException("error.departure.stock.insufficient");
        }
        departure.setStock(stock - order.getTravelerCount());
        departureRepository.save(departure);
        order.setInventoryStatus(InventoryStatus.LOCKED);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("error.finance.amount.required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void requirePositive(BigDecimal amount, String messageKey) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(messageKey);
        }
    }

    private void ensureReceivableEditable(Receivable receivable) {
        if (receivable.getReceived().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("error.finance.receivable.lockedByReceived");
        }
        if (!receiptRepository.findByReceivableIdAndDeletedFalse(receivable.getId()).isEmpty()) {
            throw new BusinessException("error.finance.receivable.lockedByReceipts");
        }
    }

    private void ensurePayableEditable(Payable payable) {
        if (payable.getPaid().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("error.finance.payable.lockedByPaid");
        }
        if (!paymentRepository.findByPayableIdAndDeletedFalse(payable.getId()).isEmpty()) {
            throw new BusinessException("error.finance.payable.lockedByPayments");
        }
    }

    private void ensureRefundEditable(Refund refund) {
        if ("APPROVED".equalsIgnoreCase(refund.getStatus())) {
            throw new BusinessException("error.finance.refund.lockedByApproved");
        }
    }
}


