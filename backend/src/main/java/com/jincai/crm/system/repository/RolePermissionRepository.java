package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {

    List<RolePermission> findByRoleIdInAndDeletedFalse(List<String> roleIds);

    List<RolePermission> findByRoleIdAndDeletedFalse(String roleId);

    void deleteByRoleId(String roleId);

    boolean existsByPermissionIdAndDeletedFalse(String permissionId);
}

