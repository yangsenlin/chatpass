package com.chatpass.repository;

import com.chatpass.entity.Message;
import com.chatpass.entity.ReadReceipt;
import com.chatpass.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Long> {

    Optional<ReadReceipt> findByMessageAndUser(Message message, UserProfile user);
    
    List<ReadReceipt> findByMessage(Message message);
    
    List<ReadReceipt> findByUser(UserProfile user);
    
    @Query("SELECT rr FROM ReadReceipt rr WHERE rr.message.id = :messageId")
    List<ReadReceipt> findByMessageId(@Param("messageId") Long messageId);
    
    @Query("SELECT rr FROM ReadReceipt rr WHERE rr.user.id = :userId")
    List<ReadReceipt> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(rr) FROM ReadReceipt rr WHERE rr.message.id = :messageId")
    Long countByMessageId(@Param("messageId") Long messageId);
    
    @Query("SELECT rr FROM ReadReceipt rr WHERE rr.message.id IN :messageIds AND rr.user.id = :userId")
    List<ReadReceipt> findByMessageIdsAndUserId(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);
    
    boolean existsByMessageAndUser(Message message, UserProfile user);
    
    @Query("SELECT MAX(rr.readAt) FROM ReadReceipt rr WHERE rr.user.id = :userId AND rr.message.recipient.id = :recipientId")
    LocalDateTime findLastReadTime(@Param("userId") Long userId, @Param("recipientId") Long recipientId);
}