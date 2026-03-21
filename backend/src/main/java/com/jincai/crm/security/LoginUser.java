package com.jincai.crm.security;

import com.jincai.crm.common.DataScope;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class LoginUser implements UserDetails {

    private final Long userId;
    private final Long departmentId;
    private final DataScope dataScope;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<String> roleCodes;

    public LoginUser(Long userId, Long departmentId, DataScope dataScope, String username, String password,
                     boolean enabled, List<String> roleCodes) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.dataScope = dataScope;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.roleCodes = roleCodes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream().map(code -> new SimpleGrantedAuthority("ROLE_" + code)).toList();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

