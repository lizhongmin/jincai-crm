package com.jincai.crm.org.repository;

import com.jincai.crm.org.entity.RolePermission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleIdInAndDeletedFalse(List<Long> roleIds);

    List<RolePermission> findByRoleIdAndDeletedFalse(Long roleId);

    void deleteByRoleId(Long roleId);
}

