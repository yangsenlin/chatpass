package com.chatpass.service;

import com.chatpass.dto.AuthDTO;
import com.chatpass.dto.UserDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.AuthenticationException;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务
 * 
 * 处理用户登录、注册、API Key 等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    private static final long TOKEN_EXPIRATION = 86400000L; // 24 hours

    /**
     * 用户登录
     */
    @Transactional
    public AuthDTO.TokenResponse login(AuthDTO.LoginRequest request) {
        UserProfile user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 生成 Token
        String token = tokenProvider.generateToken(
                user.getEmail(), 
                user.getId(), 
                user.getRealm().getId()
        );

        return AuthDTO.TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRATION / 1000)
                .user(toUserResponse(user))
                .build();
    }

    /**
     * 用户注册
     */
    @Transactional
    public AuthDTO.TokenResponse register(AuthDTO.RegisterRequest request, Long realmId) {
        // 检查邮箱是否已存在
        if (userRepository.existsByRealmIdAndEmail(realmId, request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm not found"));

        // 创建用户
        UserProfile user = UserProfile.builder()
                .realm(realm)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .shortName(request.getShortName())
                .apiKey(generateApiKey())
                .isActive(true)
                .role(100) // 普通用户
                .build();

        user = userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());

        // 生成 Token
        String token = tokenProvider.generateToken(
                user.getEmail(), 
                user.getId(), 
                user.getRealm().getId()
        );

        return AuthDTO.TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRATION / 1000)
                .user(toUserResponse(user))
                .build();
    }

    /**
     * 用户注销
     */
    @Transactional
    public void logout(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        log.info("User logged out: {}", user.getEmail());
    }

    /**
     * 获取 API Key
     */
    @Transactional
    public AuthDTO.ApiKeyResponse getApiKey(AuthDTO.ApiKeyRequest request) {
        UserProfile user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        // 如果没有 API Key，生成一个
        if (user.getApiKey() == null || user.getApiKey().isEmpty()) {
            user.setApiKey(generateApiKey());
            user = userRepository.save(user);
        }

        return AuthDTO.ApiKeyResponse.builder()
                .apiKey(user.getApiKey())
                .email(user.getEmail())
                .build();
    }

    /**
     * 通过 API Key 登录
     */
    @Transactional
    public AuthDTO.TokenResponse loginWithApiKey(String apiKey) {
        UserProfile user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new AuthenticationException("Invalid API key"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        String token = tokenProvider.generateToken(
                user.getEmail(), 
                user.getId(), 
                user.getRealm().getId()
        );

        return AuthDTO.TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRATION / 1000)
                .user(toUserResponse(user))
                .build();
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, AuthDTO.ChangePasswordRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * 生成 API Key
     */
    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换为用户响应
     */
    private UserDTO.Response toUserResponse(UserProfile user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .shortName(user.getShortName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .timezone(user.getTimezone())
                .isActive(user.getIsActive())
                .isBot(user.getBotType() != null)
                .dateJoined(user.getDateJoined())
                .build();
    }
}