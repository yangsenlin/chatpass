package com.chatpass.service;

import com.chatpass.dto.UserDTO;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户设置服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final UserProfileRepository userRepository;

    /**
     * 更新用户设置
     */
    @Transactional
    public UserDTO.ProfileResponse updateSettings(Long userId, UserDTO.UpdateRequest request) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getShortName() != null) {
            user.setShortName(request.getShortName());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getDefaultLanguage() != null) {
            user.setDefaultLanguage(request.getDefaultLanguage());
        }
        if (request.getEnableDesktopNotifications() != null) {
            user.setEnableDesktopNotifications(request.getEnableDesktopNotifications());
        }
        if (request.getEnableSounds() != null) {
            user.setEnableSounds(request.getEnableSounds());
        }
        if (request.getEnterSends() != null) {
            user.setEnterSends(request.getEnterSends());
        }
        if (request.getTwentyFourHourTime() != null) {
            user.setTwentyFourHourTime(request.getTwentyFourHourTime());
        }

        user = userRepository.save(user);

        log.info("Updated settings for user {}", userId);

        return toProfileResponse(user);
    }

    /**
     * 获取用户设置
     */
    @Transactional(readOnly = true)
    public UserDTO.ProfileResponse getSettings(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toProfileResponse(user);
    }

    /**
     * 更新通知设置
     */
    @Transactional
    public void updateNotificationSettings(Long userId, 
                                            Boolean enableDesktopNotifications,
                                            Boolean enableSounds,
                                            Boolean enableOfflineEmailNotifications,
                                            Boolean enableOfflinePushNotifications) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (enableDesktopNotifications != null) {
            user.setEnableDesktopNotifications(enableDesktopNotifications);
        }
        if (enableSounds != null) {
            user.setEnableSounds(enableSounds);
        }
        if (enableOfflineEmailNotifications != null) {
            user.setEnableOfflineEmailNotifications(enableOfflineEmailNotifications);
        }
        if (enableOfflinePushNotifications != null) {
            user.setEnableOfflinePushNotifications(enableOfflinePushNotifications);
        }

        userRepository.save(user);

        log.info("Updated notification settings for user {}", userId);
    }

    /**
     * 设置时区
     */
    @Transactional
    public void setTimezone(Long userId, String timezone) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setTimezone(timezone);
        userRepository.save(user);

        log.info("Set timezone {} for user {}", timezone, userId);
    }

    /**
     * 设置语言
     */
    @Transactional
    public void setLanguage(Long userId, String language) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setDefaultLanguage(language);
        userRepository.save(user);

        log.info("Set language {} for user {}", language, userId);
    }

    private UserDTO.ProfileResponse toProfileResponse(UserProfile user) {
        return UserDTO.ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .shortName(user.getShortName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .timezone(user.getTimezone())
                .defaultLanguage(user.getDefaultLanguage())
                .isActive(user.getIsActive())
                .isBot(user.getBotType() != null)
                .isBillingAdmin(user.getIsBillingAdmin())
                .dateJoined(user.getDateJoined())
                .lastLogin(user.getLastLogin())
                .build();
    }
}