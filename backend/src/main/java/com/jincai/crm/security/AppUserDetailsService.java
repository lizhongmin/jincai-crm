package com.jincai.crm.security;

import com.jincai.crm.common.DataScope;
import com.jincai.crm.system.entity.*;
import com.jincai.crm.system.repository.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final OrgUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AppUserDetailsService(OrgUserRepository userRepository, UserRoleRepository userRoleRepository,
                                 RoleRepository roleRepository, RolePermissionRepository rolePermissionRepository,
                                 PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        OrgUser user = userRepository.findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedFalse(user.getId());
        List<String> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<String> roleCodes;
        Map<String, Role> roleMap = Collections.emptyMap();
        if (roleIds.isEmpty()) {
            roleCodes = Collections.emptyList();
        } else {
            roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(java.util.stream.Collectors.toMap(Role::getId, Function.identity()));
            roleCodes = roleIds.stream()
                .map(roleMap::get)
                .filter(java.util.Objects::nonNull)
                .map(Role::getCode)
                .toList();
        }

        List<String> permissionCodes;
        if (roleIds.isEmpty()) {
            permissionCodes = Collections.emptyList();
        } else {
            Set<String> permissionIds = rolePermissionRepository.findByRoleIdInAndDeletedFalse(roleIds).stream()
                .map(RolePermission::getPermissionId)
                .collect(java.util.stream.Collectors.toSet());
            if (permissionIds.isEmpty()) {
                permissionCodes = Collections.emptyList();
            } else {
                permissionCodes = permissionRepository.findByIdIn(permissionIds).stream()
                    .filter(permission -> !Boolean.TRUE.equals(permission.getDeleted()))
                    .map(Permission::getCode)
                    .distinct()
                    .toList();
            }
        }

        DataScope dataScope = roleIds.stream()
            .map(roleMap::get)
            .filter(java.util.Objects::nonNull)
            .map(Role::getDataScope)
            .filter(java.util.Objects::nonNull)
            .max(java.util.Comparator.comparingInt(this::dataScopeLevel))
            .orElse(DataScope.SELF);

        return new LoginUser(
            user.getId(),
            user.getDepartmentId(),
            dataScope,
            user.getUsername(),
            user.getPassword(),
            user.getEnabled(),
            roleCodes,
            permissionCodes
        );
    }

    private int dataScopeLevel(DataScope dataScope) {
        return switch (dataScope) {
            case SELF -> 1;
            case DEPARTMENT -> 2;
            case DEPARTMENT_TREE -> 3;
            case ALL -> 4;
        };
    }
}
