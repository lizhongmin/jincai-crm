package com.jincai.crm.security;

import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.entity.RolePermission;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.UserRoleRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AppUserDetailsService(AppUserRepository userRepository, UserRoleRepository userRoleRepository,
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
        AppUser user = userRepository.findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedFalse(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<String> roleCodes;
        if (roleIds.isEmpty()) {
            roleCodes = Collections.emptyList();
        } else {
            Map<Long, Role> roleMap = roleRepository.findAllById(roleIds).stream()
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
            Set<Long> permissionIds = rolePermissionRepository.findByRoleIdInAndDeletedFalse(roleIds).stream()
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

        return new LoginUser(
            user.getId(),
            user.getDepartmentId(),
            user.getDataScope(),
            user.getUsername(),
            user.getPassword(),
            user.getEnabled(),
            roleCodes,
            permissionCodes
        );
    }
}
