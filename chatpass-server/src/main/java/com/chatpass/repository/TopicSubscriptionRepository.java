package com.chatpass.repository;

import com.chatpass.entity.TopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 话题订阅仓库
 */
@Repository
public interface TopicSubscriptionRepository extends JpaRepository<TopicSubscription, Long> {
    
    /**
     * 根据用户ID查找所有订阅
     */
    List<TopicSubscription> findByUserIdOrderBySubscribedAtDesc(Long userId);
    
    /**
     * 根据Stream和Topic查找订阅者
     */
    List<TopicSubscription> findByStreamIdAndTopic(Long streamId, String topic);
    
    /**
     * 根据用户和Stream/Topic查找订阅
     */
    Optional<TopicSubscription> findByUserIdAndStreamIdAndTopic(Long userId, Long streamId, String topic);
    
    /**
     * 检查用户是否订阅了话题
     */
    boolean existsByUserIdAndStreamIdAndTopic(Long userId, Long streamId, String topic);
    
    /**
     * 根据Stream查找订阅
     */
    List<TopicSubscription> findByStreamId(Long streamId);
    
    /**
     * 查找未静音的订阅者
     */
    @Query("SELECT ts FROM TopicSubscription ts WHERE ts.streamId = :streamId AND ts.topic = :topic AND ts.isMuted = false")
    List<TopicSubscription> findActiveSubscribers(@Param("streamId") Long streamId, @Param("topic") String topic);
    
    /**
     * 删除订阅
     */
    @Modifying
    @Query("DELETE FROM TopicSubscription ts WHERE ts.userId = :userId AND ts.streamId = :streamId AND ts.topic = :topic")
    void deleteByUserIdAndStreamIdAndTopic(@Param("userId") Long userId, @Param("streamId") Long streamId, @Param("topic") String topic);
    
    /**
     * 统计话题的订阅者数量
     */
    @Query("SELECT COUNT(ts) FROM TopicSubscription ts WHERE ts.streamId = :streamId AND ts.topic = :topic")
    long countByStreamIdAndTopic(@Param("streamId") Long streamId, @Param("topic") String topic);
    
    /**
     * 查找用户在某个Stream的订阅
     */
    List<TopicSubscription> findByUserIdAndStreamId(Long userId, Long streamId);
}
