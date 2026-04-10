package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * BotCommand 实体 - Bot 命令定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bot_commands", indexes = {
    @Index(name = "idx_cmd_bot", columnList = "bot_id")
})
public class BotCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 所属 Bot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    // 命令名称（如 /weather）
    @Column(name = "command_name", nullable = false, length = 50)
    private String commandName;

    // 命令描述
    @Column(name = "description", length = 500)
    private String description;

    // 命令处理器（方法名或 URL）
    @Column(name = "handler", length = 200)
    private String handler;

    // 是否激活
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 创建时间
    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;
}