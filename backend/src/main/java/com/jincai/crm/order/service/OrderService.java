package com.jincai.crm.order.service;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单核心服务。
 *
 * <p>负责订单的创建、查询、更新、删除等核心业务流程协调。
 * 复杂的定价计算、状态管理、导入等功能已拆分到专门的服务中。
 *
 * <p>主要职责：
 * <ul>
 *   <li>订单生命周期协调（调用各专门服务）</li>
 *   <li>订单数据查询和可见性控制</li>
 *   <li>订单上下文信息提供</li>
 *   <li>订单操作权限验证</li>
 * </ul>
 *
 * @see OrderPricingService 价格计算服务
 * @see OrderStatusService 状态管理服务
 * @see OrderImportService 导入服务
 */
@Service
@Slf4j
public class OrderService {

    private static final String CNY = "CNY";

    // 核心仓储依赖
    private final TravelOrderRepository orderRepository;
    private final OrderStatusLogRepository logRepository;

    // 协作服务依赖
    private final OrderPricingService pricingService;
    private final OrderStatusService statusService;
    private final OrderImportService importService;
    private final WorkflowService workflowService;

    // 数据访问依赖
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

    public OrderService(
        TravelOrderRepository orderRepository,
        OrderStatusLogRepository logRepository,
        OrderPricingService pricingService,
        OrderStatusService statusService,
        OrderImportService importService,
        WorkflowService workflowService,
        DataScopeResolver dataScopeResolver,
        AuditLogService auditLogService,
        CustomerRepository customerRepository,
        TravelerRepository travelerRepository,
        RouteProductRepository routeRepository,
        DepartureRepository departureRepository,
        DeparturePriceRepository priceRepository,
        OrderTravelerSnapshotRepository travelerSnapshotRepository,
        OrderPriceItemRepository orderPriceItemRepository,
        I18nService i18nService) {

        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.pricingService = pricingService;
        this.statusService = statusService;
        this.importService = importService;
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

    // ------------------------------------------------------------------ 查询功能

    /**
     * 查询当前用户可见的订单列表。
     *
     * @param user 当前登录用户
     * @return 可见订单列表
     */
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

    /**
     * 分页查询当前用户可见的订单列表。
     *
     * @param user 当前登录用户
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param status 订单状态筛选
     * @param customerId 客户ID筛选
     * @return 分页结果
     */
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

    /**
     * 获取订单上下文选项数据。
     *
     * @param user 当前登录用户
     * @return 上下文选项视图
     */
    public OrderContextOptionsView contextOptions(LoginUser user) {
        return new OrderContextOptionsView(
            listVisibleCustomers(user),
            routeRepository.findByDeletedFalse(),
            List.of() // 剥离全量 departures
        );
    }

    /**
     * 查询指定线路的所有出团日期。
     *
     * @param routeId 线路ID
     * @return 出团日期列表
     */
    public List<Departure> routeDepartures(String routeId) {
        return departureRepository.findByDeletedFalse().stream()
            .filter(d -> d.getRouteId() != null && d.getRouteId().equals(routeId))
            .toList();
    }

    /**
     * 获取订单详情视图。
     *
     * @param id 订单ID
     * @return 订单详情视图
     */
    public OrderDetailView detail(String id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        return new OrderDetailView(
            order,
            travelerSnapshotRepository.findByOrderIdAndDeletedFalse(id),
            orderPriceItemRepository.findByOrderIdAndDeletedFalse(id)
        );
    }

    /**
     * 查询订单状态变更日志。
     *
     * @param orderId 订单ID
     * @return 状态日志列表
     */
    public List<OrderStatusLog> logs(String orderId) {
        return logRepository.findByOrderIdAndDeletedFalseOrderByCreatedAtAsc(orderId);
    }

    /**
     * 生成订单报价预览。
     *
     * @param request 订单请求
     * @return 报价视图
     */
    public OrderQuoteView quote(OrderRequest request) {
        PricingCalculation calculation = pricingService.calculatePricing(request);
        return new OrderQuoteView(
            calculation.routeId(),
            calculation.departureId(),
            calculation.travelerSnapshots().size(),
            calculation.totalAmount(),
            calculation.currency(),
            calculation.travelerSnapshots(),
            calculation.priceItems()
        );
    }

    // ------------------------------------------------------------------ 写入功能

    /**
     * 创建新订单。
     *
     * @param request 订单请求
     * @param user 当前登录用户
     * @return 创建的订单
     */
    @Transactional
    public TravelOrder create(OrderRequest request, LoginUser user) {
        log.info("创建订单 - 客户ID: {}, 线路ID: {}, 出团ID: {}, 订单类型: {}",
                request.customerId(), request.routeId(), request.departureId(), request.orderType());
        try {
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
            pricingService.persistPricing(saved.getId(), calculation);
            statusService.addStatusLog(saved.getId(), null, OrderStatus.DRAFT, "Order created");
            log.info("订单创建成功 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
            return saved;
        } catch (Exception e) {
            log.error("创建订单失败 - 客户ID: {}, 线路ID: {}", request.customerId(), request.routeId(), e);
            throw e;
        }
    }

    /**
     * 更新订单信息。
     *
     * @param id 订单ID
     * @param request 订单请求
     * @param httpServletRequest HTTP请求（用于审计日志）
     * @return 更新后的订单
     */
    @Transactional
    public TravelOrder update(String id, OrderRequest request, HttpServletRequest httpServletRequest) {
        log.info("更新订单 - 订单ID: {}, 客户ID: {}, 线路ID: {}, 出团ID: {}, 订单类型: {}",
                id, request.customerId(), request.routeId(), request.departureId(), request.orderType());
        try {
            TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
            statusService.validateEditable(order);
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
            pricingService.replacePricing(saved.getId(), calculation);
            Map<String, Object> after = Map.of(
                "travelerCount", saved.getTravelerCount(),
                "totalAmount", saved.getTotalAmount(),
                "departureId", saved.getDepartureId()
            );
            auditLogService.logDiff("ORDER", saved.getId(), before, after, httpServletRequest.getRemoteAddr());
            log.info("订单更新成功 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
            return saved;
        } catch (Exception e) {
            log.error("更新订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    /**
     * 删除订单（软删除）。
     *
     * @param id 订单ID
     */
    @Transactional
    public void delete(String id) {
        log.info("删除订单 - 订单ID: {}", id);
        try {
            TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
            statusService.validateEditable(order);
            order.setDeleted(true);
            orderRepository.save(order);
            pricingService.markPricingDeleted(id);
            log.info("订单删除成功 - 订单ID: {}", id);
        } catch (Exception e) {
            log.error("删除订单失败 - 订单ID: {}", id, e);
            throw e;
        }
    }

    /**
     * 获取客户的所有旅客信息。
     *
     * @param customerId 客户ID
     * @param user 当前登录用户
     * @return 旅客列表
     */
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

    /**
     * 查询出团日期的价格列表。
     *
     * @param departureId 出团日期ID
     * @return 价格列表
     */
    public List<DeparturePrice> departurePrices(String departureId) {
        departureRepository.findById(departureId).orElseThrow(() -> new BusinessException("error.departure.notFound"));
        return priceRepository.findByDepartureIdAndDeletedFalse(departureId);
    }

    /**
     * 执行订单操作。
     *
     * @param id 订单ID
     * @param request 操作请求
     * @param user 当前登录用户
     * @return 操作后的订单
     */
    @Transactional
    public TravelOrder action(String id, OrderActionRequest request, LoginUser user) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        OrderStatus fromStatus = order.getStatus();

        switch (request.action()) {
            case SUBMIT -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                statusService.submit(order, false);
            }
            case RESUBMIT -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                statusService.submit(order, true);
            }
            case APPROVE -> {
                requireAnyPermission(user, "BTN_ORDER_APPROVE");
                statusService.approve(order, user, request.comment());
            }
            case REJECT -> {
                requireAnyPermission(user, "BTN_ORDER_REJECT");
                statusService.reject(order, user, request.comment());
            }
            case WITHDRAW -> {
                requireAnyPermission(user, "BTN_ORDER_SUBMIT");
                statusService.withdraw(order, user, request.comment());
            }
            case TRANSFER -> {
                requireAnyPermission(user, "BTN_ORDER_APPROVE");
                workflowService.transfer(order.getId(), user, request.targetRoleCode(), request.comment());
            }
            case SIGN_CONTRACT -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                statusService.signContract(order);
            }
            case LOCK_INVENTORY -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                statusService.lockInventory(order);
            }
            case RELEASE_INVENTORY -> {
                requireAnyPermission(user, "BTN_ORDER_EDIT");
                statusService.releaseInventory(order);
            }
            case CONFIRM_TRAVEL -> {
                requireAnyPermission(user, "BTN_ORDER_TRAVEL");
                statusService.confirmTravel(order);
            }
            case COMPLETE_TRAVEL -> {
                requireAnyPermission(user, "BTN_ORDER_TRAVEL");
                statusService.completeTravel(order);
            }
            case COMPLETE_ORDER -> {
                requireAnyPermission(user, "BTN_ORDER_TRAVEL");
                statusService.completeOrder(order);
            }
            case CANCEL -> {
                requireAnyPermission(user, "BTN_ORDER_CANCEL");
                statusService.cancelOrder(order, user, request.comment());
            }
            default -> throw new BusinessException("error.order.action.notSupported");
        }

        return orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
    }

    // ------------------------------------------------------------------ 公开操作接口

    /**
     * 提交订单。
     *
     * @param id 订单ID
     * @return 提交后的订单
     */
    @Transactional
    public TravelOrder submit(String id) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        statusService.submit(order, false);
        return order;
    }

    /**
     * 审批订单。
     *
     * @param id 订单ID
     * @param user 当前用户
     * @param comment 审批意见
     * @return 审批后的订单
     */
    @Transactional
    public TravelOrder approve(String id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        statusService.approve(order, user, comment);
        return order;
    }

    /**
     * 驳回订单。
     *
     * @param id 订单ID
     * @param user 当前用户
     * @param comment 驳回意见
     * @return 驳回后的订单
     */
    @Transactional
    public TravelOrder reject(String id, LoginUser user, String comment) {
        TravelOrder order = orderRepository.findById(id).orElseThrow(() -> new BusinessException("error.order.notFound"));
        statusService.reject(order, user, comment);
        return order;
    }

    /**
     * 导入订单。
     *
     * @param file Excel文件
     * @param user 当前用户
     * @return 导入结果
     */
    public ImportOrderResult importOrders(MultipartFile file, LoginUser user) {
        return importService.importOrders(file, user);
    }

    /**
     * 自动取消超时未支付的订单。
     *
     * @return 被取消的订单数量
     */
    @Transactional
    public int autoCancelOverdueOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<TravelOrder> orders = orderRepository.findByStatusInAndDeletedFalse(List.of(OrderStatus.DRAFT));
        int count = 0;
        for (TravelOrder order : orders) {
            if (shouldAutoCancel(order, now)) {
                try {
                    statusService.cancelOrder(order, null, "系统自动取消：超时未支付");
                    count++;
                } catch (Exception e) {
                    log.warn("自动取消订单失败 - 订单ID: {}", order.getId(), e);
                }
            }
        }
        return count;
    }

    // ------------------------------------------------------------------ 私有辅助方法

    private boolean shouldAutoCancel(TravelOrder order, LocalDateTime now) {
        if (order.getCreatedAt() == null || order.getAutoCancelHours() == null) {
            return false;
        }
        LocalDateTime deadline = order.getCreatedAt().plusHours(order.getAutoCancelHours());
        return now.isAfter(deadline);
    }

    private List<Customer> listVisibleCustomers(LoginUser user) {
        if (user == null) {
            return List.of();
        }
        // 这里应该调用客户服务来获取可见客户列表
        // 简化实现，直接返回所有未删除客户
        return customerRepository.findByDeletedFalse();
    }

    private Specification<TravelOrder> buildOrderSpec(LoginUser user, String keyword, String status, Long customerId) {
        Set<String> departmentIds = null;
        if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
            departmentIds = dataScopeResolver.resolveDepartmentIds(user);
            if (departmentIds.isEmpty()) {
                return (root, query, cb) -> cb.disjunction();
            }
        }

        final Set<String> scopedDepartmentIds = departmentIds;
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);

        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (user.getDataScope() == DataScope.SELF) {
                predicates.add(cb.equal(root.get("salesUserId"), user.getUserId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT) {
                predicates.add(cb.equal(root.get("salesDeptId"), user.getDepartmentId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
                predicates.add(root.get("salesDeptId").in(scopedDepartmentIds));
            }

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }

            if (!normalizedStatus.isBlank()) {
                try {
                    OrderStatus statusEnum = OrderStatus.valueOf(normalizedStatus.toUpperCase(Locale.ROOT));
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid status
                }
            }

            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(cb.like(cb.lower(root.get("orderNo")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("remark"), "")), likeValue));
                predicates.add(cb.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private PricingCalculation applyOrderAmounts(TravelOrder order, OrderRequest request) {
        PricingCalculation calculation = pricingService.calculatePricing(request);
        order.setTravelerCount(calculation.travelerSnapshots().size());
        order.setTotalAmount(calculation.totalAmount());
        order.setCurrency(calculation.currency());
        return calculation;
    }

    private void applyPolicy(TravelOrder order, OrderPolicyView policy, boolean isNew) {
        order.setContractRequired(policy.contractRequired());
        order.setLockPolicy(policy.lockPolicy());
        order.setPaymentPolicy(policy.paymentPolicy());
        order.setDepositType(policy.depositType());
        order.setDepositValue(policy.depositValue());
        order.setDepositDeadlineDays(policy.depositDeadlineDays());
        order.setBalanceDeadlineDays(policy.balanceDeadlineDays());
        order.setAutoCancelHours(policy.autoCancelHours());

        // 新订单时设置默认值
        if (isNew) {
            order.setStatus(OrderStatus.DRAFT);
            order.setContractStatus(policy.contractRequired() ? ContractStatus.REQUIRED : ContractStatus.NOT_REQUIRED);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setInventoryStatus(InventoryStatus.UNLOCKED);
            order.setSettlementStatus(SettlementStatus.UNSETTLED);
        }
    }

    private OrderPolicyView resolvePolicy(String routeId, String departureId) {
        RouteProduct route = routeRepository.findById(routeId)
            .orElseThrow(() -> new BusinessException("error.route.notFound"));
        Departure departure = departureRepository.findById(departureId)
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));

        // 优先使用出团日期的覆盖策略，否则使用线路默认策略
        return new OrderPolicyView(
            departure.getContractRequiredOverride() != null ? departure.getContractRequiredOverride() : route.getContractRequiredDefault(),
            departure.getLockPolicyOverride() != null ? departure.getLockPolicyOverride() : route.getLockPolicyDefault(),
            departure.getPaymentPolicyOverride() != null ? departure.getPaymentPolicyOverride() : route.getPaymentPolicyDefault(),
            departure.getDepositTypeOverride() != null ? departure.getDepositTypeOverride() : route.getDepositTypeDefault(),
            departure.getDepositValueOverride() != null ? departure.getDepositValueOverride() : route.getDepositValueDefault(),
            departure.getDepositDeadlineDaysOverride() != null ? departure.getDepositDeadlineDaysOverride() : route.getDepositDeadlineDaysDefault(),
            departure.getBalanceDeadlineDaysOverride() != null ? departure.getBalanceDeadlineDaysOverride() : route.getBalanceDeadlineDaysDefault(),
            departure.getAutoCancelHoursOverride() != null ? departure.getAutoCancelHoursOverride() : route.getAutoCancelHoursDefault()
        );
    }

    private void assertCnyCurrency(String currency) {
        if (currency != null && !CNY.equalsIgnoreCase(currency)) {
            throw new BusinessException("error.order.currency.onlyCnySupported");
        }
    }

    private String generateOrderNo() {
        return "DD" + System.currentTimeMillis();
    }

    private void requireAnyPermission(LoginUser user, String... permissions) {
        if (user == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        // 简化权限检查，实际应该检查用户权限
        if (permissions == null || permissions.length == 0) {
            return;
        }
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
}