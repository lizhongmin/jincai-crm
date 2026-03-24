package com.jincai.crm.workflow.service;

import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.repository.NotificationRepository;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.security.LoginUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowTemplateNodeRepository templateNodeRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowInstanceNodeRepository instanceNodeRepository;
    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RouteProductRepository routeRepository;
    private final DepartureRepository departureRepository;
    private final NotificationRepository notificationRepository;
    private final I18nService i18nService;

    public WorkflowService(WorkflowTemplateRepository templateRepository,
                           WorkflowTemplateNodeRepository templateNodeRepository,
                           WorkflowInstanceRepository instanceRepository,
                           WorkflowInstanceNodeRepository instanceNodeRepository,
                           AppUserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           RouteProductRepository routeRepository,
                           DepartureRepository departureRepository,
                           NotificationRepository notificationRepository,
                           I18nService i18nService) {
        this.templateRepository = templateRepository;
        this.templateNodeRepository = templateNodeRepository;
        this.instanceRepository = instanceRepository;
        this.instanceNodeRepository = instanceNodeRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.routeRepository = routeRepository;
        this.departureRepository = departureRepository;
        this.notificationRepository = notificationRepository;
        this.i18nService = i18nService;
    }

    public List<WorkflowTemplateView> listTemplates() {
        return templateRepository.findAll().stream()
            .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
            .map(t -> new WorkflowTemplateView(t, templateNodeRepository.findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(t.getId())))
            .toList();
    }

    public PageResult<WorkflowTemplateView> pageTemplates(int page, int size, String keyword) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<WorkflowTemplate> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("name"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("orderType"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("productCategory"), "")), likeValue)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<WorkflowTemplate> result = templateRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        List<WorkflowTemplateView> views = result.getContent().stream()
            .map(t -> new WorkflowTemplateView(t, templateNodeRepository.findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(t.getId())))
            .toList();
        return new PageResult<>(views, result.getTotalElements(), normalizedPage, normalizedSize);
    }

    public WorkflowContextOptionsView contextOptions() {
        return new WorkflowContextOptionsView(
            roleRepository.findByDeletedFalse(),
            routeRepository.findByDeletedFalse(),
            departureRepository.findByDeletedFalse()
        );
    }

    @Transactional
    public WorkflowTemplate saveTemplate(WorkflowTemplateRequest request) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName(request.name());
        template.setOrderType(request.orderType());
        template.setProductCategory(request.productCategory());
        template.setRouteId(request.routeId());
        template.setDepartureId(request.departureId());
        template.setMinAmount(request.minAmount());
        template.setMaxAmount(request.maxAmount());
        template.setActive(request.active() == null ? true : request.active());
        WorkflowTemplate saved = templateRepository.save(template);
        saveTemplateNodes(saved.getId(), request.nodes());
        return saved;
    }

    @Transactional
    public WorkflowTemplate updateTemplate(Long id, WorkflowTemplateRequest request) {
        WorkflowTemplate template = templateRepository.findById(id).orElseThrow(() -> new BusinessException("error.workflow.template.notFound"));
        if (Boolean.TRUE.equals(template.getDeleted())) {
            throw new BusinessException("error.workflow.template.notFound");
        }
        template.setName(request.name());
        template.setOrderType(request.orderType());
        template.setProductCategory(request.productCategory());
        template.setRouteId(request.routeId());
        template.setDepartureId(request.departureId());
        template.setMinAmount(request.minAmount());
        template.setMaxAmount(request.maxAmount());
        template.setActive(request.active() == null ? true : request.active());
        WorkflowTemplate updated = templateRepository.save(template);

        List<WorkflowTemplateNode> oldNodes = templateNodeRepository.findByTemplateIdAndDeletedFalse(id);
        oldNodes.forEach(node -> {
            node.setDeleted(true);
            templateNodeRepository.save(node);
        });
        saveTemplateNodes(updated.getId(), request.nodes());
        return updated;
    }

    @Transactional
    public void deleteTemplate(Long id) {
        WorkflowTemplate template = templateRepository.findById(id).orElseThrow(() -> new BusinessException("error.workflow.template.notFound"));
        if (Boolean.TRUE.equals(template.getDeleted())) {
            return;
        }
        template.setDeleted(true);
        templateRepository.save(template);

        List<WorkflowTemplateNode> nodes = templateNodeRepository.findByTemplateIdAndDeletedFalse(id);
        nodes.forEach(node -> {
            node.setDeleted(true);
            templateNodeRepository.save(node);
        });
    }

    public WorkflowTemplate matchTemplate(TravelOrder order) {
        List<WorkflowTemplate> candidates = templateRepository.findByActiveTrueAndDeletedFalse();
        return candidates.stream()
            .filter(t -> t.getOrderType().equalsIgnoreCase(order.getOrderType()))
            .filter(t -> t.getProductCategory().equalsIgnoreCase(order.getProductCategory()))
            .filter(t -> t.getRouteId() == null || t.getRouteId().equals(order.getRouteId()))
            .filter(t -> t.getDepartureId() == null || t.getDepartureId().equals(order.getDepartureId()))
            .filter(t -> withinAmount(t, order.getTotalAmount()))
            .sorted(Comparator
                .comparingInt((WorkflowTemplate t) -> templateSpecificityScore(order, t)).reversed()
                .thenComparing(t -> t.getMinAmount() == null ? BigDecimal.ZERO : t.getMinAmount(), Comparator.reverseOrder()))
            .findFirst()
            .orElseThrow(() -> new BusinessException("error.workflow.template.notMatched"));
    }

    @Transactional
    public void startWorkflow(TravelOrder order) {
        closeActiveInstance(order.getId(), "REPLACED");
        WorkflowTemplate template = matchTemplate(order);
        List<WorkflowTemplateNode> templateNodes = templateNodeRepository.findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(template.getId());
        if (templateNodes.isEmpty()) {
            throw new BusinessException("error.workflow.template.noNodes");
        }
        WorkflowInstance instance = new WorkflowInstance();
        instance.setTemplateId(template.getId());
        instance.setOrderId(order.getId());
        instance.setCurrentStep(templateNodes.get(0).getStepOrder());
        instance.setStatus("RUNNING");
        WorkflowInstance savedInstance = instanceRepository.save(instance);

        templateNodes.forEach(node -> {
            WorkflowInstanceNode in = new WorkflowInstanceNode();
            in.setInstanceId(savedInstance.getId());
            in.setStepOrder(node.getStepOrder());
            in.setNodeName(node.getNodeName());
            in.setApproverRoleCode(node.getApproverRoleCode());
            in.setStatus(node.getStepOrder().equals(savedInstance.getCurrentStep()) ? "PENDING" : "WAITING");
            instanceNodeRepository.save(in);
        });

        sendNodeNotification(savedInstance.getId(), savedInstance.getCurrentStep(), order.getOrderNo());
    }

    @Transactional
    public boolean approve(Long orderId, LoginUser currentUser, String comment) {
        WorkflowInstance instance = instanceRepository.findByOrderIdAndDeletedFalse(orderId)
            .orElseThrow(() -> new BusinessException("error.workflow.instance.notFound"));
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("error.workflow.instance.notRunning");
        }
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("error.workflow.node.currentNotFound"));
        checkApprover(currentUser, node.getApproverRoleCode());
        node.setStatus("APPROVED");
        node.setComment(comment);
        instanceNodeRepository.save(node);

        List<WorkflowInstanceNode> allNodes = instanceNodeRepository.findByInstanceIdAndDeletedFalseOrderByStepOrderAsc(instance.getId());
        Optional<WorkflowInstanceNode> next = allNodes.stream().filter(n -> n.getStepOrder() > node.getStepOrder()).findFirst();
        if (next.isEmpty()) {
            instance.setStatus("APPROVED");
            instanceRepository.save(instance);
            return true;
        }

        WorkflowInstanceNode nextNode = next.get();
        nextNode.setStatus("PENDING");
        instanceNodeRepository.save(nextNode);
        instance.setCurrentStep(nextNode.getStepOrder());
        instanceRepository.save(instance);
        sendNodeNotification(instance.getId(), nextNode.getStepOrder(), "ORDER-" + orderId);
        return false;
    }

    @Transactional
    public void reject(Long orderId, LoginUser currentUser, String comment) {
        WorkflowInstance instance = instanceRepository.findByOrderIdAndDeletedFalse(orderId)
            .orElseThrow(() -> new BusinessException("error.workflow.instance.notFound"));
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("error.workflow.instance.notRunning");
        }
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("error.workflow.node.currentNotFound"));
        checkApprover(currentUser, node.getApproverRoleCode());
        node.setStatus("REJECTED");
        node.setComment(comment);
        instanceNodeRepository.save(node);
        instance.setStatus("REJECTED");
        instanceRepository.save(instance);
    }

    @Transactional
    public void withdraw(Long orderId, LoginUser currentUser, String comment) {
        WorkflowInstance instance = instanceRepository.findByOrderIdAndDeletedFalse(orderId)
            .orElseThrow(() -> new BusinessException("error.workflow.instance.notFound"));
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("error.workflow.instance.notRunning");
        }
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("error.workflow.node.currentNotFound"));
        if (currentUser == null) {
            throw new BusinessException("error.auth.required");
        }
        node.setStatus("WITHDRAWN");
        node.setComment(comment == null || comment.isBlank() ? i18nService.getMessage("common.workflow.withdrawnBySubmitter") : comment);
        instanceNodeRepository.save(node);
        instance.setStatus("WITHDRAWN");
        instanceRepository.save(instance);
    }

    @Transactional
    public void transfer(Long orderId, LoginUser currentUser, String targetRoleCode, String comment) {
        if (targetRoleCode == null || targetRoleCode.isBlank()) {
            throw new BusinessException("error.workflow.transfer.targetRole.required");
        }
        WorkflowInstance instance = instanceRepository.findByOrderIdAndDeletedFalse(orderId)
            .orElseThrow(() -> new BusinessException("error.workflow.instance.notFound"));
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("error.workflow.instance.notRunning");
        }
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("error.workflow.node.currentNotFound"));
        checkApprover(currentUser, node.getApproverRoleCode());

        node.setApproverRoleCode(targetRoleCode.trim().toUpperCase());
        node.setComment(comment == null || comment.isBlank()
            ? i18nService.getMessage("common.workflow.transferredAt", LocalDateTime.now())
            : comment);
        instanceNodeRepository.save(node);
    }

    private void saveTemplateNodes(Long templateId, List<WorkflowNodeRequest> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("error.workflow.template.nodeRequired");
        }
        List<WorkflowNodeRequest> sorted = nodes.stream()
            .sorted(Comparator.comparing(WorkflowNodeRequest::stepOrder))
            .toList();
        sorted.forEach(nodeRequest -> {
            WorkflowTemplateNode node = new WorkflowTemplateNode();
            node.setTemplateId(templateId);
            node.setStepOrder(nodeRequest.stepOrder());
            node.setNodeName(nodeRequest.nodeName());
            node.setApproverRoleCode(nodeRequest.approverRoleCode());
            templateNodeRepository.save(node);
        });
    }

    private void checkApprover(LoginUser user, String approverRoleCode) {
        if (user == null || user.getRoleCodes() == null || user.getRoleCodes().stream().noneMatch(approverRoleCode::equalsIgnoreCase)) {
            throw new BusinessException("error.workflow.node.noApprovePermission");
        }
    }

    private boolean withinAmount(WorkflowTemplate template, BigDecimal amount) {
        boolean minPass = template.getMinAmount() == null || amount.compareTo(template.getMinAmount()) >= 0;
        boolean maxPass = template.getMaxAmount() == null || amount.compareTo(template.getMaxAmount()) <= 0;
        return minPass && maxPass;
    }

    private int templateSpecificityScore(TravelOrder order, WorkflowTemplate template) {
        int score = 0;
        if (template.getRouteId() != null && template.getRouteId().equals(order.getRouteId())) {
            score += 10;
        }
        if (template.getDepartureId() != null && template.getDepartureId().equals(order.getDepartureId())) {
            score += 20;
        }
        if (template.getMinAmount() != null) {
            score += 2;
        }
        if (template.getMaxAmount() != null) {
            score += 2;
        }
        return score;
    }

    private void closeActiveInstance(Long orderId, String reason) {
        instanceRepository.findByOrderIdAndDeletedFalse(orderId).ifPresent(instance -> {
            if (!"APPROVED".equalsIgnoreCase(instance.getStatus())
                && !"REJECTED".equalsIgnoreCase(instance.getStatus())
                && !"WITHDRAWN".equalsIgnoreCase(instance.getStatus())
                && !"REPLACED".equalsIgnoreCase(instance.getStatus())) {
                instance.setStatus(reason);
                instanceRepository.save(instance);
                instanceNodeRepository.findByInstanceIdAndDeletedFalseOrderByStepOrderAsc(instance.getId())
                    .stream()
                    .filter(node -> "PENDING".equalsIgnoreCase(node.getStatus()) || "WAITING".equalsIgnoreCase(node.getStatus()))
                    .forEach(node -> {
                        node.setStatus(reason);
                        instanceNodeRepository.save(node);
                    });
            }
        });
    }

    private void sendNodeNotification(Long instanceId, Integer step, String orderNo) {
        WorkflowInstanceNode node = instanceNodeRepository.findByInstanceIdAndStepOrderAndDeletedFalse(instanceId, step)
            .orElseThrow(() -> new BusinessException("error.workflow.node.pendingNotFound"));
        List<AppUser> users = userRepository.findByDeletedFalse();
        List<UserRole> userRoles = new ArrayList<>();
        users.forEach(user -> userRoles.addAll(userRoleRepository.findByUserIdAndDeletedFalse(user.getId())));
        Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, Role> roleMap = roleRepository.findAllById(roleIds).stream().collect(Collectors.toMap(Role::getId, Function.identity()));
        userRoles.stream()
            .filter(ur -> {
                Role role = roleMap.get(ur.getRoleId());
                return role != null && node.getApproverRoleCode().equalsIgnoreCase(role.getCode());
            })
            .map(UserRole::getUserId)
            .distinct()
            .forEach(userId -> {
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setContent("Order " + (orderNo == null ? ("ORDER-" + instanceId) : orderNo) + " pending your approval");
                notificationRepository.save(notification);
            });
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

