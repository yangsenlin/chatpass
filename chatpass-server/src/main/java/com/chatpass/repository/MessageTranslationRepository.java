package com.chatpass.repository;

import com.chatpass.entity.MessageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTranslationRepository extends JpaRepository<MessageTranslation, Long> {
    
    List<MessageTranslation> findByMessageId(Long messageId);
    
    Optional<MessageTranslation> findByMessageIdAndTargetLanguage(Long messageId, String targetLanguage);
    
    List<MessageTranslation> findByTranslationStatus(String status);
    
    List<MessageTranslation> findByTranslatorId(Long translatorId);
    
    @Query("SELECT mt FROM MessageTranslation mt WHERE mt.messageId = :messageId AND mt.translationStatus = 'COMPLETED'")
    List<MessageTranslation> findCompletedTranslations(@Param("messageId") Long messageId);
    
    @Query("SELECT COUNT(mt) FROM MessageTranslation mt WHERE mt.translationProvider = :provider")
    Long countByProvider(@Param("provider") String provider);
    
    @Query("SELECT mt.targetLanguage, COUNT(mt) FROM MessageTranslation mt WHERE mt.translationStatus = 'COMPLETED' GROUP BY mt.targetLanguage")
    List<Object[]> countByTargetLanguage();
}