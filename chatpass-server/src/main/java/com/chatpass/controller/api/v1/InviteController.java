package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.InviteDTO;
import com.chatpass.entity.RealmInvite;
import com.chatpass.entity.UserProfile;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.InviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Invite 控制器
 * 
 * 邀请系统 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Invites", description = "邀请系统 API")
public class InviteController {

    private final InviteService inviteService;
    private final SecurityUtil securityUtil;

    @PostMapping("/invites")
    @Operation(summary = "创建邀请链接")
    public ResponseEntity<ApiResponse<InviteDTO.Response>> createInvite(@RequestBody InviteDTO.CreateRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Long realmId = securityUtil.getCurrentRealmId();
        
        RealmInvite invite = inviteService.createInvite(
                realmId, 
                userId, 
                request.getEmail(), 
                request.getMaxUses(), 
                request.getExpireDays());
        
        return ResponseEntity.ok(ApiResponse.success(toResponse(invite)));
    }

    @GetMapping("/invites")
    @Operation(summary = "获取邀请列表")
    public ResponseEntity<ApiResponse<InviteDTO.ListResponse>> getInvites() {
        Long realmId = securityUtil.getCurrentRealmId();
        List<RealmInvite> invites = inviteService.getRealmInvites(realmId);
        
        List<InviteDTO.Response> responses = invites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(InviteDTO.ListResponse.builder()
                .invites(responses)
                .count(responses.size())
                .build()));
    }

    @DeleteMapping("/invites/{id}")
    @Operation(summary = "撤销邀请")
    public ResponseEntity<ApiResponse<Void>> revokeInvite(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        inviteService.revokeInvite(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/invites/accept")
    @Operation(summary = "接受邀请注册")
    public ResponseEntity<ApiResponse<UserProfile>> acceptInvite(@RequestBody InviteDTO.AcceptRequest request) {
        UserProfile user = inviteService.acceptInvite(
                request.getInviteLink(),
                request.getEmail(),
                request.getFullName(),
                request.getPassword());
        
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/invites/{link}/validate")
    @Operation(summary = "验证邀请链接")
    public ResponseEntity<ApiResponse<Boolean>> validateInvite(@PathVariable String link) {
        boolean valid = inviteService.isValidInvite(link);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }

    @GetMapping("/invites/{link}")
    @Operation(summary = "获取邀请详情")
    public ResponseEntity<ApiResponse<InviteDTO.Response>> getInvite(@PathVariable String link) {
        RealmInvite invite = inviteService.getInviteByLink(link);
        return ResponseEntity.ok(ApiResponse.success(toResponse(invite)));
    }

    private InviteDTO.Response toResponse(RealmInvite invite) {
        String statusText;
        switch (invite.getStatus()) {
            case RealmInvite.STATUS_PENDING: statusText = "待使用"; break;
            case RealmInvite.STATUS_ACCEPTED: statusText = "已使用"; break;
            case RealmInvite.STATUS_EXPIRED: statusText = "已过期"; break;
            case RealmInvite.STATUS_REVOKED: statusText = "已撤销"; break;
            default: statusText = "未知";
        }

        return InviteDTO.Response.builder()
                .id(invite.getId())
                .inviteLink(invite.getInviteLink())
                .email(invite.getEmail())
                .status(invite.getStatus())
                .statusText(statusText)
                .maxUses(invite.getMaxUses())
                .currentUses(invite.getCurrentUses())
                .expiresAt(invite.getExpiresAt() != null ? invite.getExpiresAt().toString() : null)
                .createdAt(invite.getDateCreated().toString())
                .build();
    }
}