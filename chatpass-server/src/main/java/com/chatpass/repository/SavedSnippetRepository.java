package com.chatpass.repository;

import com.chatpass.entity.SavedSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SavedSnippet Repository
 */
@Repository
public interface SavedSnippetRepository extends JpaRepository<SavedSnippet, Long> {
    
    /**
     * 获取用户的所有保存片段
     */
    List<SavedSnippet> findByUserIdOrderByDateCreatedDesc(Long userId);
    
    /**
     * 获取组织中所有用户的片段（管理员用）
     */
    List<SavedSnippet> findByRealmIdOrderByDateCreatedDesc(Long realmId);
    
    /**
     * 搜索用户的片段
     */
    @Query("SELECT s FROM SavedSnippet s WHERE s.user.id = :userId AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<SavedSnippet> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 获取用户的片段数量
     */
    long countByUserId(Long userId);
    
    /**
     * 检查标题是否存在
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SavedSnippet s WHERE s.user.id = :userId AND s.title = :title")
    boolean existsByUserIdAndTitle(@Param("userId") Long userId, @Param("title") String title);
    
    /**
     * 删除用户的所有片段
     */
    void deleteByUserId(Long userId);
}