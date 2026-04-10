package com.chatpass.repository;

import com.chatpass.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    
    List<PushNotification> findByUserId(Long userId);
    
    List<PushNotification> findByStatus(String status);
    
    List<PushNotification> findByPushConfigIdAndStatus(Long pushConfigId, String status);
    
    @Query("SELECT pn FROM PushNotification pn WHERE pn.userId = :userId AND pn.dateCreated BETWEEN :start AND :end")
    List<PushNotification> findByUserIdAndTimeRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(pn) FROM PushNotification pn WHERE pn.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Modifying
    @Query("UPDATE PushNotification pn SET pn.status = :status, pn.dateSent = :dateSent WHERE pn.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("dateSent") LocalDateTime dateSent);
    
    @Modifying
    @Query("UPDATE PushNotification pn SET pn.status = 'FAILED', pn.errorMessage = :error WHERE pn.id = :id")
    void markFailed(@Param("id") Long id, @Param("error") String error);
}