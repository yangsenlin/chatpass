package com.chatpass.service;

import com.chatpass.dto.RealmDomainDTO;
import com.chatpass.entity.RealmDomain;
import com.chatpass.repository.RealmDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 组织域名服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealmDomainService {
    
    private final RealmDomainRepository domainRepository;
    
    /**
     * 添加域名
     */
    @Transactional
    public RealmDomainDTO addDomain(Long realmId, String domain, Boolean allowSubdomains) {
        // 检查域名是否已存在
        if (domainRepository.existsByDomain(domain)) {
            throw new IllegalArgumentException("域名已被使用: " + domain);
        }
        
        // 验证域名格式
        if (!isValidDomain(domain)) {
            throw new IllegalArgumentException("域名格式无效: " + domain);
        }
        
        RealmDomain realmDomain = new RealmDomain();
        realmDomain.setDomain(domain);
        realmDomain.setRealmId(realmId);
        realmDomain.setIsPrimary(false);
        realmDomain.setAllowSubdomains(allowSubdomains != null ? allowSubdomains : false);
        realmDomain.setStatus("pending");
        
        realmDomain = domainRepository.save(realmDomain);
        log.info("添加组织域名: {} (realmId: {})", domain, realmId);
        
        return toDTO(realmDomain);
    }
    
    /**
     * 获取组织的所有域名
     */
    public List<RealmDomainDTO> getDomainsByRealm(Long realmId) {
        return domainRepository.findByRealmId(realmId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取域名详情
     */
    public Optional<RealmDomainDTO> getDomainById(Long domainId) {
        return domainRepository.findById(domainId).map(this::toDTO);
    }
    
    /**
     * 验证域名
     */
    @Transactional
    public RealmDomainDTO verifyDomain(Long domainId) {
        RealmDomain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new IllegalArgumentException("域名不存在: " + domainId));
        
        // 这里应该实际验证域名所有权（例如检查DNS记录）
        // 为了演示，直接标记为已验证
        
        domain.setStatus("verified");
        domain.setVerifiedAt(LocalDateTime.now());
        
        domain = domainRepository.save(domain);
        log.info("验证域名: {}", domain.getDomain());
        
        return toDTO(domain);
    }
    
    /**
     * 设置主要域名
     */
    @Transactional
    public void setPrimaryDomain(Long realmId, Long domainId) {
        // 取消原主要域名
        domainRepository.findByRealmIdAndIsPrimaryTrue(realmId)
                .ifPresent(d -> {
                    d.setIsPrimary(false);
                    domainRepository.save(d);
                });
        
        // 设置新主要域名
        RealmDomain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new IllegalArgumentException("域名不存在: " + domainId));
        
        if (!"verified".equals(domain.getStatus())) {
            throw new IllegalStateException("只有已验证的域名才能设为主要域名");
        }
        
        domain.setIsPrimary(true);
        domainRepository.save(domain);
        log.info("设置主要域名: {} (realmId: {})", domain.getDomain(), realmId);
    }
    
    /**
     * 删除域名
     */
    @Transactional
    public void deleteDomain(Long domainId) {
        RealmDomain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new IllegalArgumentException("域名不存在: " + domainId));
        
        if (domain.getIsPrimary()) {
            throw new IllegalStateException("不能删除主要域名");
        }
        
        domainRepository.delete(domain);
        log.info("删除域名: {}", domain.getDomain());
    }
    
    /**
     * 获取组织的主要域名
     */
    public Optional<RealmDomainDTO> getPrimaryDomain(Long realmId) {
        return domainRepository.findByRealmIdAndIsPrimaryTrue(realmId)
                .map(this::toDTO);
    }
    
    /**
     * 检查域名是否可用
     */
    public boolean isDomainAvailable(String domain) {
        return !domainRepository.existsByDomain(domain);
    }
    
    /**
     * 验证域名格式
     */
    private boolean isValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        
        // 基本格式验证
        String pattern = "^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";
        return domain.matches(pattern);
    }
    
    private RealmDomainDTO toDTO(RealmDomain domain) {
        return RealmDomainDTO.builder()
                .id(domain.getId())
                .domain(domain.getDomain())
                .realmId(domain.getRealmId())
                .isPrimary(domain.getIsPrimary())
                .allowSubdomains(domain.getAllowSubdomains())
                .status(domain.getStatus())
                .verifiedAt(domain.getVerifiedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
