package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.LoginSecurityPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginSecurityPolicyRepository extends JpaRepository<LoginSecurityPolicy, Long> {

    Optional<LoginSecurityPolicy> findFirstByDeletedFalseOrderByIdAsc();
}

