package com.jincai.crm.org.repository;

import com.jincai.crm.org.entity.Department;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdAndDeletedFalse(Long parentId);

    List<Department> findByDeletedFalse();

    long countByParentIdIsNullAndDeletedFalse();
}

