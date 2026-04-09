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
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   ├── exception/
│   └── security/
└── src/main/resources/
    ├── application.yml
    └── db/init.sql
```

## 许可证

Apache 2.0

Copyright (c) 2026 ChatPass Team
Based on Zulip (https://github.com/zulip/zulip)
Original Copyright (c) 2012-2024 Kandra Labs, Inc.