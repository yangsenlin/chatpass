package com.chatpass.repository;

import com.chatpass.entity.MessageEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MessageEditHistoryRepository
 */
@Repository
public interface MessageEditHistoryRepository extends JpaRepository<MessageEditHistory, Long> {

    /**
     * 查找消息的所有编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.message.id = :messageId ORDER BY h.editTime DESC")
    List<MessageEditHistory> findByMessageId(@Param("messageId") Long messageId);

    /**
     * 查找消息的最近编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.message.id = :messageId ORDER BY h.editTime DESC LIMIT 1")
    MessageEditHistory findLatestByMessageId(@Param("messageId") Long messageId);

    /**
     * 查找用户的编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.editor.id = :editorId ORDER BY h.editTime DESC")
    List<MessageEditHistory> findByEditorId(@Param("editorId") Long editorId);

    /**
     * 查找特定时间范围内的编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.message.id = :messageId AND h.editTime >= :startTime AND h.editTime <= :endTime ORDER BY h.editTime DESC")
    List<MessageEditHistory> findByMessageIdAndTimeRange(
            @Param("messageId") Long messageId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 统计消息的编辑次数
     */
    @Query("SELECT COUNT(h) FROM MessageEditHistory h WHERE h.message.id = :messageId")
    Long countByMessageId(@Param("messageId") Long messageId);

    /**
     * 统计用户的编辑次数
     */
    @Query("SELECT COUNT(h) FROM MessageEditHistory h WHERE h.editor.id = :editorId")
    Long countByEditorId(@Param("editorId") Long editorId);

    /**
     * 查找内容编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.message.id = :messageId AND h.editType IN (1, 3) ORDER BY h.editTime DESC")
    List<MessageEditHistory> findContentEditsByMessageId(@Param("messageId") Long messageId);

    /**
     * 查找 Topic 编辑历史
     */
    @Query("SELECT h FROM MessageEditHistory h WHERE h.message.id = :messageId AND h.editType IN (2, 3) ORDER BY h.editTime DESC")
    List<MessageEditHistory> findTopicEditsByMessageId(@Param("messageId") Long messageId);
}