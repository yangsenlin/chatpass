package com.chatpass.repository;

import com.chatpass.entity.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RealmRepository extends JpaRepository<Realm, Long> {

    Optional<Realm> findByStringId(String stringId);
    
    boolean existsByStringId(String stringId);
}