package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCodeAndDeletedFalse(String code);

    List<Permission> findByIdIn(Collection<Long> ids);

    List<Permission> findByDeletedFalse();

    List<Permission> findByParentIdAndDeletedFalse(Long parentId);
}

