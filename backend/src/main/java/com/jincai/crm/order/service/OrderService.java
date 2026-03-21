package com.jincai.crm.order.service;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.*;

import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.workflow.service.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderService {

    private static final String CNY = "CNY";

    private final TravelOrderRepository orderRepository;
    private final OrderStatusLogRepository logRepository;
    private final WorkflowService workflowService;
    private final DataScopeResolver dataScopeResolver;
    private final AuditLogService auditLogService;
    private final CustomerRepository customerRepository;
    private final TravelerRepository travelerRepository;
    private final RouteProductRepository routeRepository;
    private final DepartureRepository departureRepository;
    private final DeparturePriceRepository priceRepository;
    private final OrderTravelerSnapshotRepository travelerSnapshotRepository;
    private final OrderPriceItemRepository orderPriceItemRepository;

    public OrderService(TravelOrderRepository orderRepository, OrderStatusLogRepository logRepository,
                        WorkflowService workflowService, DataScopeResolver dataScopeResolver,
                        AuditLogService auditLogService, CustomerRepository customerRepository,
                        TravelerRepository travelerRepository, RouteProductRepository routeRepository,
                        DepartureRepository departureRepository, DeparturePriceRepository priceRepository,
                        OrderTravelerSnapshotRepository travelerSnapshotRepository,
                        OrderPriceItemRepository orderPriceItemRepository) {
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.workflowService = workflowService;
        this.dataScopeResolver = dataScopeResolver;
        this.auditLogService = auditLogService;
        this.customerRepository = customerRepository;
        this.travelerRepository = travelerRepository;
        this.routeRepository = routeRepository;
        this.departureRepository = departureRepository;
        this.priceRepository = priceRepository;
        this.travelerSnapshotRepository = travelerSnapshotRepository;
        this.orderPriceItemRepository = orderPriceItemRepository;
    }

    public List<TravelOrder> listVisible(LoginUser user) {
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
        Set<Long> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findBySalesDeptIdInAndDeletedFalse(departmentIds);
    }

    public OrderDetailView detail(Long id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        return new OrderDetailView(
            order,
            travelerSnapshotRepository.findByOrderIdAndDeletedFalse(id),
            orderPriceItemRepository.findByOrderIdAndDeletedFalse(id)
        );
    }

    public List<OrderStatusLog> logs(Long orderId) {
        return logRepository.findByOrderIdAndDeletedFalseOrderByCreatedAtAsc(orderId);
    }

    public OrderQuoteView quote(OrderRequest request) {
        PricingCalculation calculation = calculatePricing(request);
        return new OrderQuoteView(
            calculation.route().getId(),
            calculation.departure().getId(),
            calculation.travelerSnapshots().size(),
            calculation.totalAmount(),
            calculation.currency(),
            calculation.travelerSnapshots(),
            calculation.priceItems()
        );
    }

    @Transactional
    public TravelOrder create(OrderRequest request, LoginUser user) {
        if (user == null) {
            throw new BusinessException("Unauthenticated");
        }
        assertCnyCurrency(request.currency());
        TravelOrder order = new TravelOrder();
        order.setOrderNo(request.orderNo() == null || request.orderNo().isBlank() ? generateOrderNo() : request.orderNo());
        order.setCustomerId(request.customerId());
        order.setRouteId(request.routeId());
        order.setDepartureId(request.departureId());
        order.setOrderType(request.orderType());
        order.setCurrency(CNY);
        order.setSalesUserId(user.getUserId());
        order.setSalesDeptId(user.getDepartmentId());

        PricingCalculation calculation = applyOrderAmounts(order, request);
        TravelOrder saved = orderRepository.save(order);
        persistPricing(saved.getId(), calculation);
        addStatusLog(saved.getId(), null, OrderStatus.DRAFT.name(), "Order created");
        return saved;
    }

    @Transactional
    public TravelOrder update(Long id, OrderRequest request, HttpServletRequest httpServletRequest) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        validateEditable(order);
        assertCnyCurrency(request.currency());
        Map<String, Object> before = Map.of(
            "travelerCount", order.getTravelerCount(),
            "totalAmount", order.getTotalAmount(),
            "departureId", order.getDepartureId()
        );
        order.setCustomerId(request.customerId());
        order.setRouteId(request.routeId());
        order.setDepartureId(request.departureId());
        order.setOrderType(request.orderType());
        order.setCurrency(CNY);

        PricingCalculation calculation = applyOrderAmounts(order, request);
        TravelOrder saved = orderRepository.save(order);
        replacePricing(saved.getId(), calculation);
        Map<String, Object> after = Map.of(
            "travelerCount", saved.getTravelerCount(),
            "totalAmount", saved.getTotalAmount(),
            "departureId", saved.getDepartureId()
        );
        auditLogService.logDiff("ORDER", saved.getId(), before, after, httpServletRequest.getRemoteAddr());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        validateEditable(order);
        order.setDeleted(true);
        orderRepository.save(order);
        markPricingDeleted(id);
    }

    @Transactional
    public TravelOrder submit(Long id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.REJECTED) {
            throw new BusinessException("Only DRAFT/REJECTED order can be submitted");
        }
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setSubmittedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);
        workflowService.startWorkflow(saved);
        addStatusLog(saved.getId(), oldStatus.name(), OrderStatus.PENDING_APPROVAL.name(), "Order submitted");
        return saved;
    }

    @Transactional
    public TravelOrder approve(Long id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("Order is not pending approval");
        }
        boolean finalApproved = workflowService.approve(id, user, comment);
        if (finalApproved) {
            OrderStatus old = order.getStatus();
            order.setStatus(OrderStatus.APPROVED);
            order.setApprovedAt(LocalDateTime.now());
            orderRepository.save(order);
            addStatusLog(order.getId(), old.name(), OrderStatus.APPROVED.name(), comment == null ? "Approved" : comment);
        }
        return order;
    }

    @Transactional
    public TravelOrder reject(Long id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("Order is not pending approval");
        }
        workflowService.reject(id, user, comment);
        OrderStatus old = order.getStatus();
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
        addStatusLog(order.getId(), old.name(), OrderStatus.REJECTED.name(), comment == null ? "Rejected" : comment);
        return order;
    }

    public ImportOrderResult importOrders(MultipartFile file, LoginUser user) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        try (var input = file.getInputStream(); var workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    Long customerId = Long.parseLong(formatter.formatCellValue(row.getCell(0)));
                    Long routeId = Long.parseLong(formatter.formatCellValue(row.getCell(1)));
                    Long departureId = Long.parseLong(formatter.formatCellValue(row.getCell(2)));
                    String orderType = formatter.formatCellValue(row.getCell(3));
                    String productCategory = formatter.formatCellValue(row.getCell(4));
                    int travelerCount = Integer.parseInt(formatter.formatCellValue(row.getCell(5)));
                    BigDecimal totalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(6)));
                    create(new OrderRequest(null, customerId, routeId, departureId, orderType, productCategory, travelerCount,
                        totalAmount, "CNY", List.of()), user);
                    success++;
                } catch (Exception ex) {
                    errors.add("Row " + (i + 1) + " failed: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("Failed to parse file: " + ex.getMessage());
        }
        return new ImportOrderResult(success, errors.size(), errors);
    }

    private PricingCalculation applyOrderAmounts(TravelOrder order, OrderRequest request) {
        if (request.priceSelections() != null && !request.priceSelections().isEmpty()) {
            PricingCalculation calculation = calculatePricing(request);
            order.setProductCategory(calculation.route().getCategory());
            order.setTravelerCount(calculation.travelerSnapshots().size());
            order.setTotalAmount(calculation.totalAmount());
            order.setCurrency(calculation.currency());
            return calculation;
        }

        RouteProduct route = routeRepository.findById(request.routeId())
            .orElseThrow(() -> new BusinessException("Route not found"));
        Departure departure = departureRepository.findById(request.departureId())
            .orElseThrow(() -> new BusinessException("Departure not found"));
        if (!Objects.equals(departure.getRouteId(), request.routeId())) {
            throw new BusinessException("Departure does not belong to selected route");
        }
        if (request.travelerCount() == null || request.travelerCount() < 1) {
            throw new BusinessException("Traveler count is required");
        }
        if (request.totalAmount() == null) {
            throw new BusinessException("Total amount is required");
        }
        validateGroupSize(departure, request.travelerCount());
        ensureStockAvailable(departure, request.travelerCount());
        order.setProductCategory(route.getCategory());
        order.setTravelerCount(request.travelerCount());
        order.setTotalAmount(request.totalAmount().setScale(2, RoundingMode.HALF_UP));
        order.setCurrency(CNY);
        return null;
    }

    private PricingCalculation calculatePricing(OrderRequest request) {
        customerRepository.findById(request.customerId())
            .orElseThrow(() -> new BusinessException("Customer not found"));
        RouteProduct route = routeRepository.findById(request.routeId())
            .orElseThrow(() -> new BusinessException("Route not found"));
        Departure departure = departureRepository.findById(request.departureId())
            .orElseThrow(() -> new BusinessException("Departure not found"));
        if (!Objects.equals(departure.getRouteId(), route.getId())) {
            throw new BusinessException("Departure does not belong to selected route");
        }
        if (request.priceSelections() == null || request.priceSelections().isEmpty()) {
            throw new BusinessException("Price selections are required");
        }

        Map<Long, OrderTravelerSnapshot> travelerSnapshots = new LinkedHashMap<>();
        List<OrderPriceItem> priceItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = null;

        for (OrderPriceSelectionRequest selection : request.priceSelections()) {
            DeparturePrice departurePrice = priceRepository.findById(selection.departurePriceId())
                .orElseThrow(() -> new BusinessException("Departure price not found"));
            if (!Objects.equals(departurePrice.getDepartureId(), departure.getId())) {
                throw new BusinessException("Price item does not belong to selected departure");
            }
            if (departurePrice.getPrice() == null || departurePrice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Departure price must be greater than 0");
            }
            String itemCurrency = normalizeCurrency(departurePrice.getCurrency());
            if (!CNY.equals(itemCurrency)) {
                throw new BusinessException("Only CNY is supported in current version");
            }
            if (currency == null) {
                currency = itemCurrency;
            } else if (!Objects.equals(currency, itemCurrency)) {
                throw new BusinessException("Mixed currencies are not supported in one order");
            }

            Integer quantity = selection.quantity() == null ? 1 : selection.quantity();
            if (quantity < 1) {
                throw new BusinessException("Item quantity must be greater than 0");
            }

            Long travelerId = selection.travelerId();
            String travelerName = null;
            if (travelerId != null) {
                Traveler traveler = travelerRepository.findById(travelerId)
                    .orElseThrow(() -> new BusinessException("Traveler not found"));
                if (!Objects.equals(traveler.getCustomerId(), request.customerId())) {
                    throw new BusinessException("Traveler does not belong to selected customer");
                }
                travelerName = traveler.getName();
                quantity = 1;
                travelerSnapshots.computeIfAbsent(travelerId, key -> buildTravelerSnapshot(traveler));
            }

            OrderPriceItem item = new OrderPriceItem();
            item.setTravelerId(travelerId);
            item.setTravelerName(travelerName);
            item.setDeparturePriceId(departurePrice.getId());
            item.setPriceType(departurePrice.getPriceType());
            item.setItemName(departurePrice.getPriceLabel() == null || departurePrice.getPriceLabel().isBlank()
                ? departurePrice.getPriceType()
                : departurePrice.getPriceLabel());
            item.setUnitPrice(departurePrice.getPrice().setScale(2, RoundingMode.HALF_UP));
            item.setQuantity(quantity);
            item.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            item.setCurrency(itemCurrency);
            priceItems.add(item);
            totalAmount = totalAmount.add(item.getAmount());
        }

        if (travelerSnapshots.isEmpty()) {
            throw new BusinessException("At least one traveler must be selected");
        }
        validateGroupSize(departure, travelerSnapshots.size());
        ensureStockAvailable(departure, travelerSnapshots.size());

        return new PricingCalculation(
            route,
            departure,
            new ArrayList<>(travelerSnapshots.values()),
            priceItems,
            totalAmount.setScale(2, RoundingMode.HALF_UP),
            currency == null || currency.isBlank() ? CNY : currency
        );
    }

    private OrderTravelerSnapshot buildTravelerSnapshot(Traveler traveler) {
        OrderTravelerSnapshot snapshot = new OrderTravelerSnapshot();
        snapshot.setTravelerId(traveler.getId());
        snapshot.setName(traveler.getName());
        snapshot.setIdType(traveler.getIdType());
        snapshot.setIdNo(traveler.getIdNo());
        snapshot.setPhone(traveler.getPhone());
        return snapshot;
    }

    private void validateGroupSize(Departure departure, int travelerCount) {
        if (departure.getMinGroupSize() != null && travelerCount < departure.getMinGroupSize()) {
            throw new BusinessException("Traveler count does not meet departure minimum group size");
        }
        if (departure.getMaxGroupSize() != null && travelerCount > departure.getMaxGroupSize()) {
            throw new BusinessException("Traveler count exceeds departure maximum group size");
        }
    }

    private void ensureStockAvailable(Departure departure, int travelerCount) {
        if (departure.getStock() != null && travelerCount > departure.getStock()) {
            throw new BusinessException("Traveler count exceeds departure stock");
        }
    }

    private void persistPricing(Long orderId, PricingCalculation calculation) {
        if (calculation == null) {
            return;
        }
        for (OrderTravelerSnapshot snapshot : calculation.travelerSnapshots()) {
            snapshot.setOrderId(orderId);
            travelerSnapshotRepository.save(snapshot);
        }
        for (OrderPriceItem item : calculation.priceItems()) {
            item.setOrderId(orderId);
            orderPriceItemRepository.save(item);
        }
    }

    private void replacePricing(Long orderId, PricingCalculation calculation) {
        markPricingDeleted(orderId);
        persistPricing(orderId, calculation);
    }

    private void markPricingDeleted(Long orderId) {
        for (OrderTravelerSnapshot snapshot : travelerSnapshotRepository.findByOrderIdAndDeletedFalse(orderId)) {
            snapshot.setDeleted(true);
            travelerSnapshotRepository.save(snapshot);
        }
        for (OrderPriceItem item : orderPriceItemRepository.findByOrderIdAndDeletedFalse(orderId)) {
            item.setDeleted(true);
            orderPriceItemRepository.save(item);
        }
    }

    private void addStatusLog(Long orderId, String fromStatus, String toStatus, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(fromStatus == null ? "-" : fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(remark);
        logRepository.save(log);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void validateEditable(TravelOrder order) {
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.REJECTED) {
            throw new BusinessException("Only draft or rejected orders can be edited or deleted");
        }
    }

    private void assertCnyCurrency(String requestCurrency) {
        if (requestCurrency == null || requestCurrency.isBlank()) {
            return;
        }
        String normalized = requestCurrency.trim().toUpperCase(Locale.ROOT);
        if (!CNY.equals(normalized)) {
            throw new BusinessException("Only CNY is supported in current version");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return CNY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private record PricingCalculation(
        RouteProduct route,
        Departure departure,
        List<OrderTravelerSnapshot> travelerSnapshots,
        List<OrderPriceItem> priceItems,
        BigDecimal totalAmount,
        String currency
    ) {
    }
}

