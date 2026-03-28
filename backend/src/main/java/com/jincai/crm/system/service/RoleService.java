package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.system.dto.RoleGrantRequest;
import com.jincai.crm.system.dto.RoleRequest;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.entity.RolePermission;
import com.jincai.crm.system.repository.PermissionRepository;
import com.jincai.crm.system.repository.RolePermissionRepository;
import com.jincai.crm.system.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
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

    public PageResult<Role> page(int page, int size, String keyword) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<Role> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("name"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("code"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), likeValue)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<Role> result = roleRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        return new PageResult<>(result.getContent(), result.getTotalElements(), normalizedPage, normalizedSize);
    }

    public Set<String> permissionIds(String id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("error.role.notFound"));
        if (Boolean.TRUE.equals(role.getDeleted())) {
            throw new BusinessException("error.role.notFound");
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
        log.info("创建角色 - 角色代码: {}, 角色名称: {}", request.code(), request.name());
        try {
            if (ADMIN_ROLE_CODE.equalsIgnoreCase(request.code())) {
                throw new BusinessException("error.role.admin.builtinCreateDenied");
            }
            Role role = new Role();
            role.setCode(request.code());
            role.setName(request.name());
            role.setDescription(request.description());
            role.setDataScope(request.dataScope());
            Role saved = roleRepository.save(role);
            log.info("角色创建成功 - 角色ID: {}, 角色代码: {}", saved.getId(), saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("创建角色失败 - 角色代码: {}", request.code(), e);
            throw e;
        }
    }

    public Role update(String id, RoleRequest request) {
        log.info("更新角色 - 角色ID: {}, 角色代码: {}, 角色名称: {}", id, request.code(), request.name());
        try {
            Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("error.role.notFound"));
            ensureNotAdminRole(role);
            role.setCode(request.code());
            role.setName(request.name());
            role.setDescription(request.description());
            role.setDataScope(request.dataScope());
            Role saved = roleRepository.save(role);
            log.info("角色更新成功 - 角色ID: {}, 角色代码: {}", id, saved.getCode());
            return saved;
        } catch (Exception e) {
            log.error("更新角色失败 - 角色ID: {}", id, e);
            throw e;
        }
    }

    public void delete(String id) {
        log.info("删除角色 - 角色ID: {}", id);
        try {
            Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("error.role.notFound"));
            ensureNotAdminRole(role);
            role.setDeleted(true);
            roleRepository.save(role);
            log.info("角色删除成功 - 角色ID: {}", id);
        } catch (Exception e) {
            log.error("删除角色失败 - 角色ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void grant(String id, RoleGrantRequest request) {
        log.info("为角色授权 - 角色ID: {}, 授权数量: {}", id, request.permissionIds().size());
        try {
            Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("error.role.notFound"));
            if (Boolean.TRUE.equals(role.getDeleted())) {
                throw new BusinessException("error.role.notFound");
            }
            ensureNotAdminRole(role);
            rolePermissionRepository.deleteByRoleId(id);
            request.permissionIds().forEach(permissionId -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permissionId);
                rolePermissionRepository.save(rp);
            });
            log.info("角色授权成功 - 角色ID: {}", id);
        } catch (Exception e) {
            log.error("角色授权失败 - 角色ID: {}", id, e);
            throw e;
        }
    }

    private void ensureNotAdminRole(Role role) {
        if (ADMIN_ROLE_CODE.equalsIgnoreCase(role.getCode())) {
            throw new BusinessException("error.role.admin.modifyDenied");
        }
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }
}


