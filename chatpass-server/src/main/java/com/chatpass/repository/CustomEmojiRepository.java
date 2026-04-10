package com.chatpass.repository;

import com.chatpass.entity.CustomEmoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CustomEmojiRepository
 */
@Repository
public interface CustomEmojiRepository extends JpaRepository<CustomEmoji, Long> {

    /**
     * 查找 Realm 的所有活跃表情
     */
    @Query("SELECT e FROM CustomEmoji e WHERE e.realm.id = :realmId AND e.deactivated = false ORDER BY e.name")
    List<CustomEmoji> findByRealmIdAndActive(@Param("realmId") Long realmId);

    /**
     * 查找 Realm 的所有表情（包含已删除）
     */
    List<CustomEmoji> findByRealmIdOrderByDateCreated(@Param("realmId") Long realmId);

    /**
     * 根据名称查找表情
     */
    @Query("SELECT e FROM CustomEmoji e WHERE e.realm.id = :realmId AND e.name = :name AND e.deactivated = false")
    Optional<CustomEmoji> findByRealmIdAndName(@Param("realmId") Long realmId, @Param("name") String name);

    /**
     * 检查表情名称是否已存在
     */
    @Query("SELECT COUNT(e) > 0 FROM CustomEmoji e WHERE e.realm.id = :realmId AND e.name = :name AND e.deactivated = false")
    boolean existsByRealmIdAndName(@Param("realmId") Long realmId, @Param("name") String name);

    /**
     * 查找用户创建的表情
     */
    @Query("SELECT e FROM CustomEmoji e WHERE e.author.id = :authorId AND e.deactivated = false ORDER BY e.dateCreated DESC")
    List<CustomEmoji> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * 搜索表情（名称模糊匹配）
     */
    @Query("SELECT e FROM CustomEmoji e WHERE e.realm.id = :realmId AND LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) AND e.deactivated = false")
    List<CustomEmoji> searchByName(@Param("realmId") Long realmId, @Param("query") String query);

    /**
     * 统计 Realm 的表情数量
     */
    @Query("SELECT COUNT(e) FROM CustomEmoji e WHERE e.realm.id = :realmId AND e.deactivated = false")
    Long countByRealmIdAndActive(@Param("realmId") Long realmId);
}