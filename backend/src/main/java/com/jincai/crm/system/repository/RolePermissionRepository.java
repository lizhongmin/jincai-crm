package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.RolePermission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleIdInAndDeletedFalse(List<Long> roleIds);

    List<RolePermission> findByRoleIdAndDeletedFalse(Long roleId);

    void deleteByRoleId(Long roleId);

    boolean existsByPermissionIdAndDeletedFalse(Long permissionId);
}

