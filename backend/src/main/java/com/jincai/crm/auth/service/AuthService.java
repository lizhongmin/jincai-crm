package com.jincai.crm.auth.service;

import com.jincai.crm.auth.dto.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.service.LoginSecurityPolicyService;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.security.JwtService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LoginSecurityService loginSecurityService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       AppUserRepository userRepository, UserRoleRepository userRoleRepository,
                       LoginSecurityService loginSecurityService, LoginSecurityPolicyService loginSecurityPolicyService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.loginSecurityService = loginSecurityService;
        this.loginSecurityPolicyService = loginSecurityPolicyService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedUsername = request.username() == null ? null : request.username().trim();
        loginSecurityService.ensureLoginAllowedAndValidateCaptcha(normalizedUsername, request.captchaId(), request.captchaCode());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            LoginUser user = (LoginUser) authentication.getPrincipal();
            AppUser appUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new BusinessException("error.user.notFound"));
            loginSecurityService.onLoginSuccess(normalizedUsername);
            String token = jwtService.generateToken(user.getUsername());
            return new LoginResponse(token, appUser.getId(), appUser.getUsername(), appUser.getFullName(), user.getRoleCodes());
        } catch (DisabledException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            LoginStateResponse state = loginSecurityService.onLoginFailure(normalizedUsername);
            if (state.locked()) {
                long minutes = state.lockSeconds() <= 0 ? 0 : (long) Math.ceil(state.lockSeconds() / 60.0);
                throw new BusinessException("error.auth.accountLocked", minutes);
            }
            throw ex;
        }
    }

    public Map<String, Object> me() {
        LoginUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        List<Long> roleIds = userRoleRepository.findByUserIdAndDeletedFalse(user.getUserId()).stream()
            .map(UserRole::getRoleId)
            .toList();
        return Map.of(
            "userId", user.getUserId(),
            "username", user.getUsername(),
            "departmentId", user.getDepartmentId(),
            "dataScope", user.getDataScope(),
            "roleIds", roleIds,
            "roles", user.getRoleCodes()
        );
    }

    public LoginStateResponse loginState(String username) {
        return loginSecurityService.getLoginState(username);
    }

    public CaptchaResponse captcha(String username) {
        return loginSecurityService.generateCaptcha(username);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        LoginUser loginUser = SecurityUtils.currentUser();
        if (loginUser == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("error.auth.password.confirmMismatch");
        }
        AppUser user = userRepository.findById(loginUser.getUserId())
            .orElseThrow(() -> new BusinessException("error.user.notFound"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("error.auth.password.oldMismatch");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("error.auth.password.sameAsOld");
        }
        loginSecurityPolicyService.validatePasswordByPolicy(request.newPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}

