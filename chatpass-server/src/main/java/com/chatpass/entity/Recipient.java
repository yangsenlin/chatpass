package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Recipient 实体 - Zulip 消息接收者
 * 对应 Zulip Recipient model
 * 
 * Recipient 是消息投递的目标，可以是 Stream 或 私信组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 类型: 1=Stream, 2=Private(Direct Message)
    @Column(name = "type", nullable = false)
    @Builder.Default
    private Integer type = 1;

    // 关联的 Stream ID (type=1 时)
    @Column(name = "stream_id")
    private Long streamId;

    // 类型常量
    public static final int TYPE_STREAM = 1;
    public static final int TYPE_PRIVATE = 2;

    /**
     * 获取显示标签
     */
    public String label() {
        if (type == TYPE_STREAM && streamId != null) {
            return "Stream#" + streamId;
        } else if (type == TYPE_PRIVATE) {
            return "Private";
        }
        return "Unknown";
    }
}