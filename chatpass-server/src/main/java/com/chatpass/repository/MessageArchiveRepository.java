package com.chatpass.repository;

import com.chatpass.entity.MessageArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息归档仓库
 */
@Repository
public interface MessageArchiveRepository extends JpaRepository<MessageArchive, Long> {
    
    /**
     * 根据组织ID查找归档消息
     */
    List<MessageArchive> findByRealmIdOrderByArchivedAtDesc(Long realmId);
    
    /**
     * 根据原消息ID查找归档
     */
    Optional<MessageArchive> findByOriginalMessageId(Long originalMessageId);
    
    /**
     * 根据Stream查找归档
     */
    List<MessageArchive> findByStreamIdOrderByArchivedAtDesc(Long streamId);
    
    /**
     * 根据时间范围查找归档
     */
    @Query("SELECT ma FROM MessageArchive ma WHERE ma.realmId = :realmId AND ma.archivedAt BETWEEN :start AND :end ORDER BY ma.archivedAt DESC")
    List<MessageArchive> findByRealmIdAndArchivedAtBetween(@Param("realmId") Long realmId, 
                                                             @Param("start") LocalDateTime start, 
                                                             @Param("end") LocalDateTime end);
    
    /**
     * 查找可恢复的归档
     */
    @Query("SELECT ma FROM MessageArchive ma WHERE ma.realmId = :realmId AND ma.isRecoverable = true AND ma.recoverUntil >= :now ORDER BY ma.archivedAt DESC")
    List<MessageArchive> findRecoverableArchives(@Param("realmId") Long realmId, @Param("now") LocalDateTime now);
    
    /**
     * 统计归档消息数量
     */
    long countByRealmId(Long realmId);
    
    /**
     * 标记为不可恢复
     */
    @Modifying
    @Query("UPDATE MessageArchive ma SET ma.isRecoverable = false WHERE ma.recoverUntil < :now")
    void markUnrecoverable(@Param("now") LocalDateTime now);
    
    /**
     * 删除过期归档
     */
    @Modifying
    @Query("DELETE FROM MessageArchive ma WHERE ma.isRecoverable = false AND ma.archivedAt < :threshold")
    void deleteOldArchives(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 根据归档策略查找
     */
    List<MessageArchive> findByRealmIdAndArchivePolicyOrderByArchivedAtDesc(Long realmId, String archivePolicy);
}
