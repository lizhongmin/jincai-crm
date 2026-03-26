package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.OrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrgUserRepository extends JpaRepository<OrgUser, String>, JpaSpecificationExecutor<OrgUser> {

    Optional<OrgUser> findByUsernameAndDeletedFalse(String username);

    Optional<OrgUser> findByIdAndDeletedFalse(String id);

    boolean existsByPhoneAndDeletedFalse(String phone);

    boolean existsByPhoneAndDeletedFalseAndIdNot(String phone, String id);

    boolean existsByEmployeeNoAndDeletedFalse(String employeeNo);

    boolean existsByEmployeeNoAndDeletedFalseAndIdNot(String employeeNo, String id);

    List<OrgUser> findByDeletedFalse();

    List<OrgUser> findByDepartmentIdAndDeletedFalse(String departmentId);

    List<OrgUser> findByDepartmentIdInAndDeletedFalse(Collection<String> departmentIds);

    boolean existsByDepartmentIdAndDeletedFalse(String departmentId);
}

