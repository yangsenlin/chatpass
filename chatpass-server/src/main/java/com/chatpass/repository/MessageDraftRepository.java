package com.chatpass.repository;

import com.chatpass.entity.MessageDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 消息草稿仓库
 */
@Repository
public interface MessageDraftRepository extends JpaRepository<MessageDraft, Long> {
    
    /**
     * 根据用户ID查找所有草稿
     */
    List<MessageDraft> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和Stream ID查找草稿
     */
    Optional<MessageDraft> findByUserIdAndStreamId(Long userId, Long streamId);
    
    /**
     * 根据用户ID和话题查找草稿
     */
    Optional<MessageDraft> findByUserIdAndStreamIdAndTopic(Long userId, Long streamId, String topic);
    
    /**
     * 根据用户ID和目标用户查找私信草稿
     */
    Optional<MessageDraft> findByUserIdAndToUserIds(Long userId, String toUserIds);
    
    /**
     * 删除用户的草稿
     */
    @Modifying
    @Query("DELETE FROM MessageDraft d WHERE d.userId = :userId AND d.id = :draftId")
    void deleteByUserIdAndId(@Param("userId") Long userId, @Param("draftId") Long draftId);
    
    /**
     * 清空用户草稿
     */
    @Modifying
    @Query("DELETE FROM MessageDraft d WHERE d.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
