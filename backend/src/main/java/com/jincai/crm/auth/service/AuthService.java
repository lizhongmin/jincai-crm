package com.jincai.crm.auth.service;

import com.jincai.crm.auth.dto.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.entity.UserRole;
import com.jincai.crm.org.repository.UserRoleRepository;
import com.jincai.crm.security.JwtService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       AppUserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        LoginUser user = (LoginUser) authentication.getPrincipal();
        AppUser appUser = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new BusinessException("User not found"));
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, appUser.getId(), appUser.getUsername(), appUser.getFullName(), user.getRoleCodes());
    }

    public Map<String, Object> me() {
        LoginUser user = SecurityUtils.currentUser();
        if (user == null) {
            throw new BusinessException("Unauthenticated");
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
}
