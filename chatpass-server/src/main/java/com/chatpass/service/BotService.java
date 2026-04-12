package com.chatpass.service;

import com.chatpass.dto.BotDTO;
import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.Bot;
import com.chatpass.entity.Realm;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.BotRepository;
import com.chatpass.repository.RealmRepository;
import com.chatpass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 机器人服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {
    
    private final BotRepository botRepository;
    private final UserProfileRepository userRepository;
    private final RealmRepository realmRepository;
    private final MessageService messageService;
    
    /**
     * 创建Bot
     */
    @Transactional
    public BotDTO.BotInfo createBot(Long realmId, String name, String botType, 
                                      String description, String webhookUrl, Long ownerId) {
        
        // 检查名称是否已存在
        if (botRepository.findByRealmIdAndName(realmId, name).isPresent()) {
            throw new IllegalArgumentException("Bot名称已存在: " + name);
        }
        
        // 创建Bot用户
        UserProfile botUser = UserProfile.builder()
                .fullName(name)
                .email(name + "-bot@chatpass.local")
                .botType(1) // 1=Generic bot
                .isActive(true)
                .build();
        
        // 设置Realm
        Realm realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm不存在: " + realmId));
        botUser.setRealm(realm);
        
        botUser = userRepository.save(botUser);
        
        // 生成API Key
        String apiKey = generateApiKey();
        
        // 创建Bot
        Bot bot = Bot.builder()
                .userId(botUser.getId())
                .name(name)
                .botType(botType != null ? botType : "generic")
                .apiKey(apiKey)
                .realmId(realmId)
                .ownerId(ownerId)
                .description(description)
                .webhookUrl(webhookUrl)
                .isActive(true)
                .build();
        
        bot = botRepository.save(bot);
        log.info("创建Bot: {} (realmId: {}, owner: {})", name, realmId, ownerId);
        
        return toBotInfo(bot);
    }
    
    /**
     * 获取组织的所有Bot
     */
    public List<BotDTO.BotInfo> getBotsByRealm(Long realmId) {
        return botRepository.findByRealmIdAndIsActiveTrueOrderByCreatedAtDesc(realmId)
                .stream()
                .map(this::toBotInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取Bot详情
     */
    public Optional<BotDTO.BotInfo> getBotById(Long botId) {
        return botRepository.findById(botId).map(this::toBotInfo);
    }
    
    /**
     * 根据API Key获取Bot
     */
    public Optional<BotDTO.BotInfo> getBotByApiKey(String apiKey) {
        return botRepository.findByApiKey(apiKey).map(this::toBotInfo);
    }
    
    /**
     * 获取用户的Bot
     */
    public List<BotDTO.BotInfo> getBotsByOwner(Long ownerId) {
        return botRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toBotInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新Bot信息
     */
    @Transactional
    public BotDTO.BotInfo updateBot(Long botId, String name, String description, 
                                      String avatarUrl, String webhookUrl) {
        
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot不存在: " + botId));
        
        if (name != null && !name.equals(bot.getName())) {
            if (botRepository.findByRealmIdAndName(bot.getRealmId(), name).isPresent()) {
                throw new IllegalArgumentException("Bot名称已存在: " + name);
            }
            bot.setName(name);
            
            // 更新Bot用户名称
            UserProfile botUser = userRepository.findById(bot.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Bot用户不存在"));
            botUser.setFullName(name);
            userRepository.save(botUser);
        }
        
        if (description != null) {
            bot.setDescription(description);
        }
        
        if (avatarUrl != null) {
            bot.setAvatarUrl(avatarUrl);
        }
        
        if (webhookUrl != null) {
            bot.setWebhookUrl(webhookUrl);
        }
        
        bot = botRepository.save(bot);
        log.info("更新Bot: {}", bot.getName());
        
        return toBotInfo(bot);
    }
    
    /**
     * 重新生成API Key
     */
    @Transactional
    public String regenerateApiKey(Long botId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot不存在: " + botId));
        
        String newApiKey = generateApiKey();
        bot.setApiKey(newApiKey);
        botRepository.save(bot);
        
        log.info("重新生成API Key: Bot={}", bot.getName());
        return newApiKey;
    }
    
    /**
     * 禁用Bot
     */
    @Transactional
    public void deactivateBot(Long botId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot不存在: " + botId));
        
        bot.setIsActive(false);
        botRepository.save(bot);
        
        // 禁用Bot用户
        UserProfile botUser = userRepository.findById(bot.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Bot用户不存在"));
        botUser.setIsActive(false);
        userRepository.save(botUser);
        
        log.info("禁用Bot: {}", bot.getName());
    }
    
    /**
     * 激活Bot
     */
    @Transactional
    public void activateBot(Long botId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot不存在: " + botId));
        
        bot.setIsActive(true);
        botRepository.save(bot);
        
        // 激活Bot用户
        UserProfile botUser = userRepository.findById(bot.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Bot用户不存在"));
        botUser.setIsActive(true);
        userRepository.save(botUser);
        
        log.info("激活Bot: {}", bot.getName());
    }
    
    /**
     * Bot发送消息
     */
    @Transactional
    public MessageDTO.Response sendMessage(String apiKey, Long streamId, String topic, 
                                             String content, List<Long> toUserIds) {
        
        Bot bot = botRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new IllegalArgumentException("无效的API Key"));
        
        if (!bot.getIsActive()) {
            throw new IllegalStateException("Bot未激活");
        }
        
        // 调用MessageService发送消息
        Long botRealmId = bot.getRealmId();
        if (streamId != null) {
            return messageService.sendStreamMessage(bot.getUserId(), botRealmId, 
                                                     streamId, topic, content);
        } else if (toUserIds != null && !toUserIds.isEmpty()) {
            return messageService.sendDirectMessage(bot.getUserId(), botRealmId, 
                                                      toUserIds, content);
        } else {
            throw new IllegalArgumentException("必须指定streamId或toUserIds");
        }
    }
    
    /**
     * 删除Bot
     */
    @Transactional
    public void deleteBot(Long botId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot不存在: " + botId));
        
        // 删除Bot用户
        userRepository.findById(bot.getUserId())
                .ifPresent(userRepository::delete);
        
        // 删除Bot
        botRepository.delete(bot);
        log.info("删除Bot: {}", bot.getName());
    }
    
    /**
     * 验证API Key
     */
    public boolean validateApiKey(String apiKey) {
        return botRepository.findByApiKey(apiKey)
                .map(Bot::getIsActive)
                .orElse(false);
    }
    
    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private BotDTO.BotInfo toBotInfo(Bot bot) {
        return BotDTO.BotInfo.builder()
                .id(bot.getId())
                .userId(bot.getUserId())
                .name(bot.getName())
                .botType(bot.getBotType())
                .apiKey(bot.getApiKey())
                .realmId(bot.getRealmId())
                .ownerId(bot.getOwnerId())
                .avatarUrl(bot.getAvatarUrl())
                .description(bot.getDescription())
                .webhookUrl(bot.getWebhookUrl())
                .isActive(bot.getIsActive())
                .createdAt(bot.getCreatedAt())
                .updatedAt(bot.getUpdatedAt())
                .build();
    }
}
