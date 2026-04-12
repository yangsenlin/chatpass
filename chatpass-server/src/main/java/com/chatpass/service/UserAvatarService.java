package com.chatpass.service;

import com.chatpass.dto.UserDTO;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 用户头像服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAvatarService {

    private final UserProfileRepository userRepository;
    
    // 头像存储路径
    private static final String AVATAR_DIR = "/var/chatpass/avatars/";

    /**
     * 上传头像
     */
    @Transactional
    public UserDTO.AvatarResponse uploadAvatar(Long userId, Long realmId, MultipartFile file) throws IOException {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // 验证文件大小 (最大 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Avatar size must be less than 5MB");
        }

        // 创建存储目录
        Path avatarPath = Paths.get(AVATAR_DIR);
        if (!Files.exists(avatarPath)) {
            Files.createDirectories(avatarPath);
        }

        // 生成文件名
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = avatarPath.resolve(fileName);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 更新用户头像URL
        String avatarUrl = "/avatars/" + fileName;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        log.info("Avatar uploaded for user {}: {}", userId, avatarUrl);

        return UserDTO.AvatarResponse.builder()
                .avatarUrl(avatarUrl)
                .userId(userId)
                .build();
    }

    /**
     * 删除头像
     */
    @Transactional
    public void deleteAvatar(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // 删除文件
            try {
                String fileName = avatarUrl.replace("/avatars/", "");
                Path filePath = Paths.get(AVATAR_DIR, fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Failed to delete avatar file: {}", e.getMessage());
            }

            // 清除头像URL
            user.setAvatarUrl(null);
            userRepository.save(user);
            
            log.info("Avatar deleted for user {}", userId);
        }
    }

    /**
     * 获取头像信息
     */
    public UserDTO.AvatarResponse getAvatar(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return UserDTO.AvatarResponse.builder()
                .avatarUrl(user.getAvatarUrl())
                .userId(userId)
                .build();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".jpg";
    }
}