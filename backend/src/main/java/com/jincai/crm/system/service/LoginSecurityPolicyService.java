package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.system.dto.LoginSecurityPolicyRequest;
import com.jincai.crm.system.dto.LoginSecurityPolicyView;
import com.jincai.crm.system.entity.LoginSecurityPolicy;
import com.jincai.crm.system.repository.LoginSecurityPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginSecurityPolicyService {

    private final LoginSecurityPolicyRepository repository;

    public LoginSecurityPolicyService(LoginSecurityPolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LoginSecurityPolicyView getPolicy() {
        return toView(getPolicyEntity());
    }

    @Transactional
    public LoginSecurityPolicy getPolicyEntity() {
        return repository.findFirstByDeletedFalseOrderByIdAsc()
            .orElseGet(() -> repository.save(new LoginSecurityPolicy()));
    }

    @Transactional
    public LoginSecurityPolicyView updatePolicy(LoginSecurityPolicyRequest request) {
        validatePolicy(request);
        LoginSecurityPolicy policy = getPolicyEntity();
        policy.setCaptchaAfterFailures(request.captchaAfterFailures());
        policy.setLockAfterFailures(request.lockAfterFailures());
        policy.setLockMinutes(request.lockMinutes());
        policy.setCaptchaExpireSeconds(request.captchaExpireSeconds());
        policy.setPasswordMinLength(request.passwordMinLength());
        policy.setPasswordRequireUppercase(request.passwordRequireUppercase());
        policy.setPasswordRequireLowercase(request.passwordRequireLowercase());
        policy.setPasswordRequireDigit(request.passwordRequireDigit());
        policy.setPasswordRequireSpecial(request.passwordRequireSpecial());
        return toView(repository.save(policy));
    }

    @Transactional(readOnly = true)
    public void validatePasswordByPolicy(String password) {
        LoginSecurityPolicy policy = getPolicyEntity();
        if (password == null || password.isBlank()) {
            throw new BusinessException("error.auth.password.new.required");
        }
        if (password.length() < policy.getPasswordMinLength()) {
            throw new BusinessException("error.auth.password.minLength", policy.getPasswordMinLength());
        }
        if (Boolean.TRUE.equals(policy.getPasswordRequireUppercase()) && !password.matches(".*[A-Z].*")) {
            throw new BusinessException("error.auth.password.requireUppercase");
        }
        if (Boolean.TRUE.equals(policy.getPasswordRequireLowercase()) && !password.matches(".*[a-z].*")) {
            throw new BusinessException("error.auth.password.requireLowercase");
        }
        if (Boolean.TRUE.equals(policy.getPasswordRequireDigit()) && !password.matches(".*\\d.*")) {
            throw new BusinessException("error.auth.password.requireDigit");
        }
        if (Boolean.TRUE.equals(policy.getPasswordRequireSpecial()) && !password.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException("error.auth.password.requireSpecial");
        }
    }

    private void validatePolicy(LoginSecurityPolicyRequest request) {
        if (request.lockAfterFailures() <= request.captchaAfterFailures()) {
            throw new BusinessException("error.security.policy.threshold.invalid");
        }
    }

    private LoginSecurityPolicyView toView(LoginSecurityPolicy policy) {
        return new LoginSecurityPolicyView(
            policy.getCaptchaAfterFailures(),
            policy.getLockAfterFailures(),
            policy.getLockMinutes(),
            policy.getCaptchaExpireSeconds(),
            policy.getPasswordMinLength(),
            policy.getPasswordRequireUppercase(),
            policy.getPasswordRequireLowercase(),
            policy.getPasswordRequireDigit(),
            policy.getPasswordRequireSpecial()
        );
    }
}
