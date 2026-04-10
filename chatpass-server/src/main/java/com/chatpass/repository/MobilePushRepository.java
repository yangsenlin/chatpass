package com.chatpass.repository;

import com.chatpass.entity.MobilePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobilePushRepository extends JpaRepository<MobilePush, Long> {
    
    List<MobilePush> findByRealmId(Long realmId);
    
    Optional<MobilePush> findByRealmIdAndPushType(Long realmId, String pushType);
    
    List<MobilePush> findByRealmIdAndIsActiveTrue(Long realmId);
    
    @Query("SELECT COUNT(mp) FROM MobilePush mp WHERE mp.realmId = :realmId AND mp.isActive = true")
    Long countActiveConfigs(@Param("realmId") Long realmId);
}