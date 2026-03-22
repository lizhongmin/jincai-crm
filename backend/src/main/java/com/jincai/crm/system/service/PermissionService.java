package com.jincai.crm.system.service;

import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.dto.PermissionTreeGroupView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.entity.RolePermission;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

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

        List<Long> roleIds = userRoleRepository.findByUserIdAndDeletedFalse(user.getUserId()).stream()
            .map(UserRole::getRoleId)
            .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> permissionIds = rolePermissionRepository.findByRoleIdInAndDeletedFalse(roleIds).stream()
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
                .thenComparing(p -> p.getParentId() == null ? Long.MIN_VALUE : p.getParentId())
                .thenComparing(Permission::getCode))
            .toList();
    }

    public List<PermissionTreeGroupView> tree() {
        List<Permission> permissions = permissionRepository.findByDeletedFalse();
        Map<Long, Permission> permissionMap = permissions.stream()
            .filter(p -> p.getId() != null)
            .collect(java.util.stream.Collectors.toMap(Permission::getId, p -> p));

        List<Permission> menus = permissions.stream()
            .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
            .filter(p -> p.getParentId() == null)
            .sorted(java.util.Comparator.comparing(Permission::getCode))
            .toList();

        Map<Long, List<Permission>> childrenMap = permissions.stream()
            .filter(p -> !"MENU".equalsIgnoreCase(p.getType()))
            .filter(p -> p.getParentId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                Permission::getParentId,
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()
            ));

        List<PermissionTreeGroupView> result = new java.util.ArrayList<>();
        for (Permission menu : menus) {
            List<Permission> groupPermissions = new java.util.ArrayList<>();
            groupPermissions.add(menu);
            groupPermissions.addAll(childrenMap.getOrDefault(menu.getId(), List.of()).stream()
                .sorted(java.util.Comparator.comparing(Permission::getCode))
                .toList());
            result.add(new PermissionTreeGroupView(menu.getCode(), menu.getName(), groupPermissions));
        }

        List<Permission> orphanButtons = permissions.stream()
            .filter(p -> !"MENU".equalsIgnoreCase(p.getType()))
            .filter(p -> p.getParentId() == null || !permissionMap.containsKey(p.getParentId()))
            .sorted(java.util.Comparator.comparing(Permission::getCode))
            .toList();
        if (!orphanButtons.isEmpty()) {
            result.add(new PermissionTreeGroupView("MISC", "\u5176\u4ED6\u6743\u9650", orphanButtons));
        }

        return result;
    }
}
