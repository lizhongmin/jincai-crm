package com.jincai.crm.system.repository;

import com.jincai.crm.system.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    List<Department> findByParentIdAndDeletedFalse(String parentId);

    List<Department> findByDeletedFalse();

    long countByParentIdIsNullAndDeletedFalse();
}

