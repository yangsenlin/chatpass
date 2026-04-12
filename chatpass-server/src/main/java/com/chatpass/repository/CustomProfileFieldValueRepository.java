package com.chatpass.repository;

import com.chatpass.entity.CustomProfileFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomProfileFieldValueRepository extends JpaRepository<CustomProfileFieldValue, Long> {

    /**
     * 获取用户的所有字段值
     */
    @Query("SELECT v FROM CustomProfileFieldValue v WHERE v.userProfile.id = :userId")
    List<CustomProfileFieldValue> findByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的特定字段值
     */
    @Query("SELECT v FROM CustomProfileFieldValue v WHERE v.userProfile.id = :userId AND v.field.id = :fieldId")
    Optional<CustomProfileFieldValue> findByUserIdAndFieldId(@Param("userId") Long userId, @Param("fieldId") Long fieldId);

    /**
     * 获取用户的字段值（包含字段信息）
     */
    @Query("SELECT v FROM CustomProfileFieldValue v JOIN FETCH v.field WHERE v.userProfile.id = :userId")
    List<CustomProfileFieldValue> findByUserIdWithField(@Param("userId") Long userId);

    /**
     * 获取字段的所有值
     */
    @Query("SELECT v FROM CustomProfileFieldValue v WHERE v.field.id = :fieldId")
    List<CustomProfileFieldValue> findByFieldId(@Param("fieldId") Long fieldId);

    /**
     * 按值搜索用户
     */
    @Query("SELECT v FROM CustomProfileFieldValue v WHERE v.field.id = :fieldId AND LOWER(v.value) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CustomProfileFieldValue> searchByFieldIdAndValue(@Param("fieldId") Long fieldId, @Param("query") String query);

    /**
     * 删除用户的字段值
     */
    @Modifying
    @Query("DELETE FROM CustomProfileFieldValue v WHERE v.userProfile.id = :userId AND v.field.id = :fieldId")
    void deleteByUserIdAndFieldId(@Param("userId") Long userId, @Param("fieldId") Long fieldId);
}