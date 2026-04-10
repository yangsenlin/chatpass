package com.chatpass.service;

import com.chatpass.dto.BotDTO;
import com.chatpass.dto.MessageDTO;
import com.chatpass.entity.Bot;
import com.chatpass.entity.BotCommand;
import com.chatpass.entity.Message;
import com.chatpass.entity.Stream;
import com.chatpass.entity.UserProfile;
import com.chatpass.repository.BotRepository;
import com.chatpass.repository.BotCommandRepository;
import com.chatpass.repository.UserProfileRepository;
import com.chatpass.repository.StreamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BotService
 * 
 * 机器人管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {

    private final BotRepository botRepository;
    private final BotCommandRepository commandRepository;
    private final UserProfileRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final MessageService messageService;
    private final StreamRepository streamRepository;

    /**
     * 创建 Bot
     */
    @Transactional
    public Bot createBot(Long ownerId, String name, String botType, String description) {
        UserProfile owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Long realmId = owner.getRealm().getId();

        // 检查名称是否已存在
        if (botRepository.existsByName(name, realmId)) {
            throw new IllegalStateException("Bot 名称已存在");
        }

        // 创建 Bot 用户（UserProfile）
        UserProfile botUser = new UserProfile();
        botUser.setFullName(name);
        botUser.setEmail("bot-" + name.toLowerCase() + "@chatpass.local");
        botUser.setRealm(owner.getRealm());
        // UserProfile 可能没有 isBot 字段
        botUser.setIsActive(true);
        botUser = userRepository.save(botUser);

        // 创建 Bot
        Bot bot = Bot.builder()
                .name(name)
                .botType(botType)
                .botUserId(botUser.getId())
                .apiKey(Bot.generateApiKey())
                .ownerId(ownerId)
                .realmId(realmId)
                .description(description)
                .isActive(true)
                .build();

        bot = botRepository.save(bot);

        // 记录审计日志
        auditLogService.logCreate(ownerId, AuditLogService.RESOURCE_BOT, bot.getId(), 
                BotDTO.BotResponse.builder()
                        .id(bot.getId())
                        .name(name)
                        .botType(botType)
                        .apiKey(bot.getApiKey())
                        .build());

        log.info("Bot created: {} by user {}", name, ownerId);

        return bot;
    }

    /**
     * 更新 Bot
     */
    @Transactional
    public Bot updateBot(Long botId, Long ownerId, String name, String description, String endpointUrl) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot 不存在"));

        // 验证所有者
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Bot 所有者");
        }

        Bot oldBot = bot;

        if (name != null && !name.equals(bot.getName())) {
            if (botRepository.existsByName(name, bot.getRealmId())) {
                throw new IllegalStateException("Bot 名称已存在");
            }
            bot.setName(name);
        }

        if (description != null) {
            bot.setDescription(description);
        }

        if (endpointUrl != null) {
            bot.setEndpointUrl(endpointUrl);
        }

        bot = botRepository.save(bot);

        // 记录审计日志
        auditLogService.logUpdate(ownerId, AuditLogService.RESOURCE_BOT, bot.getId(), oldBot, bot);

        log.info("Bot updated: {}", botId);

        return bot;
    }

    /**
     * 删除 Bot
     */
    @Transactional
    public void deleteBot(Long botId, Long ownerId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot 不存在"));

        // 验证所有者
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Bot 所有者");
        }

        // 删除所有命令
        commandRepository.deleteByBotId(botId);

        // 软删除 Bot
        bot.setIsActive(false);
        botRepository.save(bot);

        // 删除 Bot 用户
        userRepository.findById(bot.getBotUserId()).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });

        // 记录审计日志
        auditLogService.logDelete(ownerId, AuditLogService.RESOURCE_BOT, bot.getId(), bot);

        log.info("Bot deleted: {}", botId);
    }

    /**
     * 获取 Bot 详情
     */
    public Bot getBot(Long botId) {
        return botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot 不存在"));
    }

    /**
     * 通过 API Key 获取 Bot
     */
    public Optional<Bot> getBotByApiKey(String apiKey) {
        return botRepository.findByApiKey(apiKey);
    }

    /**
     * 获取所有者的 Bot 列表
     */
    public List<Bot> getOwnerBots(Long ownerId) {
        return botRepository.findByOwnerId(ownerId);
    }

    /**
     * 获取 Realm 的 Bot 列表
     */
    public List<Bot> getRealmBots(Long realmId) {
        return botRepository.findByRealmId(realmId);
    }

    /**
     * 验证 API Key
     */
    public boolean validateApiKey(String apiKey) {
        return botRepository.validateApiKey(apiKey);
    }

    /**
     * 重置 API Key
     */
    @Transactional
    public String resetApiKey(Long botId, Long ownerId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot 不存在"));

        if (!bot.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("不是 Bot 所有者");
        }

        String newApiKey = Bot.generateApiKey();
        bot.setApiKey(newApiKey);
        botRepository.save(bot);

        log.info("Bot API key reset: {}", botId);

        return newApiKey;
    }

    /**
     * 添加 Bot 命令
     */
    @Transactional
    public BotCommand addCommand(Long botId, String commandName, String description, String handler) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot 不存在"));

        if (commandRepository.existsCommand(botId, commandName)) {
            throw new IllegalStateException("命令已存在");
        }

        BotCommand command = BotCommand.builder()
                .bot(bot)
                .commandName(commandName)
                .description(description)
                .handler(handler)
                .isActive(true)
                .build();

        command = commandRepository.save(command);

        log.info("Bot command added: {} to bot {}", commandName, botId);

        return command;
    }

    /**
     * 获取 Bot 的所有命令
     */
    public List<BotCommand> getBotCommands(Long botId) {
        return commandRepository.findByBotId(botId);
    }

    /**
     * 删除命令
     */
    @Transactional
    public void deleteCommand(Long commandId) {
        BotCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("命令不存在"));

        command.setIsActive(false);
        commandRepository.save(command);

        log.info("Bot command deleted: {}", commandId);
    }

    /**
     * Bot 发送消息
     */
    @Transactional
    public Long sendMessage(String apiKey, Long streamId, String topic, String content) {
        Bot bot = botRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new IllegalArgumentException("无效的 API Key"));

        if (!bot.getIsActive()) {
            throw new IllegalStateException("Bot 已禁用");
        }

        // 获取 Stream
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("频道不存在"));

        // 调用 MessageService 发送消息
        MessageDTO.Response messageResponse = messageService.sendStreamMessage(
                stream.getRealm().getId(),
                bot.getBotUserId(),
                streamId,
                topic,
                content
        );

        log.info("Bot {} sent message to stream {}", bot.getName(), streamId);

        return messageResponse.getId();
    }

    /**
     * Bot 发送私信
     */
    @Transactional
    public Long sendPrivateMessage(String apiKey, Long recipientId, String content) {
        Bot bot = botRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new IllegalArgumentException("无效的 API Key"));

        if (!bot.getIsActive()) {
            throw new IllegalStateException("Bot 已禁用");
        }

        UserProfile recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("收件人不存在"));

        // 调用 MessageService 发送私信
        MessageDTO.Response messageResponse = messageService.sendDirectMessage(
                bot.getRealmId(),
                bot.getBotUserId(),
                List.of(recipientId),
                content
        );

        log.info("Bot {} sent private message to user {}", bot.getName(), recipientId);

        return messageResponse.getId();
    }

    /**
     * 搜索 Bot
     */
    public List<Bot> searchBots(Long realmId, String query) {
        return botRepository.searchBots(realmId, query);
    }

    /**
     * 统计 Bot 数量
     */
    public Long countRealmBots(Long realmId) {
        return botRepository.countByRealmId(realmId);
    }

    /**
     * 转换为 DTO
     */
    public BotDTO.BotResponse toResponse(Bot bot) {
        List<BotCommand> commands = commandRepository.findActiveCommands(bot.getId());
        
        return BotDTO.BotResponse.builder()
                .id(bot.getId())
                .name(bot.getName())
                .botType(bot.getBotType())
                .botUserId(bot.getBotUserId())
                .apiKey(bot.getApiKey())
                .ownerId(bot.getOwnerId())
                .realmId(bot.getRealmId())
                .avatarUrl(bot.getAvatarUrl())
                .endpointUrl(bot.getEndpointUrl())
                .description(bot.getDescription())
                .isActive(bot.getIsActive())
                .dateCreated(bot.getDateCreated().toString())
                .commands(commands.stream()
                        .map(c -> BotDTO.CommandResponse.builder()
                                .id(c.getId())
                                .commandName(c.getCommandName())
                                .description(c.getDescription())
                                .handler(c.getHandler())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}