package com.chatpass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.chatpass.entity.Message;
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
    
    // Narrow 查询支持
    Page<Message> findByRecipientIdInOrderByDateSentDesc(List<Long> recipientIds, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.realm.id = :realmId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.dateSent DESC")
    List<Message> searchByContent(@Param("realmId") Long realmId, @Param("query") String query);
    
    // Topic 查询支持
    @Query("SELECT m FROM Message m WHERE m.recipient.stream.id = :streamId AND m.subject = :topic ORDER BY m.dateSent")
    List<Message> findByStreamIdAndTopic(@Param("streamId") Long streamId, @Param("topic") String topic);
    
    @Query("SELECT m FROM Message m WHERE m.recipient.stream.id = :streamId ORDER BY m.dateSent")
    List<Message> findByStreamId(@Param("streamId") Long streamId);
    
    // 搜索增强支持
    @Query("SELECT m FROM Message m WHERE m.realm.id = :realmId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.dateSent DESC")
    Page<Message> searchByContentPaged(@Param("realmId") Long realmId, @Param("query") String query, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.recipient.stream.id = :streamId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.dateSent DESC")
    List<Message> searchByStreamIdAndContent(@Param("streamId") Long streamId, @Param("query") String query);
    
    @Query("SELECT m FROM Message m WHERE m.recipient.stream.id = :streamId AND m.subject = :topic AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.dateSent DESC")
    List<Message> searchByStreamIdTopicAndContent(@Param("streamId") Long streamId, @Param("topic") String topic, @Param("query") String query);
    
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.dateSent DESC")
    List<Message> searchBySenderIdAndContent(@Param("senderId") Long senderId, @Param("query") String query);
    
    // Analytics statistics
    
    /**
     * 统计时间范围内消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end")
    Long countByRealmIdAndTimeRange(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 平均消息长度
     */
    @Query("SELECT AVG(LENGTH(m.content)) FROM Message m WHERE m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end")
    Double avgMessageLength(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 统计 Stream 消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.streamId = :streamId AND m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end")
    Long countStreamMessages(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 统计私信数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.streamId IS NULL AND m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end")
    Long countPrivateMessages(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 小时分布统计
     */
    @Query("SELECT HOUR(m.dateSent), COUNT(m) FROM Message m WHERE m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end GROUP BY HOUR(m.dateSent) ORDER BY HOUR(m.dateSent)")
    List<Object[]> hourlyDistribution(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 发送者统计
     */
    @Query("SELECT m.sender.id, COUNT(m) FROM Message m WHERE m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end GROUP BY m.sender.id ORDER BY COUNT(m) DESC")
    List<Object[]> findTopSenders(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("limit") int limit);
    
    /**
     * Stream 统计
     */
    @Query("SELECT m.recipient.streamId, COUNT(m) FROM Message m WHERE m.realm.id = :realmId AND m.dateSent BETWEEN :start AND :end GROUP BY m.recipient.streamId ORDER BY COUNT(m) DESC")
    List<Object[]> findTopStreams(@Param("realmId") Long realmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("limit") int limit);
}