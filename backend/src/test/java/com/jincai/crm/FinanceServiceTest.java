package com.jincai.crm;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.finance.entity.FinanceReview;
import com.jincai.crm.finance.repository.FinanceReviewRepository;
import com.jincai.crm.finance.dto.FinanceReviewRequest;
import com.jincai.crm.finance.service.FinanceService;
import com.jincai.crm.finance.entity.Payable;
import com.jincai.crm.finance.repository.PayableRepository;
import com.jincai.crm.finance.dto.PayableRequest;
import com.jincai.crm.finance.dto.PaymentRequest;
import com.jincai.crm.finance.repository.PaymentRepository;
import com.jincai.crm.finance.entity.Receivable;
import com.jincai.crm.finance.repository.ReceivableRepository;
import com.jincai.crm.finance.dto.ReceivableRequest;
import com.jincai.crm.finance.entity.Receipt;
import com.jincai.crm.finance.dto.ReceiptRequest;
import com.jincai.crm.finance.repository.ReceiptRepository;
import com.jincai.crm.finance.entity.Refund;
import com.jincai.crm.finance.repository.RefundRepository;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderStatus;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.repository.DepartureRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FinanceServiceTest {

    @Test
    void shouldRejectReceiptWhenAmountExceedsOutstanding() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        ReceivableRepository receivableRepository = Mockito.mock(ReceivableRepository.class);

        Receivable receivable = new Receivable();
        receivable.setId(1L);
        receivable.setAmount(new BigDecimal("100.00"));
        receivable.setReceived(new BigDecimal("90.00"));

        Mockito.when(receivableRepository.findById(1L)).thenReturn(Optional.of(receivable));

        FinanceService service = new FinanceService(
            orderRepository,
            receivableRepository,
            Mockito.mock(ReceiptRepository.class),
            Mockito.mock(RefundRepository.class),
            Mockito.mock(PayableRepository.class),
            Mockito.mock(PaymentRepository.class),
            Mockito.mock(FinanceReviewRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        Assertions.assertThrows(BusinessException.class,
            () -> service.createReceipt(new ReceiptRequest(1L, new BigDecimal("20.00"), "too much")));
    }

    @Test
    void shouldRejectPaymentWhenAmountExceedsOutstanding() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        PayableRepository payableRepository = Mockito.mock(PayableRepository.class);

        Payable payable = new Payable();
        payable.setId(1L);
        payable.setAmount(new BigDecimal("200.00"));
        payable.setPaid(new BigDecimal("150.00"));

        Mockito.when(payableRepository.findById(1L)).thenReturn(Optional.of(payable));

        FinanceService service = new FinanceService(
            orderRepository,
            Mockito.mock(ReceivableRepository.class),
            Mockito.mock(ReceiptRepository.class),
            Mockito.mock(RefundRepository.class),
            payableRepository,
            Mockito.mock(PaymentRepository.class),
            Mockito.mock(FinanceReviewRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        Assertions.assertThrows(BusinessException.class,
            () -> service.createPayment(new PaymentRequest(1L, new BigDecimal("60.00"), "too much")));
    }

    @Test
    void shouldCompleteOrderAfterReceiptApprovedAndNoPayables() {
        TravelOrderRepository orderRepository = Mockito.mock(TravelOrderRepository.class);
        ReceivableRepository receivableRepository = Mockito.mock(ReceivableRepository.class);
        ReceiptRepository receiptRepository = Mockito.mock(ReceiptRepository.class);
        PayableRepository payableRepository = Mockito.mock(PayableRepository.class);
        FinanceReviewRepository reviewRepository = Mockito.mock(FinanceReviewRepository.class);

        TravelOrder order = new TravelOrder();
        order.setId(88L);
        order.setStatus(OrderStatus.SETTLING);
        order.setLockPolicy(OrderLockPolicy.MANUAL);

        Receivable receivable = new Receivable();
        receivable.setId(9L);
        receivable.setOrderId(88L);
        receivable.setAmount(new BigDecimal("100.00"));
        receivable.setReceived(new BigDecimal("50.00"));
        receivable.setStatus("OPEN");

        Receipt receipt = new Receipt();
        receipt.setId(7L);
        receipt.setReceivableId(9L);
        receipt.setAmount(new BigDecimal("50.00"));
        receipt.setStatus("PENDING_REVIEW");

        Mockito.when(reviewRepository.findByTargetTypeAndTargetIdAndDeletedFalse("RECEIPT", 7L))
            .thenReturn(Optional.empty());
        Mockito.when(reviewRepository.save(Mockito.any(FinanceReview.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(receiptRepository.findById(7L)).thenReturn(Optional.of(receipt));
        Mockito.when(receiptRepository.save(Mockito.any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(receivableRepository.findById(9L)).thenReturn(Optional.of(receivable));
        Mockito.when(receivableRepository.save(Mockito.any(Receivable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(receivableRepository.findByOrderIdAndDeletedFalse(88L)).thenReturn(List.of(receivable));

        Mockito.when(payableRepository.findByOrderIdAndDeletedFalse(88L)).thenReturn(List.of());

        Mockito.when(orderRepository.findById(88L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(Mockito.any(TravelOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinanceService service = new FinanceService(
            orderRepository,
            receivableRepository,
            receiptRepository,
            Mockito.mock(RefundRepository.class),
            payableRepository,
            Mockito.mock(PaymentRepository.class),
            reviewRepository,
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        service.review(7L, new FinanceReviewRequest("RECEIPT", true, "ok"));

        Assertions.assertEquals("APPROVED", receipt.getStatus());
        Assertions.assertEquals(new BigDecimal("100.00"), receivable.getReceived());
        Assertions.assertEquals("CLOSED", receivable.getStatus());
        Assertions.assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldRejectUpdatingReceivableBelowReceivedAmount() {
        ReceivableRepository receivableRepository = Mockito.mock(ReceivableRepository.class);

        Receivable receivable = new Receivable();
        receivable.setId(11L);
        receivable.setAmount(new BigDecimal("100.00"));
        receivable.setReceived(new BigDecimal("80.00"));

        Mockito.when(receivableRepository.findById(11L)).thenReturn(Optional.of(receivable));

        FinanceService service = new FinanceService(
            Mockito.mock(TravelOrderRepository.class),
            receivableRepository,
            Mockito.mock(ReceiptRepository.class),
            Mockito.mock(RefundRepository.class),
            Mockito.mock(PayableRepository.class),
            Mockito.mock(PaymentRepository.class),
            Mockito.mock(FinanceReviewRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        Assertions.assertThrows(BusinessException.class,
            () -> service.updateReceivable(11L, new ReceivableRequest("updated receivable", new BigDecimal("79.99"))));
    }

    @Test
    void shouldRejectUpdatingPayableBelowPaidAmount() {
        PayableRepository payableRepository = Mockito.mock(PayableRepository.class);

        Payable payable = new Payable();
        payable.setId(12L);
        payable.setAmount(new BigDecimal("200.00"));
        payable.setPaid(new BigDecimal("150.00"));

        Mockito.when(payableRepository.findById(12L)).thenReturn(Optional.of(payable));

        FinanceService service = new FinanceService(
            Mockito.mock(TravelOrderRepository.class),
            Mockito.mock(ReceivableRepository.class),
            Mockito.mock(ReceiptRepository.class),
            Mockito.mock(RefundRepository.class),
            payableRepository,
            Mockito.mock(PaymentRepository.class),
            Mockito.mock(FinanceReviewRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        Assertions.assertThrows(BusinessException.class,
            () -> service.updatePayable(12L, new PayableRequest(88L, "updated payable", new BigDecimal("149.99"))));
    }

    @Test
    void shouldRejectDeletingApprovedRefund() {
        RefundRepository refundRepository = Mockito.mock(RefundRepository.class);

        Refund refund = new Refund();
        refund.setId(13L);
        refund.setOrderId(66L);
        refund.setAmount(new BigDecimal("50.00"));
        refund.setStatus("APPROVED");

        Mockito.when(refundRepository.findById(13L)).thenReturn(Optional.of(refund));

        FinanceService service = new FinanceService(
            Mockito.mock(TravelOrderRepository.class),
            Mockito.mock(ReceivableRepository.class),
            Mockito.mock(ReceiptRepository.class),
            refundRepository,
            Mockito.mock(PayableRepository.class),
            Mockito.mock(PaymentRepository.class),
            Mockito.mock(FinanceReviewRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(DataScopeResolver.class)
        );

        Assertions.assertThrows(BusinessException.class, () -> service.deleteRefund(13L));
        Mockito.verify(refundRepository, Mockito.never()).save(Mockito.any(Refund.class));
    }
}

