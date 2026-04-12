package com.chatpass.controller.api.v1;

import com.chatpass.dto.MessageArchiveDTO;
import com.chatpass.service.MessageArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息归档控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MessageArchiveController {
    
    private final MessageArchiveService archiveService;
    
    /**
     * 归档消息
     */
    @PostMapping("/messages/{messageId}/archive")
    public ResponseEntity<MessageArchiveDTO> archiveMessage(
            @PathVariable Long messageId,
            @RequestParam(required = false, defaultValue = "manual") String archivePolicy,
            @RequestParam(required = false) Long archivedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime recoverUntil) {
        
        MessageArchiveDTO archive = archiveService.archiveMessage(messageId, archivePolicy, archivedBy, recoverUntil);
        return ResponseEntity.status(HttpStatus.CREATED).body(archive);
    }
    
    /**
     * 批量归档消息
     */
    @PostMapping("/realm/{realmId}/archive/batch")
    public ResponseEntity<Integer> batchArchive(
            @PathVariable Long realmId,
            @RequestParam String archivePolicy,
            @RequestParam(defaultValue = "90") int daysThreshold) {
        
        int count = archiveService.archiveMessagesByPolicy(realmId, archivePolicy, daysThreshold);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 恢复归档消息
     */
    @PostMapping("/archives/{archiveId}/recover")
    public ResponseEntity<Void> recoverMessage(@PathVariable Long archiveId) {
        archiveService.recoverMessage(archiveId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取组织的归档消息
     */
    @GetMapping("/realm/{realmId}/archives")
    public ResponseEntity<List<MessageArchiveDTO>> getRealmArchives(@PathVariable Long realmId) {
        List<MessageArchiveDTO> archives = archiveService.getRealmArchives(realmId);
        return ResponseEntity.ok(archives);
    }
    
    /**
     * 获取Stream的归档消息
     */
    @GetMapping("/streams/{streamId}/archives")
    public ResponseEntity<List<MessageArchiveDTO>> getStreamArchives(@PathVariable Long streamId) {
        List<MessageArchiveDTO> archives = archiveService.getStreamArchives(streamId);
        return ResponseEntity.ok(archives);
    }
    
    /**
     * 获取可恢复的归档
     */
    @GetMapping("/realm/{realmId}/archives/recoverable")
    public ResponseEntity<List<MessageArchiveDTO>> getRecoverableArchives(@PathVariable Long realmId) {
        List<MessageArchiveDTO> archives = archiveService.getRecoverableArchives(realmId);
        return ResponseEntity.ok(archives);
    }
    
    /**
     * 获取归档详情
     */
    @GetMapping("/archives/{archiveId}")
    public ResponseEntity<MessageArchiveDTO> getArchive(@PathVariable Long archiveId) {
        return archiveService.getArchiveById(archiveId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 查找原消息的归档
     */
    @GetMapping("/messages/{messageId}/archive")
    public ResponseEntity<MessageArchiveDTO> findByOriginalMessageId(@PathVariable Long messageId) {
        return archiveService.findByOriginalMessageId(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 统计归档数量
     */
    @GetMapping("/realm/{realmId}/archives/count")
    public ResponseEntity<Long> countArchives(@PathVariable Long realmId) {
        long count = archiveService.countArchives(realmId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 清理过期归档
     */
    @PostMapping("/archives/cleanup")
    public ResponseEntity<Integer> cleanupExpiredArchives(
            @RequestParam(defaultValue = "365") int daysThreshold) {
        
        archiveService.cleanupExpiredArchives(daysThreshold);
        return ResponseEntity.ok(0);
    }
}
