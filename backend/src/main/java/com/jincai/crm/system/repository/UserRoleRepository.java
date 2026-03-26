package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    List<UserRole> findByUserIdAndDeletedFalse(String userId);

    List<UserRole> findByRoleIdAndDeletedFalse(String roleId);

    List<UserRole> findByUserIdInAndDeletedFalse(Collection<String> userIds);

    void deleteByUserId(String userId);
}

