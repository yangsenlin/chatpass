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

@Repository
public interface MessageArchiveRepository extends JpaRepository<MessageArchive, Long> {
    
    Optional<MessageArchive> findByArchiveId(String archiveId);
    
    Optional<MessageArchive> findByMessageId(Long messageId);
    
    List<MessageArchive> findByRealmId(Long realmId);
    
    List<MessageArchive> findByRealmIdAndArchiveReason(Long realmId, String archiveReason);
    
    @Query("SELECT ma FROM MessageArchive ma WHERE ma.realmId = :realmId AND ma.archiveDate BETWEEN :start AND :end")
    List<MessageArchive> findByRealmIdAndTimeRange(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(ma) FROM MessageArchive ma WHERE ma.realmId = :realmId AND ma.isDeleted = false")
    Long countActiveArchives(@Param("realmId") Long realmId);
    
    @Modifying
    @Query("UPDATE MessageArchive ma SET ma.restoreCount = ma.restoreCount + 1, ma.lastRestored = :lastRestored WHERE ma.id = :id")
    void incrementRestoreCount(@Param("id") Long id, @Param("lastRestored") LocalDateTime lastRestored);
    
    @Modifying
    @Query("UPDATE MessageArchive ma SET ma.isDeleted = true WHERE ma.id = :id")
    void markDeleted(@Param("id") Long id);
}