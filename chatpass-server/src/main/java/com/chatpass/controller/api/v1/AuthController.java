package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.AuthDTO;
import com.chatpass.dto.UserDTO;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "认证 API")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    @PostMapping("/auth/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponse>> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        
        AuthDTO.TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponse>> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request,
            @RequestParam(defaultValue = "1") Long realmId) {
        
        AuthDTO.TokenResponse response = authService.register(request, realmId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/fetch_api_key")
    @Operation(summary = "获取 API Key")
    public ResponseEntity<ApiResponse<AuthDTO.ApiKeyResponse>> getApiKey(
            @RequestBody AuthDTO.ApiKeyRequest request) {
        
        AuthDTO.ApiKeyResponse response = authService.getApiKey(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auth/api_key_login")
    @Operation(summary = "API Key 登录")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponse>> loginWithApiKey(
            @RequestParam String api_key) {
        
        AuthDTO.TokenResponse response = authService.loginWithApiKey(api_key);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auth/change_password")
    @Operation(summary = "修改密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody AuthDTO.ChangePasswordRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/me")
    @Operation(summary = "获取当前用户信息")
    public ResponseEntity<ApiResponse<UserDTO.ProfileResponse>> getCurrentUser() {
        var user = securityUtil.getCurrentUser();
        
        UserDTO.ProfileResponse response = UserDTO.ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .shortName(user.getShortName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .timezone(user.getTimezone())
                .defaultLanguage(user.getDefaultLanguage())
                .isActive(user.getIsActive())
                .isBot(user.getBotType() != null)
                .isBillingAdmin(user.getIsBillingAdmin())
                .dateJoined(user.getDateJoined())
                .lastLogin(user.getLastLogin())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}