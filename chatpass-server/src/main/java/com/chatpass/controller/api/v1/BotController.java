package com.chatpass.controller.api.v1;

import com.chatpass.dto.ApiResponse;
import com.chatpass.dto.BotDTO;
import com.chatpass.entity.Bot;
import com.chatpass.entity.BotCommand;
import com.chatpass.security.SecurityUtil;
import com.chatpass.service.BotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bot 控制器
 * 
 * 机器人管理 API
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Bots", description = "机器人 API")
public class BotController {

    private final BotService botService;
    private final SecurityUtil securityUtil;

    @PostMapping("/bots")
    @Operation(summary = "创建 Bot")
    public ResponseEntity<ApiResponse<BotDTO.BotResponse>> createBot(
            @RequestBody BotDTO.CreateBotRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        Bot bot = botService.createBot(userId, request.getName(), 
                request.getBotType(), request.getDescription());
        
        return ResponseEntity.ok(ApiResponse.success(botService.toResponse(bot)));
    }

    @GetMapping("/bots/{botId}")
    @Operation(summary = "获取 Bot 详情")
    public ResponseEntity<ApiResponse<BotDTO.BotResponse>> getBot(
            @PathVariable Long botId) {
        Bot bot = botService.getBot(botId);
        
        return ResponseEntity.ok(ApiResponse.success(botService.toResponse(bot)));
    }

    @GetMapping("/bots")
    @Operation(summary = "获取我的 Bot 列表")
    public ResponseEntity<ApiResponse<List<BotDTO.BotResponse>>> getMyBots() {
        Long userId = securityUtil.getCurrentUserId();
        
        List<Bot> bots = botService.getOwnerBots(userId);
        
        List<BotDTO.BotResponse> response = bots.stream()
                .map(botService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/realm/bots")
    @Operation(summary = "获取 Realm 的 Bot 列表")
    public ResponseEntity<ApiResponse<List<BotDTO.BotResponse>>> getRealmBots() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<Bot> bots = botService.getRealmBots(realmId);
        
        List<BotDTO.BotResponse> response = bots.stream()
                .map(botService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/bots/{botId}")
    @Operation(summary = "更新 Bot")
    public ResponseEntity<ApiResponse<BotDTO.BotResponse>> updateBot(
            @PathVariable Long botId,
            @RequestBody BotDTO.UpdateBotRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        Bot bot = botService.updateBot(botId, userId, 
                request.getName(), request.getDescription(), request.getEndpointUrl());
        
        return ResponseEntity.ok(ApiResponse.success(botService.toResponse(bot)));
    }

    @DeleteMapping("/bots/{botId}")
    @Operation(summary = "删除 Bot")
    public ResponseEntity<ApiResponse<Void>> deleteBot(@PathVariable Long botId) {
        Long userId = securityUtil.getCurrentUserId();
        
        botService.deleteBot(botId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/bots/{botId}/reset-api-key")
    @Operation(summary = "重置 API Key")
    public ResponseEntity<ApiResponse<BotDTO.ApiKeyResponse>> resetApiKey(
            @PathVariable Long botId) {
        Long userId = securityUtil.getCurrentUserId();
        
        String newApiKey = botService.resetApiKey(botId, userId);
        
        BotDTO.ApiKeyResponse response = BotDTO.ApiKeyResponse.builder()
                .botId(botId)
                .apiKey(newApiKey)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bots/{botId}/commands")
    @Operation(summary = "添加 Bot 命令")
    public ResponseEntity<ApiResponse<BotDTO.CommandResponse>> addCommand(
            @PathVariable Long botId,
            @RequestBody BotDTO.CreateCommandRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        
        // 验证所有者
        Bot bot = botService.getBot(botId);
        if (!bot.getOwnerId().equals(userId)) {
            throw new IllegalStateException("不是 Bot 所有者");
        }
        
        BotCommand command = botService.addCommand(botId, 
                request.getCommandName(), request.getDescription(), request.getHandler());
        
        BotDTO.CommandResponse response = BotDTO.CommandResponse.builder()
                .id(command.getId())
                .commandName(command.getCommandName())
                .description(command.getDescription())
                .handler(command.getHandler())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bots/{botId}/commands")
    @Operation(summary = "获取 Bot 命令列表")
    public ResponseEntity<ApiResponse<List<BotDTO.CommandResponse>>> getBotCommands(
            @PathVariable Long botId) {
        List<BotCommand> commands = botService.getBotCommands(botId);
        
        List<BotDTO.CommandResponse> response = commands.stream()
                .map(c -> BotDTO.CommandResponse.builder()
                        .id(c.getId())
                        .commandName(c.getCommandName())
                        .description(c.getDescription())
                        .handler(c.getHandler())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/bots/{botId}/commands/{commandId}")
    @Operation(summary = "删除 Bot 命令")
    public ResponseEntity<ApiResponse<Void>> deleteCommand(
            @PathVariable Long botId,
            @PathVariable Long commandId) {
        Long userId = securityUtil.getCurrentUserId();
        
        // 验证所有者
        Bot bot = botService.getBot(botId);
        if (!bot.getOwnerId().equals(userId)) {
            throw new IllegalStateException("不是 Bot 所有者");
        }
        
        botService.deleteCommand(commandId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/bots/search")
    @Operation(summary = "搜索 Bot")
    public ResponseEntity<ApiResponse<List<BotDTO.BotResponse>>> searchBots(
            @RequestParam String query) {
        Long realmId = securityUtil.getCurrentRealmId();
        
        List<Bot> bots = botService.searchBots(realmId, query);
        
        List<BotDTO.BotResponse> response = bots.stream()
                .map(botService::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bots/count")
    @Operation(summary = "统计 Bot 数量")
    public ResponseEntity<ApiResponse<Long>> countBots() {
        Long realmId = securityUtil.getCurrentRealmId();
        
        Long count = botService.countRealmBots(realmId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Bot API 端点（供外部调用）

    @PostMapping("/api/v1/bot/send")
    @Operation(summary = "Bot 发送消息（API 调用）")
    public ResponseEntity<ApiResponse<BotDTO.SendMessageResponse>> botSendMessage(
            @RequestBody BotDTO.SendMessageRequest request) {
        
        Long botUserId = botService.sendMessage(
                request.getApiKey(), 
                request.getStreamId(), 
                request.getTopic(), 
                request.getContent());
        
        BotDTO.SendMessageResponse response = BotDTO.SendMessageResponse.builder()
                .botUserId(botUserId)
                .streamId(request.getStreamId())
                .topic(request.getTopic())
                .sent(true)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/bot/send-private")
    @Operation(summary = "Bot 发送私信（API 调用）")
    public ResponseEntity<ApiResponse<BotDTO.SendMessageResponse>> botSendPrivateMessage(
            @RequestBody BotDTO.SendPrivateMessageRequest request) {
        
        Long botUserId = botService.sendPrivateMessage(
                request.getApiKey(), 
                request.getRecipientId(), 
                request.getContent());
        
        BotDTO.SendMessageResponse response = BotDTO.SendMessageResponse.builder()
                .botUserId(botUserId)
                .recipientId(request.getRecipientId())
                .sent(true)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/bot/validate")
    @Operation(summary = "验证 Bot API Key")
    public ResponseEntity<ApiResponse<Boolean>> validateApiKey(
            @RequestParam String apiKey) {
        
        boolean valid = botService.validateApiKey(apiKey);
        
        return ResponseEntity.ok(ApiResponse.success(valid));
    }
}