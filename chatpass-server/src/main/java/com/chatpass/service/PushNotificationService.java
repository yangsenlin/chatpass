package com.chatpass.service;

import com.chatpass.entity.MobilePush;
import com.chatpass.entity.PushNotification;
import com.chatpass.repository.MobilePushRepository;
import com.chatpass.repository.PushNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PushNotificationService
 * 推送通知服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final MobilePushRepository pushConfigRepository;
    private final PushNotificationRepository notificationRepository;

    /**
     * 创建推送配置
     */
    @Transactional
    public MobilePush createPushConfig(Long realmId, String pushType, String projectId,
                                        String apiKey, String senderId) {
        MobilePush push = MobilePush.builder()
                .realmId(realmId)
                .pushType(pushType)
                .projectId(projectId)
                .apiKey(apiKey)
                .senderId(senderId)
                .isActive(true)
                .dateCreated(LocalDateTime.now())
                .build();

        return pushConfigRepository.save(push);
    }

    /**
     * 获取推送配置
     */
    public Optional<MobilePush> getPushConfig(Long configId) {
        return pushConfigRepository.findById(configId);
    }

    /**
     * 获取推送配置（按类型）
     */
    public Optional<MobilePush> getPushConfigByType(Long realmId, String pushType) {
        return pushConfigRepository.findByRealmIdAndPushType(realmId, pushType);
    }

    /**
     * 获取 Realm 所有配置
     */
    public List<MobilePush> getRealmPushConfigs(Long realmId) {
        return pushConfigRepository.findByRealmId(realmId);
    }

    /**
     * 获取活跃配置
     */
    public List<MobilePush> getActiveConfigs(Long realmId) {
        return pushConfigRepository.findByRealmIdAndIsActiveTrue(realmId);
    }

    /**
     * 更新配置状态
     */
    @Transactional
    public MobilePush updateConfigStatus(Long configId, Boolean isActive) {
        MobilePush push = pushConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("推送配置不存在"));

        push.setIsActive(isActive);
        push.setLastUpdated(LocalDateTime.now());

        return pushConfigRepository.save(push);
    }

    /**
     * 删除推送配置
     */
    @Transactional
    public void deletePushConfig(Long configId) {
        pushConfigRepository.deleteById(configId);
    }

    /**
     * 创建推送通知
     */
    @Transactional
    public PushNotification createNotification(Long pushConfigId, Long userId, Long messageId,
                                                String deviceToken, String notificationType,
                                                String title, String body, String dataPayload) {
        PushNotification notification = PushNotification.builder()
                .pushConfigId(pushConfigId)
                .userId(userId)
                .messageId(messageId)
                .deviceToken(deviceToken)
                .notificationType(notificationType)
                .title(title)
                .body(body)
                .dataPayload(dataPayload)
                .status(PushNotification.STATUS_PENDING)
                .dateCreated(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 获取用户通知
     */
    public List<PushNotification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    /**
     * 获取待发送通知
     */
    public List<PushNotification> getPendingNotifications() {
        return notificationRepository.findByStatus(PushNotification.STATUS_PENDING);
    }

    /**
     * 标记已发送
     */
    @Transactional
    public void markNotificationSent(Long notificationId) {
        notificationRepository.updateStatus(notificationId, PushNotification.STATUS_SENT, LocalDateTime.now());
    }

    /**
     * 标记失败
     */
    @Transactional
    public void markNotificationFailed(Long notificationId, String error) {
        notificationRepository.markFailed(notificationId, error);
    }

    /**
     * 发送推送通知（模拟）
     */
    @Transactional
    public boolean sendPush(Long notificationId) {
        PushNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

        MobilePush config = pushConfigRepository.findById(notification.getPushConfigId())
                .orElseThrow(() -> new IllegalArgumentException("推送配置不存在"));

        try {
            // 模拟发送推送
            log.info("Sending push notification to user {} (device: {}, type: {})",
                    notification.getUserId(), notification.getDeviceToken(), config.getPushType());

            // 实际实现需要根据 pushType 调用不同的推送服务
            // FCM: 使用 FirebaseMessaging
            // APNS: 使用 PushNotifications (Apple)

            markNotificationSent(notificationId);
            return true;
        } catch (Exception e) {
            log.error("Failed to send push notification: {}", e.getMessage());
            markNotificationFailed(notificationId, e.getMessage());
            return false;
        }
    }

    /**
     * 批量发送
     */
    @Transactional
    public int sendBatchNotifications(Long pushConfigId) {
        List<PushNotification> notifications = notificationRepository
                .findByPushConfigIdAndStatus(pushConfigId, PushNotification.STATUS_PENDING);

        int sent = 0;
        for (PushNotification notification : notifications) {
            if (sendPush(notification.getId())) {
                sent++;
            }
        }

        return sent;
    }

    /**
     * 统计通知状态
     */
    public Long countNotificationsByStatus(String status) {
        return notificationRepository.countByStatus(status);
    }
}