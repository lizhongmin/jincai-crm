package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByCodeAndDeletedFalse(String code);

    List<Role> findByDeletedFalse();
}

