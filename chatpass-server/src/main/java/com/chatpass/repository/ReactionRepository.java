package com.chatpass.repository;

import com.chatpass.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    List<Reaction> findByMessageId(Long messageId);
    
    @Query("SELECT r FROM Reaction r WHERE r.message.id = :messageId ORDER BY r.emojiCode, r.dateCreated")
    List<Reaction> findByMessageIdOrderByEmoji(@Param("messageId") Long messageId);
    
    @Query("SELECT r.emojiCode, COUNT(r) FROM Reaction r WHERE r.message.id = :messageId GROUP BY r.emojiCode")
    List<Object[]> countByMessageIdGroupByEmoji(@Param("messageId") Long messageId);
    
    // Analytics statistics
    
    /**
     * 统计 Realm 表情数
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.message.realm.id = :realmId")
    Long countByRealmId(@Param("realmId") Long realmId);
    
    /**
     * 热门表情统计
     */
    @Query("SELECT r.emojiCode, COUNT(r) FROM Reaction r WHERE r.message.realm.id = :realmId AND r.dateCreated BETWEEN :start AND :end GROUP BY r.emojiCode ORDER BY COUNT(r) DESC")
    List<Object[]> findTopReactions(@Param("realmId") Long realmId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end, @Param("limit") int limit);
    List<Object[]> countByMessageGroupByEmoji(@Param("messageId") Long messageId);
    
    Optional<Reaction> findByUserIdAndMessageIdAndEmojiCode(Long userId, Long messageId, String emojiCode);
    
    boolean existsByUserIdAndMessageIdAndEmojiCode(Long userId, Long messageId, String emojiCode);
    
    void deleteByUserIdAndMessageIdAndEmojiCode(Long userId, Long messageId, String emojiCode);
    
    List<Reaction> findByMessageIdAndEmojiCode(Long messageId, String emojiCode);
}