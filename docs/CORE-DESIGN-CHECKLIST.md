# Zulip 核心设计复刻清单

本文档记录 ChatPass 复刻 Zulip 原版核心设计的完成情况。

---

## ✅ 已完成

### 1. Topic-based Threading（基于主题的线程）

**Zulip 核心特性**

- ✅ `Message.subject` 存储 Topic 名称
- ✅ `Message.getTopicName()` 方法获取 Topic
- ✅ Stream + Topic 组合查询支持
- ✅ Topic 自动聚合消息

**文件：**
- `entity/Message.java`
- `service/MessageService.java`
- `repository/MessageRepository.java`

---

### 2. Recipient 抽象（消息投递核心）

**统一的消息投递模型**

- ✅ `Recipient` 实体抽象投递目标
- ✅ `TYPE_STREAM` (1) = 频道消息
- ✅ `TYPE_PRIVATE` (2) = 私信组
- ✅ Stream ↔ Recipient 关联

**文件：**
- `entity/Recipient.java`
- `repository/RecipientRepository.java`

---

### 3. Narrow 查询 API

**Zulip 核心消息过滤机制**

- ✅ `NarrowDTO.Request` - 查询请求
- ✅ `NarrowDTO.Filter` - 过滤条件
- ✅ `NarrowDTO.Operators` - 操作符常量
- ✅ `NarrowDTO.Operands` - 过滤值常量
- ✅ `NarrowService.query()` - 查询执行
- ✅ `NarrowController` - API 接口

**支持的操作符：**
| Operator | 说明 |
|----------|------|
| `stream` | 频道过滤 |
| `topic` | 话题过滤 |
| `is-private` | 私信过滤 |
| `is-mentioned` | 提及消息 |
| `is-starred` | 标记消息 |
| `is-unread` | 未读消息 |
| `sender` | 发送者过滤 |
| `search` | 内容搜索 |

**文件：**
- `dto/NarrowDTO.java`
- `service/NarrowService.java`
- `controller/api/v1/NarrowController.java`

---

### 4. UserMessage Flags（用户级消息状态）

**基于位掩码的 Flags 机制**

- ✅ `UserMessage.flags` - 位掩码存储
- ✅ Flag 常量定义：
  - `FLAG_READ` (1) - 已读
  - `FLAG_STARRED` (2) - 标记
  - `FLAG_COLLAPSED` (4) - 折叠
  - `FLAG_MENTIONED` (8) - 提及
  - `FLAG_WILDCARD_MENTIONED` (16) - @all/@everyone
  - `FLAG_HAS_ALERT_WORD` (64) - 关键词
  - `FLAG_HISTORICAL` (128) - 历史消息
- ✅ `isRead()`/`setRead()` 方法
- ✅ `isStarred()`/`setStarred()` 方法
- ✅ `UserMessageService.updateFlags()` - 更新 Flags
- ✅ `UserMessageService.getUnreadSummary()` - 未读摘要
- ✅ `UserMessageService.markAllAsRead()` - 全部标记已读

**文件：**
- `entity/UserMessage.java`
- `dto/UserMessageDTO.java`
- `service/UserMessageService.java`
- `controller/api/v1/UserMessageController.java`

---

### 5. WebSocket 实时推送

**基于 STOMP 的实时消息同步**

- ✅ `WebSocketConfig` - WebSocket 配置
- ✅ `WebSocketEventHandler` - 事件处理器
- ✅ `WebSocketDTO` - Event DTO

**支持的事件：**
| Event | 说明 |
|-------|------|
| `message` | 新消息 |
| `update_message` | 消息更新 |
| `delete_message` | 消息删除 |
| `read` | 已读状态 |
| `starred/unstarred` | 标记状态 |
| `heartbeat` | 心跳 |
| `presence` | 用户状态 |

**推送路径：**
- `/topic/realm/{realmId}/stream/{streamId}` - Stream 消息
- `/topic/realm/{realmId}/stream/{streamId}/topic/{topic}` - Topic 消息
- `/topic/realm/{realmId}/private` - 私信
- `/user/{userId}/queue/read` - 已读状态
- `/user/{userId}/queue/flags` - Flags 状态

**文件：**
- `config/WebSocketConfig.java`
- `websocket/WebSocketEventHandler.java`
- `dto/WebSocketDTO.java`

---

### 6. 订阅机制

**用户订阅 Stream 关系**

- ✅ `Subscription` 实体
- ✅ 用户级订阅设置（颜色、通知等）
- ✅ 订阅查询支持

**文件：**
- `entity/Subscription.java`
- `repository/SubscriptionRepository.java`

---

## 🔄 待完善

### 1. 私信多收件人处理

**当前状态：** 基础框架已搭建，具体逻辑待实现

**需要：**
- 处理 `Recipient` 的多用户私信
- 创建 Private Recipient 时关联多个用户
- 消息发送时为所有收件人创建 `UserMessage`

---

### 2. @提及检测

**当前状态：** ✅ 已完成

**已实现：**
- ✅ `@username` 检测
- ✅ `@all/@everyone` 广播提及
- ✅ 自动设置 `FLAG_MENTIONED`
- ✅ 自动设置 `FLAG_WILDCARD_MENTIONED`

**文件：**
- `service/MarkdownService.java`（detectMentions）
- `service/MessageService.java`（createUserMessagesForStream）

---

### 3. Alert Word（关键词提醒）

**当前状态：** Flag 常量已定义，功能待实现

**需要：**
- 用户自定义关键词列表
- 消息发送时检测关键词
- 自动设置 `FLAG_HAS_ALERT_WORD`

---

### 4. Markdown 渲染

**当前状态：** ✅ 已完成

**已实现：**
- ✅ CommonMark GFM 渲染（表格、删除线）
- ✅ `MarkdownService.render()` 方法
- ✅ 存储 `rendered_content`
- ✅ 版本控制 `rendered_content_version`

**文件：**
- `build.gradle`（添加 commonmark 依赖）
- `service/MarkdownService.java`

---

### 5. 消息编辑历史

**当前状态：** ✅ 已完成

**已实现：**
- ✅ 存储编辑历史 JSON
- ✅ `Message.editHistory` 字段
- ✅ 编辑时自动记录

**文件：**
- `entity/Message.java`
- `service/MessageService.java`（update方法）

---

### 2. @提及检测

**当前状态：** ✅ 已完成

**已实现：**
- ✅ `@username` 检测
- ✅ `@all/@everyone` 广播提及
- ✅ 自动设置 `FLAG_MENTIONED`
- ✅ 自动设置 `FLAG_WILDCARD_MENTIONED`

**文件：**
- `service/MarkdownService.java`（detectMentions）
- `service/MessageService.java`（createUserMessagesForStream）

---

## 📊 代码统计

| 类别 | 数量 |
|------|------|
| Java 文件 | 46 |
| Entity | 8 |
| Repository | 8 |
| Service | 7 |
| Controller | 5 |
| DTO | 8 |
| Config | 3 |
| WebSocket | 1 |

---

## 📋 复刻对照表

| Zulip 设计 | ChatPass 实现 | 状态 |
|------------|---------------|------|
| Topic-based Threading | Message.subject | ✅ |
| Recipient 抽象 | Recipient 实体 | ✅ |
| Narrow 查询 | NarrowService | ✅ |
| UserMessage Flags | UserMessage.flags | ✅ |
| WebSocket 推送 | WebSocketEventHandler | ✅ |
| Subscription 订阅 | Subscription 实体 | ✅ |
| Markdown 渲染 | MarkdownService | ✅ |
| @提及检测 | MarkdownService.detectMentions | ✅ |
| 编辑历史 | Message.editHistory | ✅ |
| Alert Word | AlertWordService | 🔄框架 |
| 私信多收件人 | 已实现 | ✅ |

---

*更新时间: 2026-04-10 13:00*