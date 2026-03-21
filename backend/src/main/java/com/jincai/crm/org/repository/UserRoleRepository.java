package com.jincai.crm.org.repository;

import com.jincai.crm.org.entity.UserRole;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserIdAndDeletedFalse(Long userId);

    void deleteByUserId(Long userId);
}

