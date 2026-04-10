package com.chatpass.repository;

import com.chatpass.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserProfileId(Long userProfileId);
    
    List<Subscription> findByUserProfileIdAndActiveTrue(Long userProfileId);
    
    List<Subscription> findByStreamId(Long streamId);
    
    List<Subscription> findByStreamIdAndActiveTrue(Long streamId);
    
    Optional<Subscription> findByUserProfileIdAndStreamId(Long userProfileId, Long streamId);
    
    @Query("SELECT s FROM Subscription s WHERE s.userProfile.id = :userId AND s.stream.deactivated = false AND s.active = true")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.stream.id = :streamId AND s.active = true")
    Long countActiveSubscribersByStreamId(@Param("streamId") Long streamId);
    
    @Query("SELECT s FROM Subscription s WHERE s.userProfile.id = :userId AND s.isMuted = false AND s.active = true")
    List<Subscription> findUnmutedSubscriptionsByUserId(@Param("userId") Long userId);
    
    boolean existsByUserProfileIdAndStreamId(Long userProfileId, Long streamId);
}