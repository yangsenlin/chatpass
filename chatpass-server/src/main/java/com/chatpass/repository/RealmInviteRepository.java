package com.chatpass.repository;

import com.chatpass.entity.RealmInvite;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealmInviteRepository extends JpaRepository<RealmInvite, Long> {

    Optional<RealmInvite> findByInviteLink(String inviteLink);
    
    List<RealmInvite> findByRealmOrderByDateCreatedDesc(Realm realm);
    
    List<RealmInvite> findByInvitedByUserOrderByDateCreatedDesc(UserProfile user);
    
    @Query("SELECT i FROM RealmInvite i WHERE i.realm = :realm AND i.status = :status")
    List<RealmInvite> findByRealmAndStatus(@Param("realm") Realm realm, @Param("status") Integer status);
    
    @Query("SELECT i FROM RealmInvite i WHERE i.inviteLink = :link AND i.status = 1 AND (i.expiresAt IS NULL OR i.expiresAt > :now)")
    Optional<RealmInvite> findValidInvite(@Param("link") String link, @Param("now") java.time.LocalDateTime now);
}