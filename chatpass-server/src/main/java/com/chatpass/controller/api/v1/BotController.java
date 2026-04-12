package com.chatpass.controller.api.v1;

import com.chatpass.dto.BotDTO;
import com.chatpass.dto.MessageDTO;
import com.chatpass.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机器人控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BotController {
    
    private final BotService botService;
    
    /**
     * 创建Bot
     */
    @PostMapping("/realm/{realmId}/bots")
    public ResponseEntity<BotDTO.BotInfo> createBot(
            @PathVariable Long realmId,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "generic") String botType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String webhookUrl,
            @RequestParam Long ownerId) {
        
        BotDTO.BotInfo bot = botService.createBot(realmId, name, botType, description, webhookUrl, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bot);
    }
    
    /**
     * 获取组织的所有Bot
     */
    @GetMapping("/realm/{realmId}/bots")
    public ResponseEntity<List<BotDTO.BotInfo>> getRealmBots(@PathVariable Long realmId) {
        List<BotDTO.BotInfo> bots = botService.getBotsByRealm(realmId);
        return ResponseEntity.ok(bots);
    }
    
    /**
     * 获取Bot详情
     */
    @GetMapping("/bots/{botId}")
    public ResponseEntity<BotDTO.BotInfo> getBot(@PathVariable Long botId) {
        return botService.getBotById(botId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取用户的Bot
     */
    @GetMapping("/users/{ownerId}/bots")
    public ResponseEntity<List<BotDTO.BotInfo>> getUserBots(@PathVariable Long ownerId) {
        List<BotDTO.BotInfo> bots = botService.getBotsByOwner(ownerId);
        return ResponseEntity.ok(bots);
    }
    
    /**
     * 更新Bot
     */
    @PatchMapping("/bots/{botId}")
    public ResponseEntity<BotDTO.BotInfo> updateBot(
            @PathVariable Long botId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String webhookUrl) {
        
        BotDTO.BotInfo bot = botService.updateBot(botId, name, description, avatarUrl, webhookUrl);
        return ResponseEntity.ok(bot);
    }
    
    /**
     * 重新生成API Key
     */
    @PostMapping("/bots/{botId}/regenerate_api_key")
    public ResponseEntity<String> regenerateApiKey(@PathVariable Long botId) {
        String newApiKey = botService.regenerateApiKey(botId);
        return ResponseEntity.ok(newApiKey);
    }
    
    /**
     * 禁用Bot
     */
    @PostMapping("/bots/{botId}/deactivate")
    public ResponseEntity<Void> deactivateBot(@PathVariable Long botId) {
        botService.deactivateBot(botId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 激活Bot
     */
    @PostMapping("/bots/{botId}/activate")
    public ResponseEntity<Void> activateBot(@PathVariable Long botId) {
        botService.activateBot(botId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Bot发送消息（通过API Key）
     */
    @PostMapping("/bot/send_message")
    public ResponseEntity<MessageDTO.Response> sendMessage(
            @RequestParam String api_key,
            @RequestParam(required = false) Long streamId,
            @RequestParam(required = false) String topic,
            @RequestParam String content,
            @RequestParam(required = false) List<Long> toUserIds) {
        
        MessageDTO.Response message = botService.sendMessage(api_key, streamId, topic, content, toUserIds);
        return ResponseEntity.ok(message);
    }
    
    /**
     * 验证API Key
     */
    @GetMapping("/bot/validate")
    public ResponseEntity<Boolean> validateApiKey(@RequestParam String api_key) {
        boolean valid = botService.validateApiKey(api_key);
        return ResponseEntity.ok(valid);
    }
    
    /**
     * 删除Bot
     */
    @DeleteMapping("/bots/{botId}")
    public ResponseEntity<Void> deleteBot(@PathVariable Long botId) {
        botService.deleteBot(botId);
        return ResponseEntity.noContent().build();
    }
}
