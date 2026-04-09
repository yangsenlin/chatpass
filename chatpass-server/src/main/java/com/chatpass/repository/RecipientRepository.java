package com.chatpass.repository;

import com.chatpass.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    
    Optional<Recipient> findByTypeAndStreamId(Integer type, Long streamId);
    
    @Query("SELECT r FROM Recipient r WHERE r.type = 1 AND r.streamId = :streamId")
    Optional<Recipient> findStreamRecipient(@Param("streamId") Long streamId);
}