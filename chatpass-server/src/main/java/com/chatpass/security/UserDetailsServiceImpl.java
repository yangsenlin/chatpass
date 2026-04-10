package com.chatpass.security;

import com.chatpass.entity.UserProfile;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 * 
 * Spring Security 认证核心
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserProfileRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 根据角色添加权限
        if (user.getRole() >= 400) {
            authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
        }
        if (user.getRole() >= 300) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (user.getRole() >= 200) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}