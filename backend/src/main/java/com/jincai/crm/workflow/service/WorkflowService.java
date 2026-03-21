package com.jincai.crm.workflow.service;

import com.jincai.crm.workflow.dto.*;
import com.jincai.crm.workflow.entity.*;
import com.jincai.crm.workflow.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.notification.entity.Notification;
import com.jincai.crm.notification.repository.NotificationRepository;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.entity.Role;
import com.jincai.crm.org.repository.RoleRepository;
import com.jincai.crm.org.entity.UserRole;
import com.jincai.crm.org.repository.UserRoleRepository;
import com.jincai.crm.security.LoginUser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final NotificationRepository notificationRepository;

    public WorkflowService(WorkflowTemplateRepository templateRepository,
                           WorkflowTemplateNodeRepository templateNodeRepository,
                           WorkflowInstanceRepository instanceRepository,
                           WorkflowInstanceNodeRepository instanceNodeRepository,
                           AppUserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           NotificationRepository notificationRepository) {
        this.templateRepository = templateRepository;
        this.templateNodeRepository = templateNodeRepository;
        this.instanceRepository = instanceRepository;
        this.instanceNodeRepository = instanceNodeRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<WorkflowTemplateView> listTemplates() {
        return templateRepository.findAll().stream()
            .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
            .map(t -> new WorkflowTemplateView(t, templateNodeRepository.findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(t.getId())))
            .toList();
    }

    @Transactional
    public WorkflowTemplate saveTemplate(WorkflowTemplateRequest request) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName(request.name());
        template.setOrderType(request.orderType());
        template.setProductCategory(request.productCategory());
        template.setMinAmount(request.minAmount());
        template.setMaxAmount(request.maxAmount());
        template.setActive(request.active() == null ? true : request.active());
        WorkflowTemplate saved = templateRepository.save(template);
        saveTemplateNodes(saved.getId(), request.nodes());
        return saved;
    }

    @Transactional
    public WorkflowTemplate updateTemplate(Long id, WorkflowTemplateRequest request) {
        WorkflowTemplate template = templateRepository.findById(id).orElseThrow(() -> new BusinessException("Workflow template not found"));
        if (Boolean.TRUE.equals(template.getDeleted())) {
            throw new BusinessException("Workflow template not found");
        }
        template.setName(request.name());
        template.setOrderType(request.orderType());
        template.setProductCategory(request.productCategory());
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
        WorkflowTemplate template = templateRepository.findById(id).orElseThrow(() -> new BusinessException("Workflow template not found"));
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
            .filter(t -> withinAmount(t, order.getTotalAmount()))
            .findFirst()
            .orElseThrow(() -> new BusinessException("No workflow template matched"));
    }

    @Transactional
    public void startWorkflow(TravelOrder order) {
        WorkflowTemplate template = matchTemplate(order);
        List<WorkflowTemplateNode> templateNodes = templateNodeRepository.findByTemplateIdAndDeletedFalseOrderByStepOrderAsc(template.getId());
        if (templateNodes.isEmpty()) {
            throw new BusinessException("Workflow template has no nodes");
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
            .orElseThrow(() -> new BusinessException("Workflow instance not found"));
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("Workflow is not running");
        }
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("Current workflow node not found"));
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
            .orElseThrow(() -> new BusinessException("Workflow instance not found"));
        WorkflowInstanceNode node = instanceNodeRepository
            .findByInstanceIdAndStepOrderAndDeletedFalse(instance.getId(), instance.getCurrentStep())
            .orElseThrow(() -> new BusinessException("Current workflow node not found"));
        checkApprover(currentUser, node.getApproverRoleCode());
        node.setStatus("REJECTED");
        node.setComment(comment);
        instanceNodeRepository.save(node);
        instance.setStatus("REJECTED");
        instanceRepository.save(instance);
    }

    private void saveTemplateNodes(Long templateId, List<WorkflowNodeRequest> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("Workflow template must contain at least one node");
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
            throw new BusinessException("Current user has no permission to approve this node");
        }
    }

    private boolean withinAmount(WorkflowTemplate template, BigDecimal amount) {
        boolean minPass = template.getMinAmount() == null || amount.compareTo(template.getMinAmount()) >= 0;
        boolean maxPass = template.getMaxAmount() == null || amount.compareTo(template.getMaxAmount()) <= 0;
        return minPass && maxPass;
    }

    private void sendNodeNotification(Long instanceId, Integer step, String orderNo) {
        WorkflowInstanceNode node = instanceNodeRepository.findByInstanceIdAndStepOrderAndDeletedFalse(instanceId, step)
            .orElseThrow(() -> new BusinessException("Pending node not found"));
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
                notification.setContent("Order " + orderNo + " pending your approval");
                notificationRepository.save(notification);
            });
    }
}
