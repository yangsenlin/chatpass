package com.chatpass.repository;

import com.chatpass.entity.ScheduledMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduledMessage Repository
 * 
 * 定时消息数据访问层
 */
@Repository
public interface ScheduledMessageRepository extends JpaRepository<ScheduledMessage, Long> {

    /**
     * 获取用户的所有定时消息（未发送）
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.sender.id = :senderId AND sm.delivered = false ORDER BY sm.scheduledTimestamp")
    List<ScheduledMessage> findPendingBySenderId(@Param("senderId") Long senderId);

    /**
     * 获取用户的定时发送消息（未发送）
     */
    @Query("SELECT sm FROM ScheduledMessage sm " +
           "WHERE sm.sender.id = :senderId " +
           "AND sm.deliveryType = :deliveryType " +
           "AND sm.delivered = false " +
           "ORDER BY sm.scheduledTimestamp")
    List<ScheduledMessage> findPendingBySenderIdAndDeliveryType(
            @Param("senderId") Long senderId, 
            @Param("deliveryType") Integer deliveryType);

    /**
     * 获取组织所有待发送的定时消息（未发送）
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.realm.id = :realmId AND sm.delivered = false AND sm.failed = false ORDER BY sm.scheduledTimestamp")
    List<ScheduledMessage> findPendingByRealmId(@Param("realmId") Long realmId);

    /**
     * 获取需要立即发送的消息（时间已到）
     */
    @Query("SELECT sm FROM ScheduledMessage sm " +
           "WHERE sm.delivered = false " +
           "AND sm.failed = false " +
           "AND sm.scheduledTimestamp <= :now " +
           "ORDER BY sm.scheduledTimestamp")
    List<ScheduledMessage> findDueMessages(@Param("now") LocalDateTime now);

    /**
     * 获取组织的需要发送的消息
     */
    @Query("SELECT sm FROM ScheduledMessage sm " +
           "WHERE sm.realm.id = :realmId " +
           "AND sm.delivered = false " +
           "AND sm.failed = false " +
           "AND sm.scheduledTimestamp <= :now " +
           "ORDER BY sm.scheduledTimestamp")
    List<ScheduledMessage> findDueMessagesByRealmId(
            @Param("realmId") Long realmId, 
            @Param("now") LocalDateTime now);

    /**
     * 获取已发送的消息（用于历史查看）
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.sender.id = :senderId AND sm.delivered = true ORDER BY sm.scheduledTimestamp DESC")
    List<ScheduledMessage> findDeliveredBySenderId(@Param("senderId") Long senderId);

    /**
     * 获取发送失败的消息
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.sender.id = :senderId AND sm.failed = true ORDER BY sm.requestTimestamp DESC")
    List<ScheduledMessage> findFailedBySenderId(@Param("senderId") Long senderId);

    /**
     * 统计用户待发送消息数量
     */
    @Query("SELECT COUNT(sm) FROM ScheduledMessage sm WHERE sm.sender.id = :senderId AND sm.delivered = false AND sm.failed = false")
    Long countPendingBySenderId(@Param("senderId") Long senderId);

    /**
     * 根据ID和发送者查询（权限验证）
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.id = :id AND sm.sender.id = :senderId")
    Optional<ScheduledMessage> findByIdAndSenderId(@Param("id") Long id, @Param("senderId") Long senderId);

    /**
     * 更新消息为已发送状态
     */
    @Modifying
    @Query("UPDATE ScheduledMessage sm SET " +
           "sm.delivered = true, " +
           "sm.deliveredMessage.id = :messageId, " +
           "sm.dateDelivered = :deliveredAt " +
           "WHERE sm.id = :id")
    void markAsDelivered(@Param("id") Long id, 
                         @Param("messageId") Long messageId, 
                         @Param("deliveredAt") LocalDateTime deliveredAt);

    /**
     * 更新消息为失败状态
     */
    @Modifying
    @Query("UPDATE ScheduledMessage sm SET sm.failed = true, sm.failureMessage = :failureMessage WHERE sm.id = :id")
    void markAsFailed(@Param("id") Long id, @Param("failureMessage") String failureMessage);

    /**
     * 删除用户的定时消息
     */
    @Modifying
    @Query("DELETE FROM ScheduledMessage sm WHERE sm.id = :id AND sm.sender.id = :senderId")
    void deleteByIdAndSenderId(@Param("id") Long id, @Param("senderId") Long senderId);

    /**
     * 删除用户的所有定时消息（用户被删除时）
     */
    @Modifying
    @Query("DELETE FROM ScheduledMessage sm WHERE sm.sender.id = :userId")
    void deleteAllBySenderId(@Param("userId") Long userId);

    /**
     * 更新定时时间
     */
    @Modifying
    @Query("UPDATE ScheduledMessage sm SET sm.scheduledTimestamp = :newTimestamp WHERE sm.id = :id AND sm.sender.id = :senderId AND sm.delivered = false")
    void updateScheduledTimestamp(@Param("id") Long id, 
                                  @Param("senderId") Long senderId, 
                                  @Param("newTimestamp") LocalDateTime newTimestamp);

    /**
     * 获取提醒目标消息相关的提醒
     */
    @Query("SELECT sm FROM ScheduledMessage sm WHERE sm.reminderTargetMessageId = :messageId AND sm.delivered = false")
    List<ScheduledMessage> findPendingRemindersByTargetMessageId(@Param("messageId") Long messageId);

    /**
     * 检查定时消息是否存在（未发送）
     */
    @Query("SELECT CASE WHEN COUNT(sm) > 0 THEN true ELSE false END FROM ScheduledMessage sm WHERE sm.id = :id AND sm.delivered = false")
    boolean existsPendingById(@Param("id") Long id);
}