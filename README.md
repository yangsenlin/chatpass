# ChatPass - Zulip Java 复刻项目

基于 [Zulip](https://github.com/zulip/zulip) 开源团队聊天平台复刻，使用 Java + Spring Boot 重写后端。

## 原项目信息

- **原项目：** Zulip
- **许可证：** Apache 2.0
- **原版权声明：** Copyright (c) 2012-2024 Kandra Labs, Inc.
- **GitHub：** https://github.com/zulip/zulip

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Data JPA + Hibernate
- Spring Security + JWT
- Spring WebSocket (STOMP)
- PostgreSQL
- Redis

### 前端（保持不变）
- TypeScript
- React

## Zulip 核心概念

Zulip 的独特之处在于 **Topic-based Threading（基于主题的线程）**：

- **Realm** - 组织/域
- **Stream** - 频道/流（类似 Slack 的 Channel）
- **Topic** - 主题（每条消息都有一个主题）
- **Message** - 消息
- **Direct Message** - 私信

这种设计使得 Zulip 可以高效地组织大量讨论，特别适合远程协作。

## 快速开始

```bash
# 构建项目
./gradlew build

# 运行应用
./gradlew bootRun

# 访问
# API: http://localhost:8080/api/v1
# Swagger: http://localhost:8080/swagger-ui.html
```

## 项目结构

```
chatpass-server/
├── src/main/java/com/chatpass/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── WebSocketConfig.java      ← WebSocket 实时推送配置
│   ├── entity/
│   │   ├── Realm.java
│   │   ├── UserProfile.java
│   │   ├── Stream.java
│   │   ├── Recipient.java
│   │   ├── Message.java
│   │   ├── Client.java
│   │   ├── Subscription.java
│   │   └── UserMessage.java          ← Flags 位掩码机制
│   ├── repository/
│   │   ├── RealmRepository.java
│   │   ├── UserProfileRepository.java
│   │   ├── StreamRepository.java
│   │   ├── RecipientRepository.java
│   │   ├── MessageRepository.java
│   │   ├── ClientRepository.java
│   │   ├── SubscriptionRepository.java
│   │   └── UserMessageRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── MessageService.java
│   │   ├── StreamService.java
│   │   ├── NarrowService.java        ← Zulip Narrow 查询
│   │   └── UserMessageService.java   ← Flags 状态管理
│   ├── controller/api/v1/
│   │   ├── AuthController.java
│   │   ├── MessageController.java
│   │   ├── StreamController.java
│   │   ├── NarrowController.java     ← Narrow 查询 API
│   │   └── UserMessageController.java ← Flags 操作 API
│   ├── dto/
│   │   ├── ApiResponse.java
│   │   ├── AuthDTO.java
│   │   ├── UserDTO.java
│   │   ├── MessageDTO.java
│   │   ├── StreamDTO.java
│   │   ├── NarrowDTO.java            ← Narrow 查询 DTO
│   │   ├── WebSocketDTO.java         ← WebSocket Event DTO
│   │   └── UserMessageDTO.java       ← Flags 操作 DTO
│   ├── websocket/
│   │   └── WebSocketEventHandler.java ← 实时推送处理
│   ├── exception/
│   │   └── ResourceNotFoundException.java
│   └── security/
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── ChatPassApplication.java
└── src/main/resources/
    ├── application.yml
    └── db/init.sql
```

## 许可证

Apache 2.0

Copyright (c) 2026 ChatPass Team
Based on Zulip (https://github.com/zulip/zulip)
Original Copyright (c) 2012-2024 Kandra Labs, Inc.