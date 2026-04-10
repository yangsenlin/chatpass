package com.chatpass.security;

import com.chatpass.entity.UserProfile;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具类
 * 
 * 获取当前登录用户信息
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserProfileRepository userRepository;

    /**
     * 获取当前用户 ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            UserProfile user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + email));
            return user.getId();
        }

        throw new IllegalStateException("Unable to determine current user");
    }

    /**
     * 获取当前用户
     */
    public UserProfile getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }

    /**
     * 获取当前用户的 Realm ID
     */
    public Long getCurrentRealmId() {
        UserProfile user = getCurrentUser();
        return user.getRealm() != null ? user.getRealm().getId() : null;
    }

    /**
     * 检查是否已认证
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否是指定用户
     */
    public boolean isCurrentUser(Long userId) {
        return isAuthenticated() && getCurrentUserId().equals(userId);
    }

    /**
     * 检查当前用户是否是管理员
     */
    public boolean isAdmin() {
        UserProfile user = getCurrentUser();
        return user.getRole() != null && user.getRole() >= 300;
    }

    /**
     * 检查当前用户是否是版主
     */
    public boolean isModerator() {
        // 暂时简化：管理员即版主
        return isAdmin();
    }
}