package com.chatpass.repository;

import com.chatpass.entity.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 消息阅读状态仓库
 */
@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {
    
    /**
     * 根据用户ID查找阅读状态
     */
    List<MessageReadStatus> findByUserIdOrderByReadAtDesc(Long userId);
    
    /**
     * 根据消息ID查找阅读状态
     */
    List<MessageReadStatus> findByMessageIdOrderByReadAtAsc(Long messageId);
    
    /**
     * 根据用户和消息查找阅读状态
     */
    Optional<MessageReadStatus> findByUserIdAndMessageId(Long userId, Long messageId);
    
    /**
     * 检查消息是否已读
     */
    boolean existsByUserIdAndMessageId(Long userId, Long messageId);
    
    /**
     * 统计用户已读消息数
     */
    long countByUserId(Long userId);
    
    /**
     * 统计消息已读用户数
     */
    @Query("SELECT COUNT(mrs) FROM MessageReadStatus mrs WHERE mrs.messageId = :messageId")
    long countByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 获取已读用户列表
     */
    @Query("SELECT mrs.userId FROM MessageReadStatus mrs WHERE mrs.messageId = :messageId")
    List<Long> findReadUsers(@Param("messageId") Long messageId);
    
    /**
     * 删除阅读状态
     */
    @Modifying
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.userId = :userId AND mrs.messageId = :messageId")
    void deleteByUserIdAndMessageId(@Param("userId") Long userId, @Param("messageId") Long messageId);
}
