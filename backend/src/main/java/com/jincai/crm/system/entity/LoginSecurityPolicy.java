package com.jincai.crm.system.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_login_security_policy")
public class LoginSecurityPolicy extends BaseEntity {

    @Column(name = "captcha_after_failures", nullable = false)
    private Integer captchaAfterFailures = 3;

    @Column(name = "lock_after_failures", nullable = false)
    private Integer lockAfterFailures = 5;

    @Column(name = "lock_minutes", nullable = false)
    private Integer lockMinutes = 30;

    @Column(name = "captcha_expire_seconds", nullable = false)
    private Integer captchaExpireSeconds = 300;

    @Column(name = "password_min_length", nullable = false)
    private Integer passwordMinLength = 8;

    @Column(name = "password_require_uppercase", nullable = false)
    private Boolean passwordRequireUppercase = true;

    @Column(name = "password_require_lowercase", nullable = false)
    private Boolean passwordRequireLowercase = true;

    @Column(name = "password_require_digit", nullable = false)
    private Boolean passwordRequireDigit = true;

    @Column(name = "password_require_special", nullable = false)
    private Boolean passwordRequireSpecial = false;
}

