package com.chatpass.service;

import com.chatpass.dto.MutedUserDTO;
import com.chatpass.entity.MutedUser;
import com.chatpass.entity.UserProfile;
import com.chatpass.exception.ResourceNotFoundException;
import com.chatpass.repository.MutedUserRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户屏蔽服务
 * 
 * 允许用户屏蔽其他用户的消息
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutedUserService {

    private final MutedUserRepository mutedUserRepository;
    private final UserProfileRepository userRepository;

    private static final String CACHE_NAME = "muted_users";

    /**
     * 屏蔽用户
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#userId")
    public MutedUserDTO.Response muteUser(Long userId, Long mutedUserId) {
        // 不能屏蔽自己
        if (userId.equals(mutedUserId)) {
            throw new IllegalArgumentException("Cannot mute yourself");
        }

        // 检查目标用户是否存在
        UserProfile mutedUser = userRepository.findById(mutedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", mutedUserId));

        // 检查是否已屏蔽
        if (mutedUserRepository.existsByUserIdAndMutedUserId(userId, mutedUserId)) {
            log.debug("User {} already muted user {}", userId, mutedUserId);
            return findByUserIdAndMutedUserId(userId, mutedUserId);
        }

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        MutedUser muted = MutedUser.builder()
                .user(user)
                .mutedUser(mutedUser)
                .build();

        muted = mutedUserRepository.save(muted);
        log.info("User {} muted user {}", userId, mutedUserId);

        return toResponse(muted);
    }

    /**
     * 取消屏蔽
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#userId")
    public void unmuteUser(Long userId, Long mutedUserId) {
        mutedUserRepository.deleteByUserIdAndMutedUserId(userId, mutedUserId);
        log.info("User {} unmuted user {}", userId, mutedUserId);
    }

    /**
     * 获取用户屏蔽的用户列表
     */
    @Transactional(readOnly = true)
    public MutedUserDTO.ListResponse getMutedUsers(Long userId) {
        List<MutedUser> mutedUsers = mutedUserRepository.findByUserId(userId);
        
        List<MutedUserDTO.Response> responses = mutedUsers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return MutedUserDTO.ListResponse.builder()
                .mutedUsers(responses)
                .count(responses.size())
                .build();
    }

    /**
     * 获取屏蔽的用户ID列表（带缓存）
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#userId")
    public List<Long> getMutedUserIds(Long userId) {
        return mutedUserRepository.findMutedUserIds(userId);
    }

    /**
     * 检查是否已屏蔽某用户
     */
    @Transactional(readOnly = true)
    public boolean isUserMuted(Long userId, Long mutedUserId) {
        return mutedUserRepository.existsByUserIdAndMutedUserId(userId, mutedUserId);
    }

    /**
     * 批量检查是否被屏蔽
     * 返回被屏蔽的用户ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> filterMutedUsers(Long userId, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> mutedIds = getMutedUserIds(userId);
        return targetUserIds.stream()
                .filter(mutedIds::contains)
                .collect(Collectors.toList());
    }

    /**
     * 批量过滤掉被屏蔽的用户
     * 返回未被屏蔽的用户ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> excludeMutedUsers(Long userId, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> mutedIds = getMutedUserIds(userId);
        return targetUserIds.stream()
                .filter(id -> !mutedIds.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询屏蔽记录
     */
    @Transactional(readOnly = true)
    public MutedUserDTO.Response findByUserIdAndMutedUserId(Long userId, Long mutedUserId) {
        return mutedUserRepository.findByUserIdAndMutedUserId(userId, mutedUserId)
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * 清除用户的所有屏蔽记录
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#userId")
    public void clearAllMutedUsers(Long userId) {
        mutedUserRepository.deleteAllByUserId(userId);
        log.info("Cleared all muted users for user {}", userId);
    }

    /**
     * 转换为响应
     */
    private MutedUserDTO.Response toResponse(MutedUser mutedUser) {
        UserProfile mutedUserEntity = mutedUser.getMutedUser();
        
        return MutedUserDTO.Response.builder()
                .id(mutedUser.getId())
                .mutedUserId(mutedUserEntity.getId())
                .mutedUserEmail(mutedUserEntity.getEmail())
                .mutedUserFullName(mutedUserEntity.getFullName())
                .mutedUserAvatarUrl(mutedUserEntity.getAvatarUrl())
                .dateMuted(mutedUser.getDateMuted())
                .build();
    }
}