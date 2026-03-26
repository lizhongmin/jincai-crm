package com.jincai.crm.system.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "org_user")
public class OrgUser extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 32)
    private String phone;

    @Column(name = "employee_no", unique = true, length = 64)
    private String employeeNo;

    @Column(length = 128)
    private String email;

    @Column(length = 32)
    private String gender;

    @Column(length = 128)
    private String title;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "emergency_phone", length = 32)
    private String emergencyPhone;

    @Column(name = "department_id")
    private String departmentId;

    @Column(nullable = false)
    private Boolean enabled = true;
}

