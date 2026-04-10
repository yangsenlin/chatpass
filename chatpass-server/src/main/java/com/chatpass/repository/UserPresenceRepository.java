package com.chatpass.repository;

import com.chatpass.entity.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {
    
    Optional<UserPresence> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserPresence up WHERE up.status = :status AND up.lastActiveTime > :since")
    List<UserPresence> findActiveUsers(@Param("status") String status, @Param("since") LocalDateTime since);
    
    @Modifying
    @Query("UPDATE UserPresence up SET up.status = :status, up.lastActiveTime = :time WHERE up.user.id = :userId")
    void updateStatus(@Param("userId") Long userId, @Param("status") String status, @Param("time") LocalDateTime time);
}