package com.chatpass.repository;

import com.chatpass.entity.UserGroup;
import com.chatpass.entity.UserGroupMembership;
import com.chatpass.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupMembershipRepository extends JpaRepository<UserGroupMembership, Long> {

    List<UserGroupMembership> findByGroup(UserGroup group);
    
    List<UserGroupMembership> findByGroupId(Long groupId);
    
    List<UserGroupMembership> findByUser(UserProfile user);
    
    List<UserGroupMembership> findByUserId(Long userId);
    
    Optional<UserGroupMembership> findByGroupAndUser(UserGroup group, UserProfile user);
    
    @Query("SELECT m FROM UserGroupMembership m WHERE m.group.id = :groupId ORDER BY m.joinedAt")
    List<UserGroupMembership> findByGroupIdOrderByJoinedAt(@Param("groupId") Long groupId);
    
    @Query("SELECT m.user FROM UserGroupMembership m WHERE m.group.id = :groupId")
    List<UserProfile> findUsersByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT m.group FROM UserGroupMembership m WHERE m.user.id = :userId")
    List<UserGroup> findGroupsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM UserGroupMembership m WHERE m.group.id = :groupId")
    Long countByGroupId(@Param("groupId") Long groupId);
    
    boolean existsByGroupAndUser(UserGroup group, UserProfile user);
    
    void deleteByGroupAndUser(UserGroup group, UserProfile user);
    
    void deleteByGroupId(Long groupId);
}