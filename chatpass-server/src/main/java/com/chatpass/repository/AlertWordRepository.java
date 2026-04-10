package com.chatpass.repository;

import com.chatpass.entity.AlertWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AlertWordRepository extends JpaRepository<AlertWord, Long> {
    
    List<AlertWord> findByUserId(Long userId);
    
    List<AlertWord> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<AlertWord> findByUserIdAndWord(Long userId, String word);
    
    @Query("SELECT aw.word FROM AlertWord aw WHERE aw.user.id = :userId AND aw.isActive = true")
    Set<String> findWordsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT aw FROM AlertWord aw WHERE aw.realm.id = :realmId AND aw.isActive = true")
    List<AlertWord> findActiveByRealmId(@Param("realmId") Long realmId);
    
    boolean existsByUserIdAndWord(Long userId, String word);
    
    void deleteByUserIdAndWord(Long userId, String word);
}