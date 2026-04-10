package com.chatpass.repository;

import com.chatpass.entity.TopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TopicSubscriptionRepository
 */
@Repository
public interface TopicSubscriptionRepository extends JpaRepository<TopicSubscription, Long> {

    /**
     * 查找用户的订阅
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.userId = :userId ORDER BY t.dateSubscribed DESC")
    List<TopicSubscription> findByUserId(@Param("userId") Long userId);

    /**
     * 查找 Stream 的订阅
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.streamId = :streamId ORDER BY t.topicName")
    List<TopicSubscription> findByStreamId(@Param("streamId") Long streamId);

    /**
     * 查找特定订阅
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.userId = :userId AND t.streamId = :streamId AND t.topicName = :topicName")
    Optional<TopicSubscription> findByUserStreamTopic(@Param("userId") Long userId, @Param("streamId") Long streamId, @Param("topicName") String topicName);

    /**
     * 查找 Topic 的订阅者
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.streamId = :streamId AND t.topicName = :topicName AND t.subscriptionType != 'MUTE'")
    List<TopicSubscription> findTopicSubscribers(@Param("streamId") Long streamId, @Param("topicName") String topicName);

    /**
     * 统计 Topic 订阅数
     */
    @Query("SELECT COUNT(t) FROM TopicSubscription t WHERE t.streamId = :streamId AND t.topicName = :topicName")
    Long countByStreamIdAndTopicName(@Param("streamId") Long streamId, @Param("topicName") String topicName);

    /**
     * 统计用户订阅数
     */
    @Query("SELECT COUNT(t) FROM TopicSubscription t WHERE t.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 查找静音订阅
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.userId = :userId AND t.subscriptionType = 'MUTE'")
    List<TopicSubscription> findMutedSubscriptions(@Param("userId") Long userId);

    /**
     * 查找通知订阅
     */
    @Query("SELECT t FROM TopicSubscription t WHERE t.userId = :userId AND t.subscriptionType = 'NOTIFY'")
    List<TopicSubscription> findNotifySubscriptions(@Param("userId") Long userId);

    /**
     * 删除订阅
     */
    @Query("DELETE FROM TopicSubscription t WHERE t.userId = :userId AND t.streamId = :streamId AND t.topicName = :topicName")
    void deleteByUserStreamTopic(@Param("userId") Long userId, @Param("streamId") Long streamId, @Param("topicName") String topicName);
}