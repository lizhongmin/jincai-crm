package com.jincai.crm.org.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.org.dto.RoleGrantRequest;
import com.jincai.crm.org.dto.RoleRequest;
import com.jincai.crm.org.entity.Permission;
import com.jincai.crm.org.entity.Role;
import com.jincai.crm.org.entity.RolePermission;
import com.jincai.crm.org.repository.PermissionRepository;
import com.jincai.crm.org.repository.RolePermissionRepository;
import com.jincai.crm.org.repository.RoleRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private static final String ADMIN_ROLE_CODE = "ADMIN";

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, RolePermissionRepository rolePermissionRepository,
                       PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> list() {
        return roleRepository.findByDeletedFalse();
    }

    public Set<Long> permissionIds(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("Role not found"));
        if (Boolean.TRUE.equals(role.getDeleted())) {
            throw new BusinessException("Role not found");
        }
        if (ADMIN_ROLE_CODE.equalsIgnoreCase(role.getCode())) {
            return permissionRepository.findByDeletedFalse().stream()
                .map(Permission::getId)
                .collect(java.util.stream.Collectors.toSet());
        }
        return rolePermissionRepository.findByRoleIdAndDeletedFalse(id).stream()
            .map(RolePermission::getPermissionId)
            .collect(java.util.stream.Collectors.toSet());
    }

    public Role create(RoleRequest request) {
        if (ADMIN_ROLE_CODE.equalsIgnoreCase(request.code())) {
            throw new BusinessException("ADMIN role is built-in and cannot be created");
        }
        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        return roleRepository.save(role);
    }

    public Role update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("Role not found"));
        ensureNotAdminRole(role);
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("Role not found"));
        ensureNotAdminRole(role);
        role.setDeleted(true);
        roleRepository.save(role);
    }

    @Transactional
    public void grant(Long id, RoleGrantRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("Role not found"));
        if (Boolean.TRUE.equals(role.getDeleted())) {
            throw new BusinessException("Role not found");
        }
        ensureNotAdminRole(role);
        rolePermissionRepository.deleteByRoleId(id);
        request.permissionIds().forEach(permissionId -> {
            RolePermission rp = new RolePermission();
            rp.setRoleId(id);
            rp.setPermissionId(permissionId);
            rolePermissionRepository.save(rp);
        });
    }

    private void ensureNotAdminRole(Role role) {
        if (ADMIN_ROLE_CODE.equalsIgnoreCase(role.getCode())) {
            throw new BusinessException("ADMIN role cannot be edited or deleted");
        }
    }
}

