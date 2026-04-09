package com.chatpass.repository;

import com.chatpass.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByEmail(String email);
    
    Optional<UserProfile> findByApiKey(String apiKey);
    
    Optional<UserProfile> findByRealmIdAndEmail(Long realmId, String email);
    
    List<UserProfile> findByRealmId(Long realmId);
    
    List<UserProfile> findByRealmIdAndIsActiveTrue(Long realmId);
    
    @Query("SELECT u FROM UserProfile u WHERE u.realm.id = :realmId AND u.role >= :minRole")
    List<UserProfile> findByRealmIdAndRoleGreaterThanEqual(@Param("realmId") Long realmId, @Param("minRole") Integer minRole);
    
    @Query("SELECT COUNT(u) FROM UserProfile u WHERE u.realm.id = :realmId AND u.isActive = true")
    Long countActiveUsersByRealmId(@Param("realmId") Long realmId);
    
    boolean existsByRealmIdAndEmail(Long realmId, String email);
}