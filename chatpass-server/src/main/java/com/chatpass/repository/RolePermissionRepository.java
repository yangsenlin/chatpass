package com.chatpass.repository;

import com.chatpass.entity.Permission;
import com.chatpass.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Integer role);
    
    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.role = :role")
    List<Permission> findPermissionsByRole(@Param("role") Integer role);
    
    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.role >= :minRole")
    List<Permission> findPermissionsByRoleGreaterThanEqual(@Param("minRole") Integer minRole);
    
    boolean existsByRoleAndPermission(Integer role, Permission permission);
    
    void deleteByRoleAndPermission(Integer role, Permission permission);
}