package com.jincai.crm.system.entity;

import com.jincai.crm.common.BaseEntity;
import com.jincai.crm.common.DataScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "org_user")
public class AppUser extends BaseEntity {

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
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", nullable = false)
    private DataScope dataScope = DataScope.SELF;

    @Column(nullable = false)
    private Boolean enabled = true;
}

