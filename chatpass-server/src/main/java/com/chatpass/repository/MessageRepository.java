package com.chatpass.repository;

import com.chatpass.entity.Message;
import com.chatpass.entity.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByRecipientIdOrderByDateSentAsc(Long recipientId);
    
    Page<Message> findByRecipientIdOrderByDateSentDesc(Long recipientId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :recipientId AND m.subject = :subject ORDER BY m.dateSent ASC")
    List<Message> findByRecipientIdAndSubjectOrderByDateSentAsc(@Param("recipientId") Long recipientId, @Param("subject") String subject);
    
    @Query("SELECT m FROM Message m WHERE m.realm.id = :realmId ORDER BY m.dateSent DESC")
    Page<Message> findByRealmIdOrderByDateSentDesc(@Param("realmId") Long realmId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId ORDER BY m.dateSent DESC")
    List<Message> findBySenderIdOrderByDateSentDesc(@Param("senderId") Long senderId);
    
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :recipientId AND m.dateSent >= :since ORDER BY m.dateSent ASC")
    List<Message> findByRecipientIdAndDateSentAfter(@Param("recipientId") Long recipientId, @Param("since") LocalDateTime since);
    
    @Query("SELECT DISTINCT m.subject FROM Message m WHERE m.recipient.id = :recipientId AND m.subject IS NOT NULL AND m.subject != '' ORDER BY m.subject")
    List<String> findDistinctSubjectsByRecipientId(@Param("recipientId") Long recipientId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.realm.id = :realmId")
    Long countByRealmId(@Param("realmId") Long realmId);
}