# ChatPass 待实现功能清单

基于 Zulip 原版功能对比，以下是 ChatPass 待实现功能列表。

---

## 🔴 高优先级（核心功能）

### ✅ 1. 用户注册/邀请系统
- ✅ 用户注册流程
- ✅ 邀请链接生成
- ✅ Realm 加入流程
- ✅ 邀请码验证

**文件已创建：**
- ✅ `entity/RealmInvite.java`
- ✅ `service/InviteService.java`
- ✅ `controller/api/v1/InviteController.java`

---

### ✅ 2. 邮件通知系统
- ✅ 邀请邮件发送
- ✅ 提及邮件通知
- ✅ Stream 新消息通知
- ✅ 私信通知
- ✅ 邮件模板配置

**文件已创建：**
- ✅ `service/EmailService.java`
- ✅ `config/EmailConfig.java`

---

### ✅ 3. Read Receipts（阅读回执）
- ✅ 消息已读确认
- ✅ 已读时间戳记录
- ✅ 已读用户列表查询
- ✅ 已读通知推送

**文件已创建：**
- ✅ `entity/ReadReceipt.java`
- ✅ `service/ReadReceiptService.java`
- ✅ `controller/api/v1/ReadReceiptController.java`

---

### ✅ 4. 权限系统
- ✅ 用户角色管理（Admin/Member/Guest）
- Stream 权限检查
- 消息权限检查
- API 权限过滤

**文件待创建：**
- `entity/Role.java`
- `service/PermissionService.java`
- `security/PermissionFilter.java`

---

### 5. Rate Limiting（请求限制）
- API 请求频率限制
- 消息发送限制
- 登录尝试限制
- 分布式限流（Redis）

**文件待创建：**
- `config/RateLimitConfig.java`
- `filter/RateLimitFilter.java`

---

## 🟡 中优先级（重要功能）

### 6. 话题管理
- Topic 解析（自动提取）
- Topic 重命名
- Topic 合并
- Topic 拆分

**文件待创建：**
- `service/TopicService.java`
- `controller/api/v1/TopicController.java`

---

### 7. 消息搜索增强
- PostgreSQL 全文搜索（tsvector）
- 搜索结果排序
- 搜索建议
- 搜索历史

**文件待修改：**
- `service/NarrowService.java` - 搜索优化
- `repository/MessageRepository.java` - 添加搜索查询

---

### 8. 消息星标/折叠增强
- 星标消息列表查询
- 星标消息计数
- 消息折叠状态管理
- 折叠消息批量操作

**文件待修改：**
- `service/UserMessageService.java`
- `controller/api/v1/UserMessageController.java`

---

### 9. 用户组管理
- 用户组创建
- 组成员管理
- 组权限控制
- @group 提及

**文件待创建：**
- `entity/UserGroup.java`
- `entity/UserGroupMember.java`
- `service/UserGroupService.java`

---

### 10. Stream 权限管理
- Stream 成员管理
- Stream 权限级别（Public/Private/Invite-only）
- Stream 管理员
- Stream 权限继承

**文件待修改：**
- `entity/Stream.java` - 添加权限字段
- `service/StreamService.java` - 权限检查

---

## 🟢 低优先级（可选功能）

### 11. 消息队列集成
- 异步任务处理
- 消息发送队列
- 邮件发送队列
- 任务状态追踪

**技术选型：**
- Redis Streams 或 RabbitMQ

---

### 12. 移动推送集成
- Firebase Cloud Messaging (FCM)
- Apple Push Notification Service (APNs)
- 推送消息格式
- 推送配置管理

---

### 13. 消息翻译
- 消息内容翻译
- 多语言支持
- 翻译 API 集成

---

### 14. 机器人 API
- Bot 用户创建
- Bot API Key 管理
- Bot 消息发送
- Bot 命令处理

---

### 15. 自定义表情
- 自定义 Emoji 上传
- Emoji 别名管理
- Emoji 使用统计

---

### 16. 消息投票/统计
- 消息投票功能
- 消息统计（回复数、反应数）
- 消息热度排序

---

### 17. 话题订阅
- 话题订阅管理
- 话题通知设置
- 话题静音

---

### 18. 消息归档
- 消息归档策略
- 归档消息查询
- 归档消息恢复

---

### 19. 审计日志
- 操作日志记录
- 日志查询
- 日志导出

---

### 20. 数据分析
- 消息统计报告
- 用户活跃度分析
- Stream 使用统计

---

## 📋 实现计划

| 序号 | 功能 | 优先级 | 预计工作量 |
|------|------|--------|------------|
| 1 | 用户注册/邀请 | 🔴 | 中 |
| 2 | 邮件通知 | 🔴 | 中 |
| 3 | Read Receipts | 🔴 | 小 |
| 4 | 权限系统 | 🔴 | 大 |
| 5 | Rate Limiting | 🔴 | 小 |
| 6 | 话题管理 | 🟡 | 中 |
| 7 | 搜索增强 | 🟡 | 中 |
| 8 | 星标增强 | 🟡 | 小 |
| 9 | 用户组 | 🟡 | 中 |
| 10 | Stream 权限 | 🟡 | 中 |
| 11-20 | 其他功能 | 🟢 | 可选 |

---

## 实施顺序

**第一阶段（核心完善）：**
1. Read Receipts（阅读回执）
2. Rate Limiting（请求限制）
3. 权限系统（基础）

**第二阶段（功能增强）：**
4. 用户注册/邀请
5. 邮件通知
6. 话题管理
7. 搜索增强

**第三阶段（高级功能）：**
8. 用户组管理
9. 消息队列
10. 移动推送

---

*创建时间: 2026-04-10*