package com.jincai.crm.org.repository;

import com.jincai.crm.org.entity.Role;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCodeAndDeletedFalse(String code);

    List<Role> findByDeletedFalse();
}

