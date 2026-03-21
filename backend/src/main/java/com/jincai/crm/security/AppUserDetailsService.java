package com.jincai.crm.security;

import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.entity.Role;
import com.jincai.crm.org.repository.RoleRepository;
import com.jincai.crm.org.entity.UserRole;
import com.jincai.crm.org.repository.UserRoleRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public AppUserDetailsService(AppUserRepository userRepository, UserRoleRepository userRoleRepository,
                                 RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedFalse(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<String> roleCodes;
        if (roleIds.isEmpty()) {
            roleCodes = Collections.emptyList();
        } else {
            Map<Long, Role> roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(java.util.stream.Collectors.toMap(Role::getId, Function.identity()));
            roleCodes = roleIds.stream()
                .map(roleMap::get)
                .filter(java.util.Objects::nonNull)
                .map(Role::getCode)
                .toList();
        }

        return new LoginUser(
            user.getId(),
            user.getDepartmentId(),
            user.getDataScope(),
            user.getUsername(),
            user.getPassword(),
            user.getEnabled(),
            roleCodes
        );
    }
}

