package com.jincai.crm.org.repository;

import com.jincai.crm.org.entity.AppUser;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameAndDeletedFalse(String username);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByPhoneAndDeletedFalseAndIdNot(String phone, Long id);

    boolean existsByEmployeeNoAndDeletedFalse(String employeeNo);

    boolean existsByEmployeeNoAndDeletedFalseAndIdNot(String employeeNo, Long id);

    List<AppUser> findByDeletedFalse();

    boolean existsByDepartmentIdAndDeletedFalse(Long departmentId);
}

