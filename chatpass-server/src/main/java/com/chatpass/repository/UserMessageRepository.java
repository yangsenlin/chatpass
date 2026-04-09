package com.chatpass.repository;

import com.chatpass.entity.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
    
    List<UserMessage> findByUserProfileId(Long userProfileId);
    
    Optional<UserMessage> findByUserProfileIdAndMessageId(Long userProfileId, Long messageId);
    
    @Query("SELECT um FROM UserMessage um WHERE um.userProfile.id = :userId AND (um.flags & :flag) != 0")
    List<UserMessage> findByUserIdAndFlagSet(@Param("userId") Long userId, @Param("flag") Long flag);
    
    @Query("SELECT COUNT(um) FROM UserMessage um WHERE um.userProfile.id = :userId AND (um.flags & 1) = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserMessage um SET um.flags = um.flags | 1 WHERE um.userProfile.id = :userId AND um.message.id IN :messageIds")
    void markAsRead(@Param("userId") Long userId, @Param("messageIds") List<Long> messageIds);
    
    boolean existsByUserProfileIdAndMessageId(Long userProfileId, Long messageId);
}