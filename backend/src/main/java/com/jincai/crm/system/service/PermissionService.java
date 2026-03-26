package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.dto.PermissionRequest;
import com.jincai.crm.system.dto.PermissionTreeGroupView;
import com.jincai.crm.system.dto.PermissionTreeView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.entity.RolePermission;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public PermissionService(PermissionRepository permissionRepository, UserRoleRepository userRoleRepository,
                             RolePermissionRepository rolePermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public List<Permission> menus(LoginUser user) {
        if (user == null) {
            return Collections.emptyList();
        }
        Set<String> userPermissionCodes = user.getPermissionCodes() == null
            ? Set.of()
            : user.getPermissionCodes().stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(java.util.stream.Collectors.toSet());
        if (!userPermissionCodes.isEmpty()) {
            return permissionRepository.findByDeletedFalse().stream()
                .filter(p -> userPermissionCodes.contains(p.getCode()))
                .filter(p -> "MENU".equalsIgnoreCase(p.getType()) || "BUTTON".equalsIgnoreCase(p.getType()))
                .toList();
        }

        List<String> roleIds = userRoleRepository.findByUserIdAndDeletedFalse(user.getUserId()).stream()
            .map(UserRole::getRoleId)
            .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> permissionIds = rolePermissionRepository.findByRoleIdInAndDeletedFalse(roleIds).stream()
            .map(RolePermission::getPermissionId)
            .collect(java.util.stream.Collectors.toSet());
        return permissionRepository.findByIdIn(permissionIds).stream()
            .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
            .filter(p -> "MENU".equalsIgnoreCase(p.getType()) || "BUTTON".equalsIgnoreCase(p.getType()))
            .toList();
    }

    public List<Permission> list() {
        return permissionRepository.findByDeletedFalse().stream()
            .sorted(java.util.Comparator
                .comparing((Permission p) -> "MENU".equalsIgnoreCase(p.getType()) ? 0 : 1)
                .thenComparing(p -> p.getParentId() == null ? "0" : p.getParentId())
                .thenComparing(Permission::getCode))
            .toList();
    }

    /**
     * 构建3级权限树：
     * 1级 = parentId 为 null 且 type=MENU 的顶级菜单
     * 2级 = parentId 指向顶级菜单 且 type=MENU 的子菜单
     * 3级 = type != MENU 的权限点，挂在最近一级菜单下
     */
    public List<PermissionTreeGroupView> tree() {
        List<Permission> all = permissionRepository.findByDeletedFalse();
        Map<String, Permission> byId = all.stream()
            .filter(p -> p.getId() != null)
            .collect(java.util.stream.Collectors.toMap(Permission::getId, p -> p));

        // 按 parentId 分桶（所有子节点）
        Map<String, List<Permission>> childrenOf = all.stream()
            .filter(p -> p.getParentId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                Permission::getParentId,
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()
            ));

        // 1级：顶级菜单（parentId == null && type == MENU）
        List<Permission> topMenus = all.stream()
            .filter(p -> "MENU".equalsIgnoreCase(p.getType()) && p.getParentId() == null)
            .sorted(java.util.Comparator.comparing(Permission::getCode))
            .toList();

        java.util.Set<String> visited = new java.util.HashSet<>();
        List<PermissionTreeGroupView> result = new java.util.ArrayList<>();

        for (Permission top : topMenus) {
            visited.add(top.getId());
            List<Permission> topChildren = childrenOf.getOrDefault(top.getId(), List.of());

            // 2级：子菜单
            List<Permission> subMenus = topChildren.stream()
                .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                .sorted(java.util.Comparator.comparing(Permission::getCode))
                .toList();

            // 直接挂在顶级菜单下的权限点（无子菜单中间层）
            List<Permission> topActions = topChildren.stream()
                .filter(p -> !"MENU".equalsIgnoreCase(p.getType()))
                .sorted(java.util.Comparator.comparing(Permission::getCode))
                .toList();

            List<PermissionTreeGroupView.SubMenuView> subViews = new java.util.ArrayList<>();

            // 若顶级菜单下有直属权限点，生成一个与顶级同名的虚拟子分组
            if (!topActions.isEmpty()) {
                subViews.add(new PermissionTreeGroupView.SubMenuView(
                    top.getCode(), top.getName(), null, topActions
                ));
                topActions.forEach(p -> visited.add(p.getId()));
            }

            for (Permission sub : subMenus) {
                visited.add(sub.getId());
                List<Permission> actions = childrenOf.getOrDefault(sub.getId(), List.of()).stream()
                    .filter(p -> !"MENU".equalsIgnoreCase(p.getType()))
                    .sorted(java.util.Comparator.comparing(Permission::getCode))
                    .peek(p -> visited.add(p.getId()))
                    .toList();
                subViews.add(new PermissionTreeGroupView.SubMenuView(
                    sub.getCode(), sub.getName(), sub, actions
                ));
            }

            result.add(new PermissionTreeGroupView(top.getCode(), top.getName(), top, subViews));
        }

        // 未归属到任何顶级菜单的孤立权限点
        List<Permission> orphans = all.stream()
            .filter(p -> !visited.contains(p.getId()))
            .sorted(java.util.Comparator.comparing(Permission::getCode))
            .toList();
        if (!orphans.isEmpty()) {
            List<PermissionTreeGroupView.SubMenuView> orphanSubs = List.of(
                new PermissionTreeGroupView.SubMenuView("MISC", "其他权限", null, orphans)
            );
            result.add(new PermissionTreeGroupView("MISC", "其他权限", null, orphanSubs));
        }

        return result;
    }

    /**
     * 构建完整的权限树视图（用于管理界面）
     * @return 完整的权限树结构
     */
    public List<PermissionTreeView> treeView() {
        List<Permission> all = permissionRepository.findByDeletedFalse();

        all.sort(java.util.Comparator
            .comparing((Permission p) -> "MENU".equalsIgnoreCase(p.getType()) ? 0 : 1)
            .thenComparing(Permission::getCode));

        Map<String, PermissionTreeView> nodeMap = new LinkedHashMap<>();
        all.forEach(permission -> nodeMap.put(permission.getId(), new PermissionTreeView(permission)));

        List<PermissionTreeView> roots = new ArrayList<>();

        // 第一次遍历：只把根节点或孤儿节点加入 roots（不包含实际存在父节点的节点）
        for (Permission permission : all) {
            PermissionTreeView currentNode = nodeMap.get(permission.getId());
            String parentId = permission.getParentId();
            if (parentId == null || !nodeMap.containsKey(parentId)) {
                roots.add(currentNode);
            }
        }

        // 第二次遍历：挂载子节点。由于 java 的对象引用特性，这里更新 nodeMap 中的节点，roots 里的节点也会一并更新
        for (Permission permission : all) {
            String parentId = permission.getParentId();
            if (parentId != null) {
                PermissionTreeView parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    PermissionTreeView currentNode = nodeMap.get(permission.getId());
                    parentNode.children().add(currentNode);
                }
            }
        }

        return roots;
    }

    /**
     * 创建新的权限
     * @param request 权限创建请求
     * @return 创建的权限
     */
    public Permission create(PermissionRequest request) {
        // 检查编码唯一性
        if (permissionRepository.findByCodeAndDeletedFalse(request.code()).isPresent()) {
            throw new BusinessException("error.permission.code.exists");
        }

        Permission permission = new Permission();
        permission.setCode(request.code());
        permission.setName(request.name());
        permission.setType(request.type());
        permission.setMenuPath(request.menuPath());
        permission.setParentId(request.parentId());

        return permissionRepository.save(permission);
    }

    /**
     * 更新权限
     * @param id 权限ID
     * @param request 权限更新请求
     * @return 更新后的权限
     */
    public Permission update(String id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("error.permission.notFound"));
        if (Boolean.TRUE.equals(permission.getDeleted())) {
            throw new BusinessException("error.permission.notFound");
        }

        // 检查编码唯一性（排除自身）
        permissionRepository.findByCodeAndDeletedFalse(request.code())
            .filter(p -> !p.getId().equals(id))
            .ifPresent(p -> {
                throw new BusinessException("error.permission.code.exists");
            });

        permission.setCode(request.code());
        permission.setName(request.name());
        permission.setType(request.type());
        permission.setMenuPath(request.menuPath());
        permission.setParentId(request.parentId());

        return permissionRepository.save(permission);
    }

    /**
     * 删除权限
     * @param id 权限ID
     */
    public void delete(String id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("error.permission.notFound"));
        if (Boolean.TRUE.equals(permission.getDeleted())) {
            throw new BusinessException("error.permission.notFound");
        }

        // 检查是否有子权限
        if (!permissionRepository.findByParentIdAndDeletedFalse(id).isEmpty()) {
            throw new BusinessException("error.permission.delete.hasChildren");
        }

        // 检查是否被角色引用
        if (rolePermissionRepository.existsByPermissionIdAndDeletedFalse(id)) {
            throw new BusinessException("error.permission.delete.inUse");
        }

        // 软删除
        permission.setDeleted(true);
        permissionRepository.save(permission);
    }
}
