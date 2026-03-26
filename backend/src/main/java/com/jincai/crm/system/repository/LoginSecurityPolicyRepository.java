package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.LoginSecurityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginSecurityPolicyRepository extends JpaRepository<LoginSecurityPolicy, String> {

    Optional<LoginSecurityPolicy> findFirstByDeletedFalseOrderByIdAsc();
}

