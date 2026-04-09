package com.chatpass.service;

import com.chatpass.dto.AuthDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.AuthenticationException;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        UserProfile user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        return buildAuthResponse(user);
    }

    /**
     * 用户注册
     */
    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        // 查找或创建默认 Realm
        Realm realm;
        if (request.getRealmId() != null) {
            realm = realmRepository.findById(Long.valueOf(request.getRealmId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Realm", Long.valueOf(request.getRealmId())));
        } else {
            // 创建新 Realm
            realm = realmRepository.save(Realm.builder()
                    .stringId(UUID.randomUUID().toString().substring(0, 8))
                    .name(request.getFullName() + "'s Organization")
                    .build());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByRealmIdAndEmail(realm.getId(), request.getEmail())) {
            throw new AuthenticationException("Email already registered in this organization");
        }

        // 创建用户
        UserProfile user = UserProfile.builder()
                .realm(realm)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .apiKey(UUID.randomUUID().toString())
                .isActive(true)
                .role(400) // Owner
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * API Key 认证
     */
    @Transactional(readOnly = true)
    public AuthDTO.AuthResponse authenticateByApiKey(String apiKey) {
        UserProfile user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new AuthenticationException("Invalid API key"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        return buildAuthResponse(user);
    }

    /**
     * 生成新的 API Key
     */
    @Transactional
    public String regenerateApiKey(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String newApiKey = UUID.randomUUID().toString();
        user.setApiKey(newApiKey);
        userRepository.save(user);

        return newApiKey;
    }

    private AuthDTO.AuthResponse buildAuthResponse(UserProfile user) {
        return AuthDTO.AuthResponse.builder()
                .apiKey(user.getApiKey())
                .email(user.getEmail())
                .userId(user.getId())
                .realmId(user.getRealm().getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}