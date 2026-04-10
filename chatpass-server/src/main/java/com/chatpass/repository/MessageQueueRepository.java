package com.chatpass.repository;

import com.chatpass.entity.MessageQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageQueueRepository extends JpaRepository<MessageQueue, Long> {
    
    Optional<MessageQueue> findByQueueName(String queueName);
    
    List<MessageQueue> findByRealmId(Long realmId);
    
    List<MessageQueue> findByRealmIdAndIsActiveTrue(Long realmId);
    
    @Query("SELECT COUNT(q) FROM MessageQueue q WHERE q.realmId = :realmId AND q.isActive = true")
    Long countActiveQueues(@Param("realmId") Long realmId);
}