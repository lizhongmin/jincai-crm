package com.jincai.crm.common;

import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.repository.DepartmentRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataScopeResolver {

    private final DepartmentRepository departmentRepository;

    public DataScopeResolver(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Set<String> resolveDepartmentIds(LoginUser user) {
        if (user == null || user.getDepartmentId() == null) {
            return Set.of();
        }
        if (user.getDataScope() == DataScope.DEPARTMENT) {
            return Set.of(user.getDepartmentId());
        }
        if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
            List<Department> departments = departmentRepository.findByDeletedFalse();
            Map<String, List<String>> childrenMap = new HashMap<>();
            for (Department department : departments) {
                childrenMap.computeIfAbsent(department.getParentId(), k -> new ArrayList<>()).add(department.getId());
            }
            Set<String> ids = new HashSet<>();
            ArrayDeque<String> queue = new ArrayDeque<>();
            queue.add(user.getDepartmentId());
            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (!ids.add(current)) {
                    continue;
                }
                for (String childId : childrenMap.getOrDefault(current, List.of())) {
                    queue.add(childId);
                }
            }
            return ids;
        }
        return Set.of();
    }
}

