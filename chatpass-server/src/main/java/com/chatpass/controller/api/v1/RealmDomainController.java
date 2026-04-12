package com.chatpass.controller.api.v1;

import com.chatpass.dto.RealmDomainDTO;
import com.chatpass.service.RealmDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织域名控制器
 */
@RestController
@RequestMapping("/api/v1/realm/{realmId}/domains")
@RequiredArgsConstructor
@Slf4j
public class RealmDomainController {
    
    private final RealmDomainService domainService;
    
    /**
     * 添加域名
     */
    @PostMapping
    public ResponseEntity<RealmDomainDTO> addDomain(
            @PathVariable Long realmId,
            @RequestParam String domain,
            @RequestParam(required = false) Boolean allowSubdomains) {
        
        RealmDomainDTO realmDomain = domainService.addDomain(realmId, domain, allowSubdomains);
        return ResponseEntity.status(HttpStatus.CREATED).body(realmDomain);
    }
    
    /**
     * 获取所有域名
     */
    @GetMapping
    public ResponseEntity<List<RealmDomainDTO>> getDomains(@PathVariable Long realmId) {
        List<RealmDomainDTO> domains = domainService.getDomainsByRealm(realmId);
        return ResponseEntity.ok(domains);
    }
    
    /**
     * 获取域名详情
     */
    @GetMapping("/{domainId}")
    public ResponseEntity<RealmDomainDTO> getDomain(@PathVariable Long domainId) {
        return domainService.getDomainById(domainId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 验证域名
     */
    @PostMapping("/{domainId}/verify")
    public ResponseEntity<RealmDomainDTO> verifyDomain(@PathVariable Long domainId) {
        RealmDomainDTO domain = domainService.verifyDomain(domainId);
        return ResponseEntity.ok(domain);
    }
    
    /**
     * 设置主要域名
     */
    @PostMapping("/{domainId}/primary")
    public ResponseEntity<Void> setPrimaryDomain(
            @PathVariable Long realmId,
            @PathVariable Long domainId) {
        
        domainService.setPrimaryDomain(realmId, domainId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除域名
     */
    @DeleteMapping("/{domainId}")
    public ResponseEntity<Void> deleteDomain(@PathVariable Long domainId) {
        domainService.deleteDomain(domainId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取主要域名
     */
    @GetMapping("/primary")
    public ResponseEntity<RealmDomainDTO> getPrimaryDomain(@PathVariable Long realmId) {
        return domainService.getPrimaryDomain(realmId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 检查域名是否可用
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkDomainAvailability(@RequestParam String domain) {
        boolean available = domainService.isDomainAvailable(domain);
        return ResponseEntity.ok(available);
    }
}
