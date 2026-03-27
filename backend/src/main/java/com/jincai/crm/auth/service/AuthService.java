package com.jincai.crm.auth.service;

import com.jincai.crm.auth.dto.*;
import lombok.extern.slf4j.Slf4j;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.security.JwtService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import com.jincai.crm.system.entity.OrgUser;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.OrgUserRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.system.service.LoginSecurityPolicyService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OrgUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LoginSecurityService loginSecurityService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       OrgUserRepository userRepository, UserRoleRepository userRoleRepository,
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
        log.info("Attempting login for user: {}", normalizedUsername);
        loginSecurityService.ensureLoginAllowedAndValidateCaptcha(normalizedUsername, request.captchaId(), request.captchaCode());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            LoginUser user = (LoginUser) authentication.getPrincipal();
            OrgUser orgUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new BusinessException("error.user.notFound"));
            loginSecurityService.onLoginSuccess(normalizedUsername);
            String token = jwtService.generateToken(user.getUsername());
            log.info("Login successful for user: {}", normalizedUsername);
            return new LoginResponse(token, orgUser.getId(), orgUser.getUsername(), orgUser.getFullName(), user.getRoleCodes());
        } catch (DisabledException ex) {
            log.warn("Login failed: account disabled for user: {}", normalizedUsername);
            throw ex;
        } catch (AuthenticationException ex) {
            log.warn("Login failed: authentication rejected for user: {}", normalizedUsername);
            LoginStateResponse state = loginSecurityService.onLoginFailure(normalizedUsername);
            if (state.locked()) {
                long minutes = state.lockSeconds() <= 0 ? 0 : (long) Math.ceil(state.lockSeconds() / 60.0);
                log.warn("Account locked for user: {} for {} minutes due to too many failed attempts", normalizedUsername, minutes);
                throw new BusinessException("error.auth.accountLocked", minutes);
            }
            throw ex;
        }
    }

    public UserProfileResponse me() {
        LoginUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        List<String> roleIds = userRoleRepository.findByUserIdAndDeletedFalse(user.getUserId()).stream()
            .map(UserRole::getRoleId)
            .toList();
        return new UserProfileResponse(
            user.getUserId(),
            user.getUsername(),
            user.getDepartmentId(),
            user.getDataScope(),
            roleIds,
            user.getRoleCodes()
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
            log.error("Change password failed: user unauthenticated");
            throw new BusinessException("error.auth.unauthenticated");
        }
        log.info("User {} is attempting to change password", loginUser.getUsername());
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("Change password failed for {}: confirm password mismatch", loginUser.getUsername());
            throw new BusinessException("error.auth.password.confirmMismatch");
        }
        OrgUser user = userRepository.findById(loginUser.getUserId())
            .orElseThrow(() -> new BusinessException("error.user.notFound"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.warn("Change password failed for {}: old password mismatch", loginUser.getUsername());
            throw new BusinessException("error.auth.password.oldMismatch");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            log.warn("Change password failed for {}: new password is the same as the old one", loginUser.getUsername());
            throw new BusinessException("error.auth.password.sameAsOld");
        }
        loginSecurityPolicyService.validatePasswordByPolicy(request.newPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", loginUser.getUsername());
    }
}

