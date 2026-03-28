package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.system.dto.DepartmentRequest;
import com.jincai.crm.system.dto.DepartmentTreeView;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.entity.OrgUser;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.OrgUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrgUserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, OrgUserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<Department> list() {
        return departmentRepository.findByDeletedFalse();
    }

    public List<DepartmentTreeView> tree() {
        return buildTreeViews();
    }

    public Department create(DepartmentRequest request) {
        log.info("创建部门 - 部门名称: {}, 父部门ID: {}, 负责人: {}, 排序: {}",
                request.name(), request.parentId(), request.leaderUserId(), request.sort());
        try {
            Department department = new Department();
            department.setName(request.name());
            department.setParentId(request.parentId());
            department.setLeaderUserId(request.leaderUserId());
            department.setSortOrder(request.sort());
            department.setTreePath(buildTreePath(request.parentId()));
            Department saved = departmentRepository.save(department);
            log.info("部门创建成功 - 部门ID: {}, 名称: {}", saved.getId(), saved.getName());
            return saved;
        } catch (Exception e) {
            log.error("创建部门失败 - 部门名称: {}", request.name(), e);
            throw e;
        }
    }

    public Department update(String id, DepartmentRequest request) {
        log.info("更新部门 - 部门ID: {}, 部门名称: {}, 父部门ID: {}, 负责人: {}, 排序: {}",
                id, request.name(), request.parentId(), request.leaderUserId(), request.sort());
        try {
            Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("error.department.notFound"));
            if (Boolean.TRUE.equals(department.getDeleted())) {
                throw new BusinessException("error.department.notFound");
            }
            validateParentSelection(id, request.parentId());
            validateRootDepartmentGuaranteeOnMove(department, request.parentId());

            department.setName(request.name());
            department.setParentId(request.parentId());
            department.setLeaderUserId(request.leaderUserId());
            department.setSortOrder(request.sort());
            department.setTreePath(buildTreePath(request.parentId()));
            Department saved = departmentRepository.save(department);
            refreshDescendantTreePath(saved.getId());
            log.info("部门更新成功 - 部门ID: {}, 名称: {}", saved.getId(), saved.getName());
            return saved;
        } catch (Exception e) {
            log.error("更新部门失败 - 部门ID: {}", id, e);
            throw e;
        }
    }

    public void delete(String id) {
        log.info("删除部门 - 部门ID: {}", id);
        try {
            Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("error.department.notFound"));
            if (Boolean.TRUE.equals(department.getDeleted())) {
                throw new BusinessException("error.department.notFound");
            }
            if (!departmentRepository.findByParentIdAndDeletedFalse(id).isEmpty()) {
                throw new BusinessException("error.department.delete.hasChildren");
            }
            if (userRepository.existsByDepartmentIdAndDeletedFalse(id)) {
                throw new BusinessException("error.department.delete.hasUsers");
            }
            if (department.getParentId() == null && departmentRepository.countByParentIdIsNullAndDeletedFalse() <= 1) {
                throw new BusinessException("error.department.root.required");
            }
            department.setDeleted(true);
            departmentRepository.save(department);
            log.info("部门删除成功 - 部门ID: {}", id);
        } catch (Exception e) {
            log.error("删除部门失败 - 部门ID: {}", id, e);
            throw e;
        }
    }

    private void validateParentSelection(String id, String parentId) {
        if (parentId == null) {
            return;
        }
        if (id.equals(parentId)) {
            throw new BusinessException("error.department.parent.self");
        }
        Department parent = departmentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException("error.department.parent.notFound"));
        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("error.department.parent.notFound");
        }

        Set<String> descendantIds = collectDescendantIds(id);
        if (descendantIds.contains(parentId)) {
            throw new BusinessException("error.department.parent.circular");
        }
    }

    private void validateRootDepartmentGuaranteeOnMove(Department department, String newParentId) {
        if (department.getParentId() == null && newParentId != null
            && departmentRepository.countByParentIdIsNullAndDeletedFalse() <= 1) {
            throw new BusinessException("error.department.root.required");
        }
    }

    private Set<String> collectDescendantIds(String rootId) {
        Map<String, List<Department>> childrenMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.groupingBy(d -> d.getParentId() == null ? "0" : d.getParentId()));
        Set<String> ids = new java.util.HashSet<>();
        java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<Department> children = childrenMap.getOrDefault(current, List.of());
            for (Department child : children) {
                if (ids.add(child.getId())) {
                    queue.add(child.getId());
                }
            }
        }
        return ids;
    }

    private void refreshDescendantTreePath(String rootId) {
        Map<String, Department> map = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, d -> d));
        Department root = map.get(rootId);
        if (root == null) {
            return;
        }
        Map<String, List<Department>> childrenMap = map.values().stream()
            .collect(Collectors.groupingBy(d -> d.getParentId() == null ? "0" : d.getParentId()));
        java.util.ArrayDeque<Department> queue = new java.util.ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Department current = queue.poll();
            String currentPath = current.getTreePath() + current.getId() + "/";
            for (Department child : childrenMap.getOrDefault(current.getId(), List.of())) {
                child.setTreePath(currentPath);
                departmentRepository.save(child);
                queue.add(child);
            }
        }
    }

    private String buildTreePath(String parentId) {
        if (parentId == null) {
            return "/";
        }
        Department parent = departmentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException("error.department.parent.notFound"));
        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("error.department.parent.notFound");
        }
        return parent.getTreePath() + parent.getId() + "/";
    }

    private static final Comparator<DepartmentTreeView> DEPARTMENT_TREE_VIEW_COMPARATOR = Comparator
        .comparing((DepartmentTreeView d) -> d.sort() == null ? Integer.MAX_VALUE : d.sort())
        .thenComparing(DepartmentTreeView::name);

    private List<DepartmentTreeView> buildTreeViews() {
        List<Department> departments = departmentRepository.findByDeletedFalse();
        Map<String, Department> departmentMap = departments.stream()
            .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, LinkedHashMap::new));

        Map<String, String> userNameMap = userRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(OrgUser::getId, OrgUser::getFullName, (a, b) -> a, LinkedHashMap::new));

        Map<String, Boolean> hasUsersMap = new LinkedHashMap<>();
        for (Department department : departments) {
            hasUsersMap.put(department.getId(), userRepository.existsByDepartmentIdAndDeletedFalse(department.getId()));
        }

        Map<String, Boolean> hasChildrenMap = new LinkedHashMap<>();
        for (Department department : departments) {
            hasChildrenMap.put(department.getId(), false);
        }
        for (Department department : departments) {
            if (department.getParentId() != null && hasChildrenMap.containsKey(department.getParentId())) {
                hasChildrenMap.put(department.getParentId(), true);
            }
        }

        Map<String, DepartmentTreeView> nodeMap = new LinkedHashMap<>();
        departments.forEach(department -> {
            Department parent = department.getParentId() == null ? null : departmentMap.get(department.getParentId());
            nodeMap.put(
                department.getId(),
                new DepartmentTreeView(
                    department.getId(),
                    department.getName(),
                    department.getParentId(),
                    parent == null ? null : parent.getName(),
                    department.getTreePath(),
                    department.getLeaderUserId(),
                    department.getLeaderUserId() == null ? null : userNameMap.get(department.getLeaderUserId()),
                    hasUsersMap.getOrDefault(department.getId(), false),
                    hasChildrenMap.getOrDefault(department.getId(), false),
                    department.getSortOrder()
                )
            );
        });

        List<DepartmentTreeView> roots = new ArrayList<>();
        nodeMap.values().forEach(node -> {
            if (node.parentId() == null) {
                roots.add(node);
                return;
            }
            DepartmentTreeView parent = nodeMap.get(node.parentId());
            if (parent == null) {
                roots.add(node);
                return;
            }
            parent.children().add(node);
        });

        roots.sort(DEPARTMENT_TREE_VIEW_COMPARATOR);
        nodeMap.values().forEach(node -> {
            if (node.children() != null && !node.children().isEmpty()) {
                node.children().sort(DEPARTMENT_TREE_VIEW_COMPARATOR);
            }
        });

        return roots;
    }
}


