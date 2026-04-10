package com.chatpass.service;

import com.chatpass.dto.AuthDTO;
import com.chatpass.dto.UserDTO;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.AuthenticationException;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private RealmRepository realmRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserProfile testUser;
    private Realm testRealm;

    @BeforeEach
    void setUp() {
        testRealm = Realm.builder()
                .id(1L)
                .stringId("test")
                .name("Test Realm")
                .build();

        testUser = UserProfile.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .realm(testRealm)
                .isActive(true)
                .role(100)
                .build();
    }

    @Test
    @DisplayName("登录成功")
    void login_success() {
        // Given
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(tokenProvider.generateToken(anyString(), anyLong(), anyLong())).thenReturn("jwt_token");

        // When
        AuthDTO.TokenResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_fail_userNotFound() {
        // Given
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_fail_wrongPassword() {
        // Given
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("test@example.com")
                .password("wrong_password")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("登录失败 - 账户已停用")
    void login_fail_accountDeactivated() {
        // Given
        testUser.setIsActive(false);
        
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account is deactivated");
    }

    @Test
    @DisplayName("注册成功")
    void register_success() {
        // Given
        AuthDTO.RegisterRequest request = AuthDTO.RegisterRequest.builder()
                .email("new@example.com")
                .password("password")
                .fullName("New User")
                .build();

        when(userRepository.existsByRealmIdAndEmail(1L, "new@example.com")).thenReturn(false);
        when(realmRepository.findById(1L)).thenReturn(Optional.of(testRealm));
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(tokenProvider.generateToken(anyString(), anyLong(), anyLong())).thenReturn("jwt_token");

        // When
        AuthDTO.TokenResponse response = authService.register(request, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token");
        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_fail_emailExists() {
        // Given
        AuthDTO.RegisterRequest request = AuthDTO.RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .fullName("New User")
                .build();

        when(userRepository.existsByRealmIdAndEmail(1L, "test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }
}