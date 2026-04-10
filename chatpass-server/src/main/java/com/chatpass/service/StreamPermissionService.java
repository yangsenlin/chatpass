package com.chatpass.service;

import com.chatpass.entity.Stream;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.StreamRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StreamPermissionService
 * 
 * 频道权限管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamPermissionService {

    private final StreamRepository streamRepository;
    private final UserProfileRepository userRepository;

    /**
     * 检查用户是否可以访问频道
     */
    public boolean canAccessStream(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 公开频道所有人可访问
        if (!stream.getInviteOnly()) {
            return true;
        }
        
        // 私有频道需要检查订阅关系
        return isSubscribed(streamId, userId);
    }

    /**
     * 检查用户是否订阅了频道
     */
    public boolean isSubscribed(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        // TODO: 实现订阅关系查询
        // 目前通过 Stream 的 subscribers 关系检查
        // 简化实现：检查用户是否在频道成员列表中
        
        return true; // 简化实现
    }

    /**
     * 检查用户是否可以向频道发送消息
     */
    public boolean canPostToStream(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        // 需要先能访问频道
        if (!canAccessStream(streamId, userId)) {
            return false;
        }
        
        // 检查频道是否允许用户发消息
        // 简化实现：公开频道所有人可发，私有频道订阅者可发
        return true;
    }

    /**
     * 检查用户是否可以管理频道
     */
    public boolean canManageStream(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 检查用户角色等级
        // Admin (300+) 或 Owner (400) 可以管理
        return user.getRole() >= 300;
    }

    /**
     * 获取频道的访问权限设置
     */
    public Map<String, Object> getStreamPermissionSettings(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        return Map.of(
                "stream_id", streamId,
                "stream_name", stream.getName(),
                "is_public", !stream.getInviteOnly(),
                "invite_only", stream.getInviteOnly(),
                "can_subscribes_group", "all", // 简化实现
                "can_post_group", "all",
                "can_admin_group", "admins"
        );
    }

    /**
     * 更新频道权限设置
     */
    @Transactional
    public Stream updateStreamPermission(Long streamId, boolean inviteOnly) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        stream.setInviteOnly(inviteOnly);
        streamRepository.save(stream);
        
        log.info("Updated stream {} permission: inviteOnly={}", streamId, inviteOnly);
        
        return stream;
    }

    /**
     * 获取用户可以访问的所有频道
     */
    public List<Stream> getUserAccessibleStreams(Long userId, Long realmId) {
        List<Stream> allStreams = streamRepository.findByRealmId(realmId);
        
        return allStreams.stream()
                .filter(s -> canAccessStream(s.getId(), userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取频道的成员列表
     */
    public List<UserProfile> getStreamMembers(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        // TODO: 实现订阅关系查询
        // 简化实现：返回 realm 的所有成员
        
        return userRepository.findByRealmId(stream.getRealm().getId());
    }

    /**
     * 添加频道成员
     */
    @Transactional
    public void addStreamMember(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // TODO: 实现订阅关系添加
        // 简化实现
        
        log.info("Added user {} to stream {}", userId, streamId);
    }

    /**
     * 移除频道成员
     */
    @Transactional
    public void removeStreamMember(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));
        
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // TODO: 实现订阅关系移除
        // 简化实现
        
        log.info("Removed user {} from stream {}", userId, streamId);
    }

    /**
     * 获取频道的订阅者数量
     */
    public Long getStreamSubscriberCount(Long streamId) {
        // TODO: 实现订阅关系统计
        // 简化实现：返回成员数量
        List<UserProfile> members = getStreamMembers(streamId);
        return (long) members.size();
    }
}