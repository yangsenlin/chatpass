package com.chatpass.repository;

import com.chatpass.entity.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    
    List<Stream> findByRealmId(Long realmId);
    
    List<Stream> findByRealmIdAndDeactivatedFalse(Long realmId);
    
    Optional<Stream> findByRealmIdAndId(Long realmId, Long id);
    
    Optional<Stream> findByRealmIdAndName(Long realmId, String name);
    
    @Query("SELECT s FROM Stream s WHERE s.realm.id = :realmId AND s.inviteOnly = false AND s.deactivated = false")
    List<Stream> findPublicStreamsByRealmId(@Param("realmId") Long realmId);
    
    @Query("SELECT s FROM Stream s WHERE s.realm.id = :realmId AND s.isWebPublic = true AND s.deactivated = false")
    List<Stream> findWebPublicStreamsByRealmId(@Param("realmId") Long realmId);
    
    boolean existsByRealmIdAndName(Long realmId, String name);
}