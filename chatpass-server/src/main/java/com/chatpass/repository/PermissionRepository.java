package com.chatpass.repository;

import com.chatpass.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);
    
    List<Permission> findByCategory(String category);
    
    @Query("SELECT p FROM Permission p ORDER BY p.category, p.name")
    List<Permission> findAllOrderByCategoryAndName();
    
    boolean existsByCode(String code);
}