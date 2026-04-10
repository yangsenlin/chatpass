package com.chatpass.repository;

import com.chatpass.entity.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
    
    List<UserTopic> findByUserId(Long userId);
    
    Optional<UserTopic> findByUserIdAndRecipientIdAndTopicName(Long userId, Long recipientId, String topicName);
    
    @Modifying
    @Query("UPDATE UserTopic ut SET ut.lastReadMessageId = :messageId WHERE ut.user.id = :userId AND ut.recipient.id = :recipientId AND ut.topicName = :topicName")
    void updateLastRead(@Param("userId") Long userId, @Param("recipientId") Long recipientId, 
                        @Param("topicName") String topicName, @Param("messageId") Long messageId);
}