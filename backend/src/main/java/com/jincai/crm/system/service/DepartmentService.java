package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.system.dto.DepartmentRequest;
import com.jincai.crm.system.dto.DepartmentTreeView;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.entity.OrgUser;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.OrgUserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
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
        Department department = new Department();
        department.setName(request.name());
        department.setParentId(request.parentId());
        department.setLeaderUserId(request.leaderUserId());
        department.setSortOrder(request.sort());
        department.setTreePath(buildTreePath(request.parentId()));
        return departmentRepository.save(department);
    }

    public Department update(String id, DepartmentRequest request) {
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
        return saved;
    }

    public void delete(String id) {
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


