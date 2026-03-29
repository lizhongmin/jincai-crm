package com.jincai.crm.order.service;

import com.jincai.crm.audit.service.AuditLogService;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.OrderStatusLogRepository;
import com.jincai.crm.order.repository.TravelOrderRepository;
import com.jincai.crm.product.entity.Departure;
import com.jincai.crm.product.entity.RouteProduct;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单状态管理服务。
 *
 * <p>负责订单的状态流转、验证和相关业务逻辑处理。
 * 从庞大的 {@link OrderService} 中拆分出来，以提高代码的可维护性和可测试性。
 *
 * <p>主要职责：
 * <ul>
 *   <li>订单状态验证</li>
 *   <li>订单状态流转（提交、审批、拒绝等）</li>
 *   <li>库存锁定与释放</li>
 *   <li>合同签署处理</li>
 *   <li>订单状态日志记录</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final TravelOrderRepository orderRepository;
    private final OrderStatusLogRepository logRepository;
    private final WorkflowService workflowService;
    private final AuditLogService auditLogService;
    private final CustomerRepository customerRepository;
    private final RouteProductRepository routeRepository;
    private final DepartureRepository departureRepository;

    /**
     * 验证订单是否可以编辑（仅草稿或已驳回状态可编辑）。
     *
     * @param order 订单
     * @throws BusinessException 若订单状态不允许编辑
     */
    public void validateEditable(TravelOrder order) {
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.DRAFT && status != OrderStatus.REJECTED) {
            throw new BusinessException("error.order.edit.invalidStatus");
        }
    }

    /**
     * 提交订单（草稿 → 待审批）。
     *
     * @param order 订单
     * @param resubmitOnly 是否仅重新提交（不创建应收应付）
     * @throws BusinessException 若订单状态不允许提交
     */
    @Transactional
    public void submit(TravelOrder order, boolean resubmitOnly) {
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.DRAFT && status != OrderStatus.REJECTED) {
            throw new BusinessException("error.order.submit.invalidStatus");
        }

        // 验证客户存在
        Customer customer = customerRepository.findById(order.getCustomerId())
            .orElseThrow(() -> new BusinessException("error.customer.notFound"));

        // 验证线路存在
        RouteProduct route = routeRepository.findById(order.getRouteId())
            .orElseThrow(() -> new BusinessException("error.route.notFound"));

        // 验证出团日期存在
        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));

        // 检查出团日期状态
        if (!"OPEN".equals(departure.getStatus())) {
            throw new BusinessException("error.departure.notOpen");
        }

        // 检查合同要求
        if (order.getContractRequired() && order.getContractStatus() != ContractStatus.SIGNED) {
            throw new BusinessException("error.order.contract.required");
        }

        // 检查库存
        Integer stock = departure.getStock();
        if (stock != null && order.getTravelerCount() != null && order.getTravelerCount() > stock) {
            throw new BusinessException("error.departure.stock.insufficient");
        }

        // 状态流转
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setSubmittedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.PENDING_APPROVAL, "订单提交");

        // 首次提交时创建工作流实例
        if (!resubmitOnly) {
            workflowService.startWorkflow(saved);
        }

        log.info("订单提交成功 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 审批通过订单（待审批 → 已批准）。
     *
     * @param order 订单
     * @param user 当前用户
     * @param comment 审批意见
     * @throws BusinessException 若订单状态不允许审批
     */
    @Transactional
    public void approve(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.approve.invalidStatus");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.APPROVED);
        order.setApprovedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 根据锁定策略锁定库存
        if (saved.getLockPolicy() == OrderLockPolicy.ON_APPROVAL) {
            lockInventory(saved);
        }

        // 完成工作流节点
        workflowService.approve(saved.getId(), user, comment);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.APPROVED, comment);

        log.info("订单审批通过 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 驳回订单（待审批 → 已驳回）。
     *
     * @param order 订单
     * @param user 当前用户
     * @param comment 驳回原因
     * @throws BusinessException 若订单状态不允许驳回
     */
    @Transactional
    public void reject(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.reject.invalidStatus");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.REJECTED);
        TravelOrder saved = orderRepository.save(order);

        // 完成工作流节点（驳回）
        workflowService.reject(saved.getId(), user, comment);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.REJECTED, comment);

        log.info("订单被驳回 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 撤回订单（待审批 → 草稿）。
     *
     * @param order 订单
     * @param user 当前用户
     * @param comment 撤回原因
     * @throws BusinessException 若订单状态不允许撤回
     */
    @Transactional
    public void withdraw(TravelOrder order, LoginUser user, String comment) {
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("error.order.withdraw.invalidStatus");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.DRAFT);
        TravelOrder saved = orderRepository.save(order);

        // 撤回工作流节点
        workflowService.withdraw(saved.getId(), user, comment);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.DRAFT, comment);

        log.info("订单已撤回 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 签署合同。
     *
     * @param order 订单
     * @throws BusinessException 若订单状态或合同状态不允许签署
     */
    @Transactional
    public void signContract(TravelOrder order) {
        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.SETTLING) {
            throw new BusinessException("error.order.contract.sign.invalidStatus");
        }
        if (order.getContractStatus() != ContractStatus.PENDING_SIGN) {
            throw new BusinessException("error.order.contract.notRequired");
        }

        ContractStatus oldStatus = order.getContractStatus();
        order.setContractStatus(ContractStatus.SIGNED);
        order.setContractSignedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 根据锁定策略锁定库存（若尚未锁定）
        if (saved.getLockPolicy() == OrderLockPolicy.ON_CONTRACT && saved.getInventoryStatus() != InventoryStatus.LOCKED) {
            lockInventory(saved);
        }

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, ContractStatus.SIGNED, "合同签署");

        log.info("合同签署完成 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 开始出行（已批准 → 出行中）。
     *
     * @param order 订单
     * @throws BusinessException 若订单状态或前置条件不满足
     */
    @Transactional
    public void confirmTravel(TravelOrder order) {
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("error.order.travel.confirm.invalidStatus");
        }
        if (order.getContractStatus() != ContractStatus.SIGNED && order.getContractRequired()) {
            throw new BusinessException("error.order.travel.contractRequired");
        }
        if (order.getInventoryStatus() != InventoryStatus.LOCKED) {
            throw new BusinessException("error.order.travel.inventoryRequired");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("error.order.travel.paymentRequired");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.IN_TRAVEL);
        order.setTravelStartedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.IN_TRAVEL, "开始出行");

        log.info("订单开始出行 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 结束出行（出行中 → 出行结束）。
     *
     * @param order 订单
     * @throws BusinessException 若订单状态不正确
     */
    @Transactional
    public void completeTravel(TravelOrder order) {
        if (order.getStatus() != OrderStatus.IN_TRAVEL) {
            throw new BusinessException("error.order.travel.complete.invalidStatus");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.TRAVEL_FINISHED);
        order.setTravelFinishedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.TRAVEL_FINISHED, "出行结束");

        log.info("订单出行结束 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 完成订单（出行结束/结算中 → 已完成）。
     *
     * @param order 订单
     * @throws BusinessException 若订单状态或结算状态不满足条件
     */
    @Transactional
    public void completeOrder(TravelOrder order) {
        if (order.getStatus() != OrderStatus.TRAVEL_FINISHED && order.getStatus() != OrderStatus.SETTLING) {
            throw new BusinessException("error.order.complete.invalidStatus");
        }
        if (order.getSettlementStatus() != SettlementStatus.SETTLED) {
            throw new BusinessException("error.order.complete.settlementRequired");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.COMPLETED, "订单完成");

        log.info("订单已完成 - 订单ID: {}, 订单号: {}", saved.getId(), saved.getOrderNo());
    }

    /**
     * 取消订单。
     *
     * @param order 订单
     * @param user 当前用户
     * @param reason 取消原因
     * @throws BusinessException 若订单状态不允许取消
     */
    @Transactional
    public void cancelOrder(TravelOrder order, LoginUser user, String reason) {
        OrderStatus status = order.getStatus();
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELED) {
            throw new BusinessException("error.order.cancel.invalidStatus");
        }

        // 释放已锁定的库存
        if (order.getInventoryStatus() == InventoryStatus.LOCKED) {
            releaseInventory(order);
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        TravelOrder saved = orderRepository.save(order);

        // 记录状态日志
        addStatusLog(saved.getId(), oldStatus, OrderStatus.CANCELED, reason);

        log.info("订单已取消 - 订单ID: {}, 订单号: {}, 原因: {}", saved.getId(), saved.getOrderNo(), reason);
    }

    /**
     * 锁定出团日期库存。
     *
     * @param order 订单
     * @throws BusinessException 若库存不足
     */
    @Transactional
    public void lockInventory(TravelOrder order) {
        if (order.getInventoryStatus() == InventoryStatus.LOCKED) {
            return; // 已锁定，无需重复操作
        }

        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));

        Integer stock = departure.getStock();
        if (stock == null) {
            stock = Integer.MAX_VALUE; // 无库存限制
        }

        if (stock < order.getTravelerCount()) {
            throw new BusinessException("error.departure.stock.insufficient");
        }

        departure.setStock(stock - order.getTravelerCount());
        departureRepository.save(departure);

        order.setInventoryStatus(InventoryStatus.LOCKED);
        orderRepository.save(order);

        log.info("库存已锁定 - 订单ID: {}, 出团ID: {}, 锁定数量: {}",
            order.getId(), departure.getId(), order.getTravelerCount());
    }

    /**
     * 释放出团日期库存。
     *
     * @param order 订单
     */
    @Transactional
    public void releaseInventory(TravelOrder order) {
        if (order.getInventoryStatus() != InventoryStatus.LOCKED) {
            return; // 未锁定，无需释放
        }

        Departure departure = departureRepository.findById(order.getDepartureId())
            .orElseThrow(() -> new BusinessException("error.departure.notFound"));

        Integer stock = departure.getStock();
        if (stock == null) {
            stock = 0;
        }

        departure.setStock(stock + order.getTravelerCount());
        departureRepository.save(departure);

        order.setInventoryStatus(InventoryStatus.RELEASED);
        orderRepository.save(order);

        log.info("库存已释放 - 订单ID: {}, 出团ID: {}, 释放数量: {}",
            order.getId(), departure.getId(), order.getTravelerCount());
    }

    /**
     * 添加订单状态日志。
     *
     * @param orderId 订单ID
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param remark 备注
     */
    public void addStatusLog(String orderId, Object fromStatus, Object toStatus, String remark) {
        OrderStatusLog logEntry = new OrderStatusLog();
        logEntry.setOrderId(orderId);
        logEntry.setFromStatus(fromStatus == null ? null : fromStatus.toString());
        logEntry.setToStatus(toStatus.toString());
        logEntry.setRemark(remark);
        logRepository.save(logEntry);
    }

    /**
     * 刷新订单的财务状态。
     *
     * <p>根据应收应付情况更新订单的支付状态和结算状态。
     *
     * @param order 订单
     */
    @Transactional
    public void refreshFinanceState(TravelOrder order) {
        // 这里应该调用财务服务来获取应收应付信息
        // 为简化实现，这里只做占位符

        // 示例逻辑（实际应从财务服务获取）：
        BigDecimal receivableAmount = BigDecimal.ZERO; // 应收总额
        BigDecimal receivedAmount = BigDecimal.ZERO;    // 已收总额
        BigDecimal payableAmount = BigDecimal.ZERO;     // 应付总额
        BigDecimal paidAmount = BigDecimal.ZERO;        // 已付总额

        // 更新支付状态
        if (receivedAmount.compareTo(receivableAmount) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else if (receivedAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        } else {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }

        // 更新结算状态
        if (paidAmount.compareTo(payableAmount) >= 0 && receivedAmount.compareTo(receivableAmount) >= 0) {
            order.setSettlementStatus(SettlementStatus.SETTLED);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0 || receivedAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setSettlementStatus(SettlementStatus.PARTIAL);
        } else {
            order.setSettlementStatus(SettlementStatus.UNSETTLED);
        }

        orderRepository.save(order);

        log.debug("订单财务状态已刷新 - 订单ID: {}", order.getId());
    }
}