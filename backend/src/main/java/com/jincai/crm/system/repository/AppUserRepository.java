package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.AppUser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameAndDeletedFalse(String username);

    Optional<AppUser> findByIdAndDeletedFalse(Long id);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByPhoneAndDeletedFalseAndIdNot(String phone, Long id);

    boolean existsByEmployeeNoAndDeletedFalse(String employeeNo);

    boolean existsByEmployeeNoAndDeletedFalseAndIdNot(String employeeNo, Long id);

    List<AppUser> findByDeletedFalse();

    List<AppUser> findByDepartmentIdAndDeletedFalse(Long departmentId);

    List<AppUser> findByDepartmentIdInAndDeletedFalse(Collection<Long> departmentIds);

    boolean existsByDepartmentIdAndDeletedFalse(Long departmentId);
}

