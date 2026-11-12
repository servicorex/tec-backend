package com.tecnrt.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String role;
    private final int tokenVersion;
    private final boolean active;
    private final String departmentId; // populated for HODs
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String username, String password, String role, int tokenVersion, boolean active, String departmentId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.tokenVersion = tokenVersion;
        this.active = active;
        this.departmentId = departmentId;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    public UUID getId() { return id; }
    public String getRole() { return role; }
    public int getTokenVersion() { return tokenVersion; }
    public String getDepartmentId() { return departmentId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}

