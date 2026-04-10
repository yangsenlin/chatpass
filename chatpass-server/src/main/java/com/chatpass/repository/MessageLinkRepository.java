package com.chatpass.repository;

import com.chatpass.entity.MessageLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageLinkRepository extends JpaRepository<MessageLink, Long> {
    
    List<MessageLink> findByMessageId(Long messageId);
    
    List<MessageLink> findByTargetMessageId(Long targetMessageId);
    
    @Query("SELECT ml FROM MessageLink ml WHERE ml.message.id = :messageId AND ml.linkType = :linkType")
    List<MessageLink> findByMessageIdAndLinkType(@Param("messageId") Long messageId, @Param("linkType") String linkType);
    
    @Query("SELECT ml FROM MessageLink ml WHERE ml.targetMessage.id = :messageId ORDER BY ml.dateCreated DESC")
    List<MessageLink> findReferencesToMessage(@Param("messageId") Long messageId);
    
    boolean existsByMessageIdAndTargetMessageId(Long messageId, Long targetMessageId);
}