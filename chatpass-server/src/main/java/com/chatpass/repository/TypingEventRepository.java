package com.chatpass.repository;

import com.chatpass.entity.TypingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TypingEventRepository extends JpaRepository<TypingEvent, Long> {
    
    List<TypingEvent> findByRecipientIdAndExpiresAtAfter(Long recipientId, LocalDateTime now);
    
    @Query("SELECT t FROM TypingEvent t WHERE t.recipient.id = :recipientId AND t.topic = :topic AND t.expiresAt > :now")
    List<TypingEvent> findActiveTypingInTopic(@Param("recipientId") Long recipientId, 
                                               @Param("topic") String topic, 
                                               @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM TypingEvent t WHERE t.recipient.id = :recipientId AND t.recipient.type = 2 AND t.expiresAt > :now")
    List<TypingEvent> findActiveTypingInDirectMessage(@Param("recipientId") Long recipientId, 
                                                       @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM TypingEvent t WHERE t.expiresAt < :now")
    int deleteExpiredEvents(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM TypingEvent t WHERE t.user.id = :userId AND t.recipient.id = :recipientId")
    void deleteUserTypingEvents(@Param("userId") Long userId, @Param("recipientId") Long recipientId);
}