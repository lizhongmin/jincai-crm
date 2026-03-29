package com.jincai.crm.common;

import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据权限解析器。
 *
 * <p>根据用户的登录信息和数据权限范围，解析出用户可以访问的部门ID集合。
 * 用于实现基于组织架构的数据行级访问控制（Row Level Security）。
 *
 * <p>支持的数据权限范围：
 * <ul>
 *   <li>{@link DataScope#SELF}：仅限用户自己创建的数据</li>
 *   <li>{@link DataScope#DEPARTMENT}：用户所在部门的数据</li>
 *   <li>{@link DataScope#DEPARTMENT_TREE}：用户所在部门及其所有下级部门的数据</li>
 *   <li>{@link DataScope#ALL}：所有数据（无部门限制）</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Service
 * public class OrderService {
 *     private final DataScopeResolver dataScopeResolver;
 *
 *     public List<TravelOrder> listOrders(LoginUser user) {
 *         if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
 *             Set<String> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
 *             return orderRepository.findBySalesDeptIdInAndDeletedFalse(departmentIds);
 *         }
 *         // 其他数据权限处理...
 *     }
 * }
 * }</pre>
 *
 * @see LoginUser 获取当前登录用户信息
 * @see DataScope  数据权限范围枚举
 */
@Slf4j
@Component
public class DataScopeResolver {

    private final DepartmentRepository departmentRepository;

    public DataScopeResolver(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * 解析用户可访问的部门ID集合。
     *
     * <p>根据用户的数据权限范围，返回其可访问的部门ID集合：
     * <ul>
     *   <li>{@code SELF/ALL}：返回空集合，表示无部门限制或由业务层特殊处理</li>
     *   <li>{@code DEPARTMENT}：返回用户所在部门ID的单元素集合</li>
     *   <li>{@code DEPARTMENT_TREE}：通过BFS遍历部门树，返回用户部门及其所有子孙部门ID集合</li>
     * </ul>
     *
     * @param user 当前登录用户，若为 {@code null} 则返回空集合
     * @return 部门ID集合，可能为空
     */
    public Set<String> resolveDepartmentIds(LoginUser user) {
        if (user == null || user.getDepartmentId() == null) {
            return Set.of();
        }

        return switch (user.getDataScope()) {
            case DEPARTMENT -> Set.of(user.getDepartmentId());
            case DEPARTMENT_TREE -> resolveDepartmentTree(user.getDepartmentId());
            case SELF, ALL -> Set.of(); // SELF 由业务层处理，ALL 表示无限制
        };
    }

    /**
     * 解析部门树结构，获取指定部门及其所有子孙部门的ID集合。
     *
     * <p>使用广度优先搜索（BFS）算法遍历部门树，避免深度递归可能导致的栈溢出问题。
     * 算法复杂度：时间 O(N)，空间 O(N)，其中 N 为部门总数。
     *
     * <p><b>性能优化建议</b>：
     * <ul>
     *   <li>部门数据变化频率低，可考虑本地缓存 {@code parentId -> childrenId} 映射关系</li>
     *   <li>若部门树层级较深（>10层），可使用数据库递归查询（如MySQL 8.0的CTE）代替应用层遍历</li>
     * </ul>
     *
     * @param rootDepartmentId 根部门ID
     * @return 部门ID集合，包含根部门及其所有子孙部门
     */
    private Set<String> resolveDepartmentTree(String rootDepartmentId) {
        // 加载所有未删除部门，构建父子关系映射
        List<Department> departments = departmentRepository.findByDeletedFalse();
        Map<String, List<String>> childrenMap = new HashMap<>(departments.size());

        // 构建 parentId -> [childId, ...] 映射
        for (Department department : departments) {
            childrenMap.computeIfAbsent(department.getParentId(), k -> new ArrayList<>())
                       .add(department.getId());
        }

        // BFS遍历部门树
        Set<String> result = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(rootDepartmentId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            // 避免循环引用导致的重复处理
            if (!result.add(currentId)) {
                continue;
            }

            // 将所有子部门加入遍历队列
            List<String> children = childrenMap.getOrDefault(currentId, List.of());
            queue.addAll(children);
        }

        log.debug("部门树解析完成 - 根部门: {}, 子孙部门数: {}", rootDepartmentId, result.size() - 1);
        return result;
    }
}