package com.chatpass.repository;

import com.chatpass.entity.MutedTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MutedTopicRepository extends JpaRepository<MutedTopic, Long> {
    
    List<MutedTopic> findByUserId(Long userId);
    
    Optional<MutedTopic> findByUserIdAndStreamIdAndTopicName(Long userId, Long streamId, String topicName);
    
    boolean existsByUserIdAndStreamIdAndTopicName(Long userId, Long streamId, String topicName);
    
    void deleteByUserIdAndStreamIdAndTopicName(Long userId, Long streamId, String topicName);
}