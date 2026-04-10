package com.chatpass.repository;

import com.chatpass.entity.QueuedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueuedMessageRepository extends JpaRepository<QueuedMessage, Long> {
    
    List<QueuedMessage> findByQueueId(Long queueId);
    
    List<QueuedMessage> findByStatus(String status);
    
    List<QueuedMessage> findByQueueIdAndStatus(Long queueId, String status);
    
    @Query("SELECT qm FROM QueuedMessage qm WHERE qm.status = 'PENDING' OR (qm.status = 'RETRYING' AND qm.retryCount < :maxRetry)")
    List<QueuedMessage> findPendingMessages(@Param("maxRetry") Integer maxRetry);
    
    @Query("SELECT COUNT(qm) FROM QueuedMessage qm WHERE qm.queueId = :queueId AND qm.status = :status")
    Long countByQueueIdAndStatus(@Param("queueId") Long queueId, @Param("status") String status);
    
    @Modifying
    @Query("UPDATE QueuedMessage qm SET qm.status = :status, qm.dateSent = :dateSent WHERE qm.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("dateSent") LocalDateTime dateSent);
    
    @Modifying
    @Query("UPDATE QueuedMessage qm SET qm.status = 'FAILED', qm.errorMessage = :error WHERE qm.id = :id")
    void markFailed(@Param("id") Long id, @Param("error") String error);
    
    @Modifying
    @Query("UPDATE QueuedMessage qm SET qm.retryCount = qm.retryCount + 1, qm.status = 'RETRYING', qm.lastRetry = :lastRetry WHERE qm.id = :id")
    void incrementRetry(@Param("id") Long id, @Param("lastRetry") LocalDateTime lastRetry);
}