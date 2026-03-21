package com.jincai.crm.common;

import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.repository.DepartmentRepository;
import com.jincai.crm.security.LoginUser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DataScopeResolver {

    private final DepartmentRepository departmentRepository;

    public DataScopeResolver(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Set<Long> resolveDepartmentIds(LoginUser user) {
        if (user == null || user.getDepartmentId() == null) {
            return Set.of();
        }
        if (user.getDataScope() == DataScope.DEPARTMENT) {
            return Set.of(user.getDepartmentId());
        }
        if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
            List<Department> departments = departmentRepository.findByDeletedFalse();
            Map<Long, List<Long>> childrenMap = new HashMap<>();
            for (Department department : departments) {
                childrenMap.computeIfAbsent(department.getParentId(), k -> new ArrayList<>()).add(department.getId());
            }
            Set<Long> ids = new HashSet<>();
            ArrayDeque<Long> queue = new ArrayDeque<>();
            queue.add(user.getDepartmentId());
            while (!queue.isEmpty()) {
                Long current = queue.poll();
                if (!ids.add(current)) {
                    continue;
                }
                for (Long childId : childrenMap.getOrDefault(current, List.of())) {
                    queue.add(childId);
                }
            }
            return ids;
        }
        return Set.of();
    }
}

