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
 * 用户详情服务
 * 
 * 通过 API Key 加载用户
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserProfileRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String apiKey) throws UsernameNotFoundException {
        UserProfile user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with API key"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        if (user.getRole() >= 300) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (user.getRole() >= 400) {
            authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
        }

        return new User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.getIsActive(),
                true,
                true,
                true,
                authorities
        );
    }
}