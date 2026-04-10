package com.chatpass.security;

import com.chatpass.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 当前用户 Principal
 * 
 * 封装认证用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Long realmId;
    private Integer role;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserProfile user, Collection<? extends GrantedAuthority> authorities) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.getRealm().getId(),
                user.getRole(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
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
        return true;
    }

    public boolean isAdmin() {
        return role >= 300;
    }

    public boolean isModerator() {
        return role >= 200;
    }

    public boolean isOwner() {
        return role >= 400;
    }
}