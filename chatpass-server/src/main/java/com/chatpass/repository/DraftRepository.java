package com.chatpass.repository;

import com.chatpass.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {
    
    List<Draft> findByUserId(Long userId);
    
    Optional<Draft> findByUserIdAndRecipientIdAndTopic(Long userId, Long recipientId, String topic);
    
    @Query("SELECT d FROM Draft d WHERE d.user.id = :userId ORDER BY d.lastUpdated DESC")
    List<Draft> findByUserIdOrderByLastUpdatedDesc(@Param("userId") Long userId);
    
    void deleteByUserIdAndRecipientIdAndTopic(Long userId, Long recipientId, String topic);
}