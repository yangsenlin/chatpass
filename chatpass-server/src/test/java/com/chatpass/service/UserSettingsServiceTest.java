package com.chatpass.service;

import com.chatpass.dto.UserDTO;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserSettingsService 测试
 */
@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock private UserProfileRepository userRepository;
    @InjectMocks private UserSettingsService userSettingsService;

    private UserProfile testUser;

    @BeforeEach
    void setUp() {
        testUser = UserProfile.builder()
                .id(1L).email("test@test.com").fullName("Test User")
                .timezone("UTC").defaultLanguage("en")
                .enableDesktopNotifications(true)
                .enableSounds(true)
                .build();
    }

    @Test
    @DisplayName("更新用户设置")
    void updateSettings_success() {
        UserDTO.UpdateRequest request = UserDTO.UpdateRequest.builder()
                .fullName("New Name")
                .timezone("Asia/Shanghai")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserProfile.class))).thenReturn(testUser);

        UserDTO.ProfileResponse response = userSettingsService.updateSettings(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository).save(argThat(u -> 
                "New Name".equals(u.getFullName()) && 
                "Asia/Shanghai".equals(u.getTimezone())));
    }

    @Test
    @DisplayName("更新通知设置")
    void updateNotificationSettings_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userSettingsService.updateNotificationSettings(1L, false, false, null, null);

        verify(userRepository).save(argThat(u -> 
                !u.getEnableDesktopNotifications() && !u.getEnableSounds()));
    }
}