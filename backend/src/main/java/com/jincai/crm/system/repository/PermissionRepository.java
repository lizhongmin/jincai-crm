package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByCodeAndDeletedFalse(String code);

    List<Permission> findByIdIn(Collection<String> ids);

    List<Permission> findByDeletedFalse();

    List<Permission> findByParentIdAndDeletedFalse(String parentId);
}

