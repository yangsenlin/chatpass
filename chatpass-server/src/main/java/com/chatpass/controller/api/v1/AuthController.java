package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.AuthDTO;
import com.chatpass.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * Zulip API 风格认证端点
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "认证 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/fetch_api_key")
    @Operation(summary = "登录获取 API Key", description = "使用邮箱和密码获取 API Key")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> fetchApiKey(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    @Operation(summary = "注册新用户", description = "创建新用户和组织")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        AuthDTO.AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/api_key/regenerate")
    @Operation(summary = "重新生成 API Key")
    public ResponseEntity<ApiResponse<String>> regenerateApiKey() {
        // TODO: 从 SecurityContext 获取用户 ID
        String newApiKey = authService.regenerateApiKey(1L);
        return ResponseEntity.ok(ApiResponse.success("API key regenerated", newApiKey));
    }
}