package com.chatpass.repository;

import com.chatpass.entity.Realm;
import com.chatpass.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findByRealm(Realm realm);
    
    List<UserGroup> findByRealmId(Long realmId);
    
    Optional<UserGroup> findByRealmAndName(Realm realm, String name);
    
    Optional<UserGroup> findByRealmIdAndName(Long realmId, String name);
    
    List<UserGroup> findByRealmIdAndIsSystemTrue(Long realmId);
    
    List<UserGroup> findByRealmIdAndIsSystemFalse(Long realmId);
    
    @Query("SELECT g FROM UserGroup g WHERE g.realm.id = :realmId ORDER BY g.name")
    List<UserGroup> findByRealmIdOrderByName(@Param("realmId") Long realmId);
    
    boolean existsByRealmIdAndName(Long realmId, String name);
    
    @Query("SELECT COUNT(g) FROM UserGroup g WHERE g.realm.id = :realmId")
    Long countByRealmId(@Param("realmId") Long realmId);
}