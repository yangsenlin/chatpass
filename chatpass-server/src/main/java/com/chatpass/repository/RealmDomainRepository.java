package com.chatpass.repository;

import com.chatpass.entity.RealmDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 组织域名仓库
 */
@Repository
public interface RealmDomainRepository extends JpaRepository<RealmDomain, Long> {
    
    /**
     * 根据组织ID查找所有域名
     */
    List<RealmDomain> findByRealmId(Long realmId);
    
    /**
     * 根据域名查找
     */
    Optional<RealmDomain> findByDomain(String domain);
    
    /**
     * 查找组织的主要域名
     */
    Optional<RealmDomain> findByRealmIdAndIsPrimaryTrue(Long realmId);
    
    /**
     * 检查域名是否已存在
     */
    boolean existsByDomain(String domain);
    
    /**
     * 检查域名是否属于指定组织
     */
    boolean existsByDomainAndRealmId(String domain, Long realmId);
    
    /**
     * 根据状态查找组织的域名
     */
    List<RealmDomain> findByRealmIdAndStatus(Long realmId, String status);
}
