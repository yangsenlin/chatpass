package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.NarrowDTO;
import com.chatpass.service.NarrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Narrow 查询控制器
 * 
 * Zulip 核心消息过滤 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Narrow", description = "消息过滤查询 API")
public class NarrowController {

    private final NarrowService narrowService;

    @PostMapping("/messages/query")
    @Operation(summary = "Narrow 查询", description = "按过滤条件查询消息")
    public ResponseEntity<ApiResponse<NarrowDTO.Response>> queryMessages(
            @RequestBody NarrowDTO.Request request) {
        
        // TODO: 从 SecurityContext 获取用户信息
        Long userId = 1L;
        Long realmId = 1L;
        
        NarrowDTO.Response response = narrowService.query(realmId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages")
    @Operation(summary = "简单消息查询", description = "使用 GET 参数进行 Narrow 查询")
    public ResponseEntity<ApiResponse<NarrowDTO.Response>> getMessages(
            @RequestParam(required = false) Long anchor,
            @RequestParam(required = false, defaultValue = "50") Integer numBefore,
            @RequestParam(required = false, defaultValue = "50") Integer numAfter,
            @RequestParam(required = false) String narrow) {
        
        // TODO: 解析 narrow 字符串为 Filter 列表
        // 格式: [{"operator":"stream","operand":"general"},{"operator":"topic","operand":"Kubernetes"}]
        
        NarrowDTO.Request request = NarrowDTO.Request.builder()
                .anchor(anchor)
                .numBefore(numBefore)
                .numAfter(numAfter)
                .build();
        
        Long userId = 1L;
        Long realmId = 1L;
        
        NarrowDTO.Response response = narrowService.query(realmId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}