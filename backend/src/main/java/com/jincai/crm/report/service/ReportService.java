package com.jincai.crm.report.service;

import com.jincai.crm.report.controller.*;

import com.jincai.crm.finance.entity.Payable;
import com.jincai.crm.finance.repository.PayableRepository;
import com.jincai.crm.finance.entity.Payment;
import com.jincai.crm.finance.repository.PaymentRepository;
import com.jincai.crm.finance.entity.Receivable;
import com.jincai.crm.finance.repository.ReceivableRepository;
import com.jincai.crm.finance.entity.Receipt;
import com.jincai.crm.finance.repository.ReceiptRepository;
import com.jincai.crm.order.entity.OrderStatus;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.RouteProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final TravelOrderRepository orderRepository;
    private final ReceivableRepository receivableRepository;
    private final ReceiptRepository receiptRepository;
    private final PayableRepository payableRepository;
    private final PaymentRepository paymentRepository;
    private final RouteProductRepository routeRepository;

    public ReportService(TravelOrderRepository orderRepository, ReceivableRepository receivableRepository,
                         ReceiptRepository receiptRepository, PayableRepository payableRepository,
                         PaymentRepository paymentRepository, RouteProductRepository routeRepository) {
        this.orderRepository = orderRepository;
        this.receivableRepository = receivableRepository;
        this.receiptRepository = receiptRepository;
        this.payableRepository = payableRepository;
        this.paymentRepository = paymentRepository;
        this.routeRepository = routeRepository;
    }

    public Map<String, Object> salesFunnel() {
        List<TravelOrder> orders = orderRepository.findByDeletedFalse();
        long total = orders.size();
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_APPROVAL).count();
        long approved = orders.stream().filter(o -> o.getStatus() == OrderStatus.APPROVED || o.getStatus() == OrderStatus.FINANCE_IN_PROGRESS || o.getStatus() == OrderStatus.COMPLETED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        BigDecimal conversionRate = total == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

        return Map.of(
            "totalOrders", total,
            "pendingApproval", pending,
            "approvedOrders", approved,
            "completedOrders", completed,
            "conversionRate", conversionRate
        );
    }

    public Map<String, Object> cashflowAging() {
        List<Receivable> receivables = receivableRepository.findByDeletedFalse();
        List<Payable> payables = payableRepository.findByDeletedFalse();

        Map<String, BigDecimal> receivableAging = new LinkedHashMap<>();
        receivableAging.put("0-30", BigDecimal.ZERO);
        receivableAging.put("31-60", BigDecimal.ZERO);
        receivableAging.put("61-90", BigDecimal.ZERO);
        receivableAging.put("90+", BigDecimal.ZERO);

        for (Receivable receivable : receivables) {
            BigDecimal outstanding = receivable.getAmount().subtract(receivable.getReceived());
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            long days = Duration.between(receivable.getCreatedAt(), LocalDateTime.now()).toDays();
            String bucket = days <= 30 ? "0-30" : days <= 60 ? "31-60" : days <= 90 ? "61-90" : "90+";
            receivableAging.put(bucket, receivableAging.get(bucket).add(outstanding));
        }

        BigDecimal totalPayableOutstanding = payables.stream()
            .map(p -> p.getAmount().subtract(p.getPaid()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
            "receivableAging", receivableAging,
            "payableOutstanding", totalPayableOutstanding
        );
    }

    public List<Map<String, Object>> profit() {
        List<TravelOrder> orders = orderRepository.findByDeletedFalse();
        Map<Long, String> routeNameMap = routeRepository.findByDeletedFalse().stream()
            .collect(java.util.stream.Collectors.toMap(RouteProduct::getId, RouteProduct::getName));

        return orders.stream()
            .collect(java.util.stream.Collectors.groupingBy(TravelOrder::getRouteId))
            .entrySet()
            .stream()
            .map(entry -> {
                Long routeId = entry.getKey();
                List<TravelOrder> routeOrders = entry.getValue();

                BigDecimal income = routeOrders.stream()
                    .map(order -> receivableRepository.findByOrderIdAndDeletedFalse(order.getId()))
                    .flatMap(List::stream)
                    .map(Receivable::getReceived)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal cost = routeOrders.stream()
                    .map(order -> payableRepository.findByOrderIdAndDeletedFalse(order.getId()))
                    .flatMap(List::stream)
                    .map(Payable::getPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return Map.<String, Object>of(
                    "routeId", routeId,
                    "routeName", routeNameMap.getOrDefault(routeId, "Unknown"),
                    "income", income,
                    "cost", cost,
                    "grossProfit", income.subtract(cost)
                );
            })
            .toList();
    }
}

