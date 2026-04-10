package com.chatpass.repository;

import com.chatpass.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    List<Attachment> findByMessageId(Long messageId);
    
    List<Attachment> findByOwnerId(Long ownerId);
    
    List<Attachment> findByRealmIdAndIsDeletedFalse(Long realmId);
    
    Optional<Attachment> findByPathId(String pathId);
    
    @Query("SELECT a FROM Attachment a WHERE a.message.id = :messageId AND a.isDeleted = false")
    List<Attachment> findActiveByMessageId(@Param("messageId") Long messageId);
    
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.owner.id = :ownerId AND a.isDeleted = false")
    Long getTotalSizeByOwner(@Param("ownerId") Long ownerId);
    
    void deleteByMessageId(Long messageId);
}