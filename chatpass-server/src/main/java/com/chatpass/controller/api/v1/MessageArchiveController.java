package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.ArchiveDTO;
import com.chatpass.entity.MessageArchive;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.MessageArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message Archive 控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Message Archive", description = "消息归档 API")
public class MessageArchiveController {

    private final MessageArchiveService archiveService;
    private final SecurityUtil securityUtil;

    @PostMapping("/archives")
    @Operation(summary = "归档消息")
    public ResponseEntity<ApiResponse<ArchiveDTO.ArchiveResponse>> archiveMessage(
            @RequestBody ArchiveDTO.ArchiveRequest request) {
        Long realmId = securityUtil.getCurrentRealmId();

        LocalDateTime expireDate = request.getExpireDate() != null ?
                LocalDateTime.parse(request.getExpireDate()) : LocalDateTime.now().plusDays(30);

        MessageArchive archive = archiveService.archiveMessage(
                realmId, request.getMessageId(), request.getOriginalContent(),
                request.getStorageType(), request.getArchiveReason(), expireDate);

        return ResponseEntity.ok(ApiResponse.success(toArchiveResponse(archive)));
    }

    @GetMapping("/archives")
    @Operation(summary = "获取归档列表")
    public ResponseEntity<ApiResponse<List<ArchiveDTO.ArchiveResponse>>> getArchives() {
        Long realmId = securityUtil.getCurrentRealmId();

        List<MessageArchive> archives = archiveService.getRealmArchivesList(realmId);

        List<ArchiveDTO.ArchiveResponse> response = archives.stream()
                .map(this::toArchiveResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/archives/{archiveId}")
    @Operation(summary = "获取归档详情")
    public ResponseEntity<ApiResponse<ArchiveDTO.ArchiveResponse>> getArchive(
            @PathVariable Long archiveId) {
        MessageArchive archive = archiveService.getArchive(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("归档不存在"));

        return ResponseEntity.ok(ApiResponse.success(toArchiveResponse(archive)));
    }

    @GetMapping("/archives/message/{messageId}")
    @Operation(summary = "获取消息归档")
    public ResponseEntity<ApiResponse<ArchiveDTO.ArchiveResponse>> getMessageArchive(
            @PathVariable Long messageId) {
        MessageArchive archive = archiveService.getMessageArchive(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息未归档"));

        return ResponseEntity.ok(ApiResponse.success(toArchiveResponse(archive)));
    }

    @PostMapping("/archives/{archiveId}/restore")
    @Operation(summary = "恢复归档消息")
    public ResponseEntity<ApiResponse<ArchiveDTO.RestoreResponse>> restoreArchive(
            @PathVariable Long archiveId) {
        MessageArchive archive = archiveService.restoreArchive(archiveId);

        ArchiveDTO.RestoreResponse response = ArchiveDTO.RestoreResponse.builder()
                .archiveId(archive.getArchiveId())
                .messageId(archive.getMessageId())
                .originalContent(archive.getOriginalContent())
                .restoreCount(archive.getRestoreCount())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/archives/{archiveId}")
    @Operation(summary = "删除归档")
    public ResponseEntity<ApiResponse<Void>> deleteArchive(@PathVariable Long archiveId) {
        archiveService.deleteArchive(archiveId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/archives/reason/{reason}")
    @Operation(summary = "按原因获取归档")
    public ResponseEntity<ApiResponse<List<ArchiveDTO.ArchiveResponse>>> getArchivesByReason(
            @PathVariable String reason) {
        Long realmId = securityUtil.getCurrentRealmId();

        List<MessageArchive> archives = archiveService.getArchivesByReason(realmId, reason);

        List<ArchiveDTO.ArchiveResponse> response = archives.stream()
                .map(this::toArchiveResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/archives/cleanup")
    @Operation(summary = "清理过期归档")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredArchives() {
        Long realmId = securityUtil.getCurrentRealmId();

        int cleaned = archiveService.cleanupExpiredArchives(realmId);

        return ResponseEntity.ok(ApiResponse.success(cleaned));
    }

    @GetMapping("/archives/count")
    @Operation(summary = "统计归档数")
    public ResponseEntity<ApiResponse<Long>> countArchives() {
        Long realmId = securityUtil.getCurrentRealmId();

        Long count = archiveService.countActiveArchives(realmId);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private ArchiveDTO.ArchiveResponse toArchiveResponse(MessageArchive archive) {
        return ArchiveDTO.ArchiveResponse.builder()
                .id(archive.getId())
                .realmId(archive.getRealmId())
                .archiveId(archive.getArchiveId())
                .messageId(archive.getMessageId())
                .storageType(archive.getStorageType())
                .archiveReason(archive.getArchiveReason())
                .archiveDate(archive.getArchiveDate() != null ? archive.getArchiveDate().toString() : null)
                .expireDate(archive.getExpireDate() != null ? archive.getExpireDate().toString() : null)
                .isDeleted(archive.getIsDeleted())
                .restoreCount(archive.getRestoreCount())
                .lastRestored(archive.getLastRestored() != null ? archive.getLastRestored().toString() : null)
                .build();
    }
}