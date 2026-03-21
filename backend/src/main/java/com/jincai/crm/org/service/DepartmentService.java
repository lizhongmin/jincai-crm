package com.jincai.crm.org.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.org.dto.DepartmentRequest;
import com.jincai.crm.org.dto.DepartmentTreeView;
import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.repository.DepartmentRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AppUserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, AppUserRepository userRepository) {
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
        department.setTreePath(buildTreePath(request.parentId()));
        return departmentRepository.save(department);
    }

    public Department update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Department not found"));
        if (Boolean.TRUE.equals(department.getDeleted())) {
            throw new BusinessException("Department not found");
        }
        validateParentSelection(id, request.parentId());
        validateRootDepartmentGuaranteeOnMove(department, request.parentId());

        department.setName(request.name());
        department.setParentId(request.parentId());
        department.setLeaderUserId(request.leaderUserId());
        department.setTreePath(buildTreePath(request.parentId()));
        Department saved = departmentRepository.save(department);
        refreshDescendantTreePath(saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Department not found"));
        if (Boolean.TRUE.equals(department.getDeleted())) {
            throw new BusinessException("Department not found");
        }
        if (!departmentRepository.findByParentIdAndDeletedFalse(id).isEmpty()) {
            throw new BusinessException("Department has child departments and cannot be deleted");
        }
        if (userRepository.existsByDepartmentIdAndDeletedFalse(id)) {
            throw new BusinessException("Department has users and cannot be deleted");
        }
        if (department.getParentId() == null && departmentRepository.countByParentIdIsNullAndDeletedFalse() <= 1) {
            throw new BusinessException("At least one top-level department must be retained");
        }
        department.setDeleted(true);
        departmentRepository.save(department);
    }

    private void validateParentSelection(Long id, Long parentId) {
        if (parentId == null) {
            return;
        }
        if (id.equals(parentId)) {
            throw new BusinessException("Parent department cannot be itself");
        }
        Department parent = departmentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException("Parent department not found"));
        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("Parent department not found");
        }

        Set<Long> descendantIds = collectDescendantIds(id);
        if (descendantIds.contains(parentId)) {
            throw new BusinessException("Parent department cannot be a child department");
        }
    }

    private void validateRootDepartmentGuaranteeOnMove(Department department, Long newParentId) {
        if (department.getParentId() == null && newParentId != null
            && departmentRepository.countByParentIdIsNullAndDeletedFalse() <= 1) {
            throw new BusinessException("At least one top-level department must be retained");
        }
    }

    private Set<Long> collectDescendantIds(Long rootId) {
        Map<Long, List<Department>> childrenMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.groupingBy(d -> d.getParentId() == null ? -1L : d.getParentId()));
        Set<Long> ids = new java.util.HashSet<>();
        java.util.ArrayDeque<Long> queue = new java.util.ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            List<Department> children = childrenMap.getOrDefault(current, List.of());
            for (Department child : children) {
                if (ids.add(child.getId())) {
                    queue.add(child.getId());
                }
            }
        }
        return ids;
    }

    private void refreshDescendantTreePath(Long rootId) {
        Map<Long, Department> map = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, d -> d));
        Department root = map.get(rootId);
        if (root == null) {
            return;
        }
        Map<Long, List<Department>> childrenMap = map.values().stream()
            .collect(Collectors.groupingBy(d -> d.getParentId() == null ? -1L : d.getParentId()));
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

    private String buildTreePath(Long parentId) {
        if (parentId == null) {
            return "/";
        }
        Department parent = departmentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException("Parent department not found"));
        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("Parent department not found");
        }
        return parent.getTreePath() + parent.getId() + "/";
    }

    private List<DepartmentTreeView> buildTreeViews() {
        List<Department> departments = departmentRepository.findByDeletedFalse();
        Map<Long, Department> departmentMap = departments.stream()
            .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, LinkedHashMap::new));

        Map<Long, String> userNameMap = userRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(AppUser::getId, AppUser::getFullName, (a, b) -> a, LinkedHashMap::new));

        Map<Long, Boolean> hasUsersMap = new LinkedHashMap<>();
        for (Department department : departments) {
            hasUsersMap.put(department.getId(), userRepository.existsByDepartmentIdAndDeletedFalse(department.getId()));
        }

        Map<Long, Boolean> hasChildrenMap = new LinkedHashMap<>();
        for (Department department : departments) {
            hasChildrenMap.put(department.getId(), false);
        }
        for (Department department : departments) {
            if (department.getParentId() != null && hasChildrenMap.containsKey(department.getParentId())) {
                hasChildrenMap.put(department.getParentId(), true);
            }
        }

        Map<Long, DepartmentTreeView> nodeMap = new LinkedHashMap<>();
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
                    hasChildrenMap.getOrDefault(department.getId(), false)
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
        return roots;
    }
}

