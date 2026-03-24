package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.Role;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByCodeAndDeletedFalse(String code);

    List<Role> findByDeletedFalse();
}

