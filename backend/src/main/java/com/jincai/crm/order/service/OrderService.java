package com.jincai.crm.order.service;

import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.*;
import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.OrderPriceItemRepository;
import com.jincai.crm.order.repository.OrderStatusLogRepository;
import com.jincai.crm.order.repository.OrderTravelerSnapshotRepository;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.DeparturePrice;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.DeparturePriceRepository;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.workflow.service.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final I18nService i18nService;

    public OrderService(TravelOrderRepository orderRepository, OrderStatusLogRepository logRepository,
                        WorkflowService workflowService, DataScopeResolver dataScopeResolver,
                        AuditLogService auditLogService, CustomerRepository customerRepository,
                        TravelerRepository travelerRepository, RouteProductRepository routeRepository,
                        DepartureRepository departureRepository, DeparturePriceRepository priceRepository,
                        OrderTravelerSnapshotRepository travelerSnapshotRepository,
                        OrderPriceItemRepository orderPriceItemRepository, I18nService i18nService) {
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
        this.i18nService = i18nService;
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
        Set<String> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findBySalesDeptIdInAndDeletedFalse(departmentIds);
    }

    public PageResult<TravelOrder> pageVisible(LoginUser user, int page, int size, String keyword, String status, Long customerId) {
        if (user == null) {
            return new PageResult<>(List.of(), 0, normalizePage(page), normalizeSize(size));
        }
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        Specification<TravelOrder> spec = buildOrderSpec(user, keyword, status, customerId);
        Page<TravelOrder> result = orderRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        return new PageResult<>(result.getContent(), result.getTotalElements(), normalizedPage, normalizedSize);
    }

    public OrderContextOptionsView contextOptions(LoginUser user) {
        return new OrderContextOptionsView(
            listVisibleCustomers(user),
            routeRepository.findByDeletedFalse(),
            departureRepository.findByDeletedFalse()
        );
    }

    public OrderDetailView detail(String id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        return new OrderDetailView(
            order,
            travelerSnapshotRepository.findByOrderIdAndDeletedFalse(id),
            orderPriceItemRepository.findByOrderIdAndDeletedFalse(id)
        );
    }

    public List<OrderStatusLog> logs(String orderId) {
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
            throw new BusinessException("error.auth.unauthenticated");
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
        applyPolicy(order, resolvePolicy(request.routeId(), request.departureId()), true);
        TravelOrder saved = orderRepository.save(order);
        persistPricing(saved.getId(), calculation);
        addStatusLog(saved.getId(), null, OrderStatus.DRAFT.name(), "Order created");
        return saved;
    }

    @Transactional
    public TravelOrder update(String id, OrderRequest request, HttpServletRequest httpServletRequest) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
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
        applyPolicy(order, resolvePolicy(request.routeId(), request.departureId()), false);
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
    public void delete(String id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        validateEditable(order);
        order.setDeleted(true);
        orderRepository.save(order);
        markPricingDeleted(id);
    }

    public List<Traveler> customerTravelers(String customerId, LoginUser user) {
        if (customerId == null) {
            return List.of();
        }
        boolean visible = listVisibleCustomers(user).stream().anyMatch(customer -> Objects.equals(customer.getId(), customerId));
        if (!visible) {
            throw new BusinessException("common.auth.forbidden");
        }
        return travelerRepository.findByCustomerIdAndDeletedFalse(customerId);
    }

    public List<DeparturePrice> departurePrices(String departureId) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        return priceRepository.findByDepartureIdAndDeletedFalse(departureId);
    }

    @Transactional
    public TravelOrder action(String id, OrderActionRequest request, LoginUser user) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        OrderStatus fromStatus = order.getStatus();

        switch (request.action()) {
            case SUBMIT -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                submitInternal(order, false);
            }
            case RESUBMIT -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                submitInternal(order, true);
            }
            case APPROVE -> {
                requireAnyPermission(user, "BTN_ORDER_APPROVE");
                approveInternal(order, user, request.comment());
            }
            case REJECT -> {
                requireAnyPermission(user, "BTN_ORDER_REJECT");
                rejectInternal(order, user, request.comment());
            }
            case WITHDRAW -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                withdrawInternal(order, user, request.comment());
            }
            case TRANSFER -> {
                requireAnyPermission(user, "BTN_ORDER_APPROVE");
                workflowService.transfer(order.getId(), user, request.targetRoleCode(), request.comment());
            }
            case SIGN_CONTRACT -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                signContractInternal(order);
            }
            case LOCK_INVENTORY -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                lockInventory(order);
            }
            case UNLOCK_INVENTORY -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                releaseInventory(order);
            }
            case MARK_IN_TRAVEL -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                markInTravelInternal(order);
            }
            case MARK_TRAVEL_FINISHED -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                markTravelFinishedInternal(order);
            }
            case CANCEL, AUTO_CANCEL -> {
                requireAnyPermission(user, "BTN_ORDER_DELETE");
                cancelInternal(order, user);
            }
            default -> throw new BusinessException("error.order.action.unsupported");
        }

        TravelOrder saved = orderRepository.save(order);
        addStatusLog(saved.getId(), fromStatus.name(), saved.getStatus().name(),
            request.comment() == null || request.comment().isBlank() ? request.action().name() : request.comment());
        return saved;
    }

    @Transactional
    public TravelOrder submit(String id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        OrderStatus oldStatus = order.getStatus();
        submitInternal(order, false);
        TravelOrder saved = orderRepository.save(order);
        addStatusLog(saved.getId(), oldStatus.name(), saved.getStatus().name(), "Order submitted");
        return saved;
    }

    @Transactional
    public TravelOrder approve(String id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        OrderStatus old = order.getStatus();
        approveInternal(order, user, comment);
        TravelOrder saved = orderRepository.save(order);
        addStatusLog(saved.getId(), old.name(), saved.getStatus().name(), comment == null ? "Approved" : comment);
        return saved;
    }

    @Transactional
    public TravelOrder reject(String id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        OrderStatus old = order.getStatus();
        rejectInternal(order, user, comment);
        TravelOrder saved = orderRepository.save(order);
        addStatusLog(saved.getId(), old.name(), saved.getStatus().name(), comment == null ? "Rejected" : comment);
        return saved;
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
                    String customerId = formatter.formatCellValue(row.getCell(0));
                    String routeId = formatter.formatCellValue(row.getCell(1));
                    String departureId = formatter.formatCellValue(row.getCell(2));
                    String orderType = formatter.formatCellValue(row.getCell(3));
                    String productCategory = formatter.formatCellValue(row.getCell(4));
                    int travelerCount = Integer.parseInt(formatter.formatCellValue(row.getCell(5)));
                    BigDecimal totalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(6)));
                    create(new OrderRequest(null, customerId, routeId, departureId, orderType, productCategory, travelerCount,
                        totalAmount, "CNY", List.of()), user);
                    success++;
                } catch (Exception ex) {
                    errors.add(i18nService.getMessage("error.import.rowFailed", i + 1, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("error.file.parseFailed", ex.getMessage());
        }
        return new ImportOrderResult(success, errors.size(), errors);
    }

    @Transactional
    public int autoCancelOverdueOrders() {
        List<TravelOrder> candidates = orderRepository.findByStatusInAndDeletedFalse(List.of(OrderStatus.APPROVED));
        if (candidates.isEmpty()) {
            return 0;
        }

        LoginUser systemOperator = new LoginUser("1", "1", DataScope.ALL, "system", "N/A", true, List.of("ADMIN"));
        LocalDateTime now = LocalDateTime.now();
        int canceledCount = 0;
        for (TravelOrder order : candidates) {
            if (!shouldAutoCancel(order, now)) {
                continue;
            }
            OrderStatus from = order.getStatus();
            cancelInternal(order, systemOperator);
            TravelOrder saved = orderRepository.save(order);
            addStatusLog(saved.getId(), from.name(), saved.getStatus().name(), "AUTO_CANCEL_OVERDUE");
            canceledCount++;
        }
        return canceledCount;
    }

    private boolean shouldAutoCancel(TravelOrder order, LocalDateTime now) {
        if (order.getStatus() != OrderStatus.APPROVED) {
            return false;
        }
        if (order.getApprovedAt() == null) {
            return false;
        }
        Integer autoCancelHours = order.getAutoCancelHours();
        if (autoCancelHours == null || autoCancelHours <= 0) {
            return false;
        }
        if (order.getApprovedAt().plusHours(autoCancelHours).isAfter(now)) {
            return false;
        }

        boolean contractOverdue = Boolean.TRUE.equals(order.getContractRequired())
            && order.getContractStatus() != ContractStatus.SIGNED;
        OrderPaymentPolicy policy = order.getPaymentPolicy() == null ? OrderPaymentPolicy.DEPOSIT_BALANCE : order.getPaymentPolicy();
        PaymentStatus paymentStatus = order.getPaymentStatus() == null ? PaymentStatus.UNPAID : order.getPaymentStatus();
        boolean paymentOverdue;
        if (policy == OrderPaymentPolicy.FULL) {
            paymentOverdue = paymentStatus == PaymentStatus.UNPAID || paymentStatus == PaymentStatus.PARTIAL;
        } else {
            paymentOverdue = paymentStatus == PaymentStatus.UNPAID;
        }
        return contractOverdue || paymentOverdue;
    }

    private void submitInternal(TravelOrder order, boolean resubmitOnly) {
        if (resubmitOnly) {
            if (order.getStatus() != OrderStatus.REJECTED) {
                throw new BusinessException("error.order.submit.invalidStatus");
            }
        } else if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.REJECTED) {
            throw new BusinessException("error.order.submit.invalidStatus");
        }
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setSubmittedAt(LocalDateTime.now());
        workflowService.startWorkflow(order);
    }

    private void approveInternal(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.review.invalidStatus");
        }
        boolean finalApproved = workflowService.approve(order.getId(), user, comment);
        if (!finalApproved) {
            return;
        }
        order.setStatus(OrderStatus.APPROVED);
        order.setApprovedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(order.getContractRequired())) {
            if (order.getContractStatus() != ContractStatus.SIGNED) {
                order.setContractStatus(ContractStatus.PENDING_SIGN);
            }
        } else {
            order.setContractStatus(ContractStatus.NOT_REQUIRED);
        }
        if (order.getLockPolicy() == OrderLockPolicy.ON_APPROVAL) {
            lockInventory(order);
        }
    }

    private void rejectInternal(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.review.invalidStatus");
        }
        workflowService.reject(order.getId(), user, comment);
        order.setStatus(OrderStatus.REJECTED);
    }

    private void withdrawInternal(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.review.invalidStatus");
        }
        workflowService.withdraw(order.getId(), user, comment);
        order.setStatus(OrderStatus.DRAFT);
    }

    private void signContractInternal(TravelOrder order) {
        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.SETTLING) {
            throw new BusinessException("error.order.contract.sign.invalidStatus");
        }
        if (!Boolean.TRUE.equals(order.getContractRequired())) {
            order.setContractStatus(ContractStatus.NOT_REQUIRED);
            return;
        }
        order.setContractStatus(ContractStatus.SIGNED);
        order.setContractSignedAt(LocalDateTime.now());
    }

    private void markInTravelInternal(TravelOrder order) {
        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.SETTLING) {
            throw new BusinessException("error.order.travel.start.invalidStatus");
        }
        if (Boolean.TRUE.equals(order.getContractRequired()) && order.getContractStatus() != ContractStatus.SIGNED) {
            throw new BusinessException("error.order.travel.contractNotSigned");
        }
        if (order.getInventoryStatus() != InventoryStatus.LOCKED) {
            throw new BusinessException("error.order.travel.inventoryNotLocked");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("error.order.travel.paymentNotCompleted");
        }
        order.setStatus(OrderStatus.IN_TRAVEL);
        order.setTravelStartedAt(LocalDateTime.now());
    }

    private void markTravelFinishedInternal(TravelOrder order) {
        if (order.getStatus() != OrderStatus.IN_TRAVEL) {
            throw new BusinessException("error.order.travel.finish.invalidStatus");
        }
        order.setTravelFinishedAt(LocalDateTime.now());
        if (order.getSettlementStatus() == SettlementStatus.SETTLED) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
        } else {
            order.setStatus(OrderStatus.SETTLING);
        }
    }

    private void cancelInternal(TravelOrder order, LoginUser user) {
        if (order.getStatus() == OrderStatus.CANCELED
            || order.getStatus() == OrderStatus.COMPLETED
            || order.getStatus() == OrderStatus.IN_TRAVEL) {
            throw new BusinessException("error.order.cancel.invalidStatus");
        }
        if (order.getStatus() == OrderStatus.PENDING_APPROVAL) {
            workflowService.withdraw(order.getId(), user, "Canceled by operator");
        }
        releaseInventory(order);
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
    }

    private void lockInventory(TravelOrder order) {
        if (order.getInventoryStatus() == InventoryStatus.LOCKED) {
            return;
        }
        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        if (departure.getStock() == null || departure.getStock() < order.getTravelerCount()) {
            throw new BusinessException("error.departure.stock.insufficient");
        }
        departure.setStock(departure.getStock() - order.getTravelerCount());
        departureRepository.save(departure);
        order.setInventoryStatus(InventoryStatus.LOCKED);
    }

    private void releaseInventory(TravelOrder order) {
        if (order.getInventoryStatus() != InventoryStatus.LOCKED) {
            return;
        }
        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        departure.setStock((departure.getStock() == null ? 0 : departure.getStock()) + order.getTravelerCount());
        departureRepository.save(departure);
        order.setInventoryStatus(InventoryStatus.RELEASED);
    }

    private void requireAnyPermission(LoginUser user, String... permissionCodes) {
        if (user == null || user.getPermissionCodes() == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        for (String permissionCode : permissionCodes) {
            if (user.getPermissionCodes().stream().anyMatch(code -> code.equalsIgnoreCase(permissionCode))) {
                return;
            }
        }
        throw new BusinessException("common.auth.forbidden");
    }

    private OrderPolicyResolved resolvePolicy(String routeId, String departureId) {
        RouteProduct route = routeRepository.findById(routeId)
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = departureRepository.findById(departureId)
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        if (!Objects.equals(departure.getRouteId(), routeId)) {
            throw new BusinessException("error.departure.routeMismatch");
        }

        boolean contractRequired = route.getContractRequiredDefault() != null && route.getContractRequiredDefault();
        OrderLockPolicy lockPolicy = route.getLockPolicyDefault() == null
            ? OrderLockPolicy.ON_DEPOSIT : route.getLockPolicyDefault();
        OrderPaymentPolicy paymentPolicy = route.getPaymentPolicyDefault() == null
            ? OrderPaymentPolicy.DEPOSIT_BALANCE : route.getPaymentPolicyDefault();
        DepositRuleType depositType = route.getDepositTypeDefault() == null
            ? DepositRuleType.PERCENT : route.getDepositTypeDefault();
        BigDecimal depositValue = route.getDepositValueDefault() == null
            ? new BigDecimal("30.00") : route.getDepositValueDefault().setScale(2, RoundingMode.HALF_UP);
        Integer depositDeadlineDays = route.getDepositDeadlineDaysDefault() == null ? 3 : route.getDepositDeadlineDaysDefault();
        Integer balanceDeadlineDays = route.getBalanceDeadlineDaysDefault() == null ? 7 : route.getBalanceDeadlineDaysDefault();
        Integer autoCancelHours = route.getAutoCancelHoursDefault() == null ? 24 : route.getAutoCancelHoursDefault();

        if (departure.getContractRequiredOverride() != null) {
            contractRequired = departure.getContractRequiredOverride();
        }
        if (departure.getLockPolicyOverride() != null) {
            lockPolicy = departure.getLockPolicyOverride();
        }
        if (departure.getPaymentPolicyOverride() != null) {
            paymentPolicy = departure.getPaymentPolicyOverride();
        }
        if (departure.getDepositTypeOverride() != null) {
            depositType = departure.getDepositTypeOverride();
        }
        if (departure.getDepositValueOverride() != null) {
            depositValue = departure.getDepositValueOverride().setScale(2, RoundingMode.HALF_UP);
        }
        if (departure.getDepositDeadlineDaysOverride() != null) {
            depositDeadlineDays = departure.getDepositDeadlineDaysOverride();
        }
        if (departure.getBalanceDeadlineDaysOverride() != null) {
            balanceDeadlineDays = departure.getBalanceDeadlineDaysOverride();
        }
        if (departure.getAutoCancelHoursOverride() != null) {
            autoCancelHours = departure.getAutoCancelHoursOverride();
        }

        if (paymentPolicy == OrderPaymentPolicy.DEPOSIT_BALANCE) {
            if (depositValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("error.policy.depositValue.positive");
            }
            if (depositType == DepositRuleType.PERCENT && depositValue.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("error.policy.depositPercent.max100");
            }
        }
        if (depositDeadlineDays < 0 || balanceDeadlineDays < 0 || autoCancelHours < 0) {
            throw new BusinessException("error.policy.deadline.nonNegative");
        }

        return new OrderPolicyResolved(
            contractRequired,
            lockPolicy,
            paymentPolicy,
            depositType,
            depositValue,
            depositDeadlineDays,
            balanceDeadlineDays,
            autoCancelHours
        );
    }

    private void applyPolicy(TravelOrder order, OrderPolicyResolved policy, boolean initializing) {
        order.setContractRequired(policy.contractRequired());
        order.setLockPolicy(policy.lockPolicy());
        order.setPaymentPolicy(policy.paymentPolicy());
        order.setDepositType(policy.depositType());
        order.setDepositValue(policy.depositValue());
        order.setDepositDeadlineDays(policy.depositDeadlineDays());
        order.setBalanceDeadlineDays(policy.balanceDeadlineDays());
        order.setAutoCancelHours(policy.autoCancelHours());

        if (initializing) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setInventoryStatus(InventoryStatus.UNLOCKED);
            order.setSettlementStatus(SettlementStatus.UNSETTLED);
            order.setContractStatus(policy.contractRequired() ? ContractStatus.PENDING_SIGN : ContractStatus.NOT_REQUIRED);
            return;
        }

        if (!policy.contractRequired()) {
            order.setContractStatus(ContractStatus.NOT_REQUIRED);
        } else if (order.getContractStatus() == null || order.getContractStatus() == ContractStatus.NOT_REQUIRED) {
            order.setContractStatus(ContractStatus.PENDING_SIGN);
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }
        if (order.getInventoryStatus() == null) {
            order.setInventoryStatus(InventoryStatus.UNLOCKED);
        }
        if (order.getSettlementStatus() == null) {
            order.setSettlementStatus(SettlementStatus.UNSETTLED);
        }
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
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = departureRepository.findById(request.departureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        if (!Objects.equals(departure.getRouteId(), request.routeId())) {
            throw new BusinessException("error.departure.routeMismatch");
        }
        if (request.travelerCount() == null || request.travelerCount() < 1) {
            throw new BusinessException("error.order.travelerCount.required");
        }
        if (request.totalAmount() == null) {
            throw new BusinessException("error.order.totalAmount.required");
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
            .orElseThrow(() -> new BusinessException("error.customer.notFound"));
        RouteProduct route = routeRepository.findById(request.routeId())
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = departureRepository.findById(request.departureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));
        if (!Objects.equals(departure.getRouteId(), route.getId())) {
            throw new BusinessException("error.departure.routeMismatch");
        }
        if (request.priceSelections() == null || request.priceSelections().isEmpty()) {
            throw new BusinessException("error.order.priceSelections.required");
        }

        Map<String, OrderTravelerSnapshot> travelerSnapshots = new LinkedHashMap<>();
        List<OrderPriceItem> priceItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = null;

        for (OrderPriceSelectionRequest selection : request.priceSelections()) {
            String travelerId = selection.travelerId();
            String travelerName = null;
            Traveler traveler = null;
            if (travelerId != null) {
                traveler = travelerRepository.findById(travelerId)
                    .orElseThrow(() -> new BusinessException("error.traveler.notFound"));
                if (!Objects.equals(traveler.getCustomerId(), request.customerId())) {
                    throw new BusinessException("error.order.traveler.customerMismatch");
                }
                travelerName = traveler.getName();
                Traveler travelerRef = traveler;
                travelerSnapshots.computeIfAbsent(travelerId, key -> buildTravelerSnapshot(travelerRef));
            }

            DeparturePrice departurePrice;
            if (selection.departurePriceId() != null) {
                departurePrice = priceRepository.findById(selection.departurePriceId())
                    .orElseThrow(() -> new BusinessException("error.departure.price.notFound"));
            } else {
                departurePrice = resolveTravelerDefaultPrice(departure, traveler);
            }
            if (!Objects.equals(departurePrice.getDepartureId(), departure.getId())) {
                throw new BusinessException("error.order.price.departureMismatch");
            }
            if (departurePrice.getPrice() == null || departurePrice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("error.order.departurePrice.invalid");
            }
            String itemCurrency = normalizeCurrency(departurePrice.getCurrency());
            if (!CNY.equals(itemCurrency)) {
                throw new BusinessException("error.currency.onlyCnySupported");
            }
            if (currency == null) {
                currency = itemCurrency;
            } else if (!Objects.equals(currency, itemCurrency)) {
                throw new BusinessException("error.order.currency.mixedNotSupported");
            }

            Integer quantity = selection.quantity() == null ? 1 : selection.quantity();
            if (quantity < 1) {
                throw new BusinessException("error.order.itemQuantity.invalid");
            }

            if (travelerId != null) {
                quantity = 1;
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
            throw new BusinessException("error.order.traveler.required");
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

    private Specification<TravelOrder> buildOrderSpec(LoginUser user, String keyword, String status, Long customerId) {
        Set<String> scopedDepartmentIds = Set.of();
        if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
            scopedDepartmentIds = dataScopeResolver.resolveDepartmentIds(user);
            if (scopedDepartmentIds.isEmpty()) {
                return (root, query, cb) -> cb.disjunction();
            }
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(normalizedStatus)) {
            normalizedStatus = "";
        }
        OrderStatus statusEnum = null;
        if (!normalizedStatus.isBlank()) {
            try {
                statusEnum = OrderStatus.valueOf(normalizedStatus);
            } catch (IllegalArgumentException ex) {
                statusEnum = null;
            }
        }

        Set<String> matchedCustomerIds = Set.of();
        Set<String> matchedRouteIds = Set.of();
        if (!normalizedKeyword.isBlank()) {
            matchedCustomerIds = customerRepository.findByDeletedFalse().stream()
                .filter(item -> containsIgnoreCase(item.getName(), normalizedKeyword)
                    || containsIgnoreCase(item.getPhone(), normalizedKeyword))
                .map(Customer::getId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
            matchedRouteIds = routeRepository.findByDeletedFalse().stream()
                .filter(item -> containsIgnoreCase(item.getName(), normalizedKeyword)
                    || containsIgnoreCase(item.getCode(), normalizedKeyword))
                .map(RouteProduct::getId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        }

        final Set<String> finalScopedDepartmentIds = scopedDepartmentIds;
        final Set<String> finalMatchedCustomerIds = matchedCustomerIds;
        final Set<String> finalMatchedRouteIds = matchedRouteIds;
        final String finalKeyword = normalizedKeyword;
        final OrderStatus finalStatus = statusEnum;
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (user.getDataScope() == DataScope.SELF) {
                predicates.add(cb.equal(root.get("salesUserId"), user.getUserId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT) {
                predicates.add(cb.equal(root.get("salesDeptId"), user.getDepartmentId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
                predicates.add(root.get("salesDeptId").in(finalScopedDepartmentIds));
            }

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }

            if (finalStatus != null) {
                predicates.add(cb.equal(root.get("status"), finalStatus));
            }

            if (!finalKeyword.isBlank()) {
                String likeValue = "%" + finalKeyword + "%";
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("orderNo"), "")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("orderType"), "")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("productCategory"), "")), likeValue));
                if (!finalMatchedCustomerIds.isEmpty()) {
                    keywordPredicates.add(root.get("customerId").in(finalMatchedCustomerIds));
                }
                if (!finalMatchedRouteIds.isEmpty()) {
                    keywordPredicates.add(root.get("routeId").in(finalMatchedRouteIds));
                }
                predicates.add(cb.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private List<Customer> listVisibleCustomers(LoginUser user) {
        if (user == null) {
            return List.of();
        }
        if (user.getDataScope() == DataScope.ALL) {
            return customerRepository.findByDeletedFalse();
        }
        if (user.getDataScope() == DataScope.SELF) {
            return customerRepository.findByOwnerUserIdAndDeletedFalse(user.getUserId());
        }
        if (user.getDataScope() == DataScope.DEPARTMENT) {
            return customerRepository.findByOwnerDeptIdAndDeletedFalse(user.getDepartmentId());
        }
        Set<String> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return customerRepository.findByOwnerDeptIdInAndDeletedFalse(departmentIds);
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || source.isBlank()) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private DeparturePrice resolveTravelerDefaultPrice(Departure departure, Traveler traveler) {
        if (traveler == null) {
            throw new BusinessException("error.departure.price.notFound");
        }
        List<DeparturePrice> prices = priceRepository.findByDepartureIdAndDeletedFalse(departure.getId());
        if (prices.isEmpty()) {
            throw new BusinessException("error.departure.price.notFound");
        }

        int age = 18;
        if (traveler.getBirthday() != null && departure.getStartDate() != null) {
            age = Math.max(0, Period.between(traveler.getBirthday(), departure.getStartDate()).getYears());
        }

        List<String> preferredTypes = new ArrayList<>();
        if (age < 2) {
            preferredTypes.add("INFANT");
            preferredTypes.add("CHILD");
            preferredTypes.add("ADULT");
        } else if (age < 12) {
            preferredTypes.add("CHILD");
            preferredTypes.add("ADULT");
        } else {
            preferredTypes.add("ADULT");
            preferredTypes.add("CHILD");
        }

        for (String priceType : preferredTypes) {
            Optional<DeparturePrice> matched = prices.stream()
                .filter(item -> item.getPriceType() != null && item.getPriceType().equalsIgnoreCase(priceType))
                .findFirst();
            if (matched.isPresent()) {
                return matched.get();
            }
        }

        return prices.stream()
            .findFirst()
            .orElseThrow(() -> new BusinessException("error.departure.price.notFound"));
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
            throw new BusinessException("error.order.travelerCount.lessThanMinGroup");
        }
        if (departure.getMaxGroupSize() != null && travelerCount > departure.getMaxGroupSize()) {
            throw new BusinessException("error.order.travelerCount.exceedsMaxGroup");
        }
    }

    private void ensureStockAvailable(Departure departure, int travelerCount) {
        if (departure.getStock() != null && travelerCount > departure.getStock()) {
            throw new BusinessException("error.order.travelerCount.exceedsStock");
        }
    }

    private void persistPricing(String orderId, PricingCalculation calculation) {
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

    private void replacePricing(String orderId, PricingCalculation calculation) {
        markPricingDeleted(orderId);
        persistPricing(orderId, calculation);
    }

    private void markPricingDeleted(String orderId) {
        for (OrderTravelerSnapshot snapshot : travelerSnapshotRepository.findByOrderIdAndDeletedFalse(orderId)) {
            snapshot.setDeleted(true);
            travelerSnapshotRepository.save(snapshot);
        }
        for (OrderPriceItem item : orderPriceItemRepository.findByOrderIdAndDeletedFalse(orderId)) {
            item.setDeleted(true);
            orderPriceItemRepository.save(item);
        }
    }

    private void addStatusLog(String orderId, String fromStatus, String toStatus, String remark) {
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
            throw new BusinessException("error.order.edit.invalidStatus");
        }
    }

    private void assertCnyCurrency(String requestCurrency) {
        if (requestCurrency == null || requestCurrency.isBlank()) {
            return;
        }
        String normalized = requestCurrency.trim().toUpperCase(Locale.ROOT);
        if (!CNY.equals(normalized)) {
            throw new BusinessException("error.currency.onlyCnySupported");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return CNY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private record OrderPolicyResolved(
        boolean contractRequired,
        OrderLockPolicy lockPolicy,
        OrderPaymentPolicy paymentPolicy,
        DepositRuleType depositType,
        BigDecimal depositValue,
        Integer depositDeadlineDays,
        Integer balanceDeadlineDays,
        Integer autoCancelHours
    ) {
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

