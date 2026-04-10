# ChatPass 详细工作计划

## 当前进度

| 指标 | 数值 | 对比上次 |
|------|------|----------|
| Java 文件 | 74 | +12 |
| 代码行数 | 7,332 | +1,033 |
| 测试文件 | 4 | - |
| 测试用例 | 22 | - |
| Controller | 9 | +3 |
| Service | 10 | +3 |
| Entity | 11 | +2 |
| Repository | 10 | +2 |

---

## 已完成功能

### ✅ 核心消息系统
- 消息发送（Stream / Direct Message）
- Narrow 查询
- Topic-based Threading
- Markdown 渲染
- @提及检测
- Alert Words
- UserMessage Flags
- 消息软删除

### ✅ 实时通信
- WebSocket 配置
- 事件推送框架
- 输入状态（Typing）

### ✅ 用户系统
- JWT 认证
- 用户注册/登录
- API Key 认证
- 用户设置

### ✅ 表情反应
- 添加/移除反应
- 反应聚合统计
- 批量查询

### ✅ Stream 管理
- 创建/更新/删除
- 订阅/取消订阅
- Topic 列表

---

## 第一阶段剩余任务（本周）

### 1.1 消息系统完善

| 任务 | 状态 | 预计时间 |
|------|------|----------|
| 消息引用/转发 | ❌ 待实现 | 2h |
| 消息搜索优化 | ❌ 待实现 | 2h |
| 文件上传 | ❌ 待实现 | 3h |
| 话题解析/合并 | ❌ 待实现 | 2h |

### 1.2 测试完善

| 任务 | 状态 | 预计时间 |
|------|------|----------|
| ReactionService 测试 | ❌ 待实现 | 1h |
| TypingService 测试 | ❌ 待实现 | 1h |
| UserSettingsService 测试 | ❌ 待实现 | 1h |
| Repository 测试 | ❌ 待实现 | 2h |
| 集成测试 | ❌ 待实现 | 3h |

---

## 第二阶段：生产就绪（下周）

### 2.1 通知系统

| 任务 | 说明 |
|------|------|
| 邮件服务 | 新消息邮件提醒 |
| 推送服务 | 移动推送（预留接口） |
| 通知模板 | 自定义通知模板 |

### 2.2 缓存系统

| 任务 | 说明 |
|------|------|
| Redis 缓存 | 用户信息、消息列表 |
| 计数缓存 | 未读数、订阅数 |
| 热点缓存 | 热门 Stream |

### 2.3 性能优化

| 任务 | 说明 |
|------|------|
| 批量查询优化 | 减少 N+1 问题 |
| 索引优化 | 添加必要索引 |
| 分页优化 | 游标分页 |

---

## 第三阶段：管理功能

### 3.1 Realm 管理

| 任务 | 说明 |
|------|------|
| Realm 设置 | 组织配置 |
| 邀请系统 | 邀请用户 |
| 成员管理 | 角色管理 |
| 权限系统 | 细粒度权限 |

### 3.2 审计日志

| 任务 | 说明 |
|------|------|
| 操作日志 | 关键操作记录 |
| 消息历史 | 完整历史 |
| 登录日志 | 登录记录 |

---

## API 接口清单

### 认证 API

| Method | Path | 说明 |
|--------|------|------|
| POST | /auth/login | 登录 |
| POST | /register | 注册 |
| POST | /fetch_api_key | 获取 API Key |
| POST | /auth/api_key_login | API Key 登录 |
| POST | /auth/change_password | 修改密码 |

### 消息 API

| Method | Path | 说明 |
|--------|------|------|
| POST | /messages | 发送消息 |
| GET | /messages/{id} | 获取消息 |
| PATCH | /messages/{id} | 编辑消息 |
| POST | /messages/query | Narrow 查询 |
| POST | /messages/flags | 更新 Flags |
| POST | /messages/mark_all_as_read | 全部已读 |
| GET | /unread | 未读摘要 |

### Stream API

| Method | Path | 说明 |
|--------|------|------|
| GET | /streams | 获取所有 |
| GET | /streams/{id} | 获取详情 |
| POST | /streams | 创建 |
| PATCH | /streams/{id} | 更新 |
| DELETE | /streams/{id} | 删除 |
| POST | /streams/{id}/subscribe | 订阅 |
| DELETE | /streams/{id}/subscribe | 取消订阅 |

### Reaction API

| Method | Path | 说明 |
|--------|------|------|
| POST | /messages/{id}/reactions | 添加反应 |
| DELETE | /messages/{id}/reactions | 移除反应 |
| GET | /messages/{id}/reactions | 获取反应 |

### Typing API

| Method | Path | 说明 |
|--------|------|------|
| POST | /typing/start | 开始输入 |
| POST | /typing/stop | 停止输入 |
| GET | /typing/status | 获取状态 |

### 用户设置 API

| Method | Path | 说明 |
|--------|------|------|
| GET | /users/me/settings | 获取设置 |
| PATCH | /users/me/settings | 更新设置 |
| PUT | /users/me/timezone | 设置时区 |
| PUT | /users/me/language | 设置语言 |

---

## 代码目标

| 阶段 | 文件数 | 代码行数 | 测试用例 |
|------|--------|----------|----------|
| 当前 | 74 | 7,332 | 22 |
| 第一阶段完成 | 90+ | 12,000+ | 60+ |
| 第二阶段完成 | 110+ | 18,000+ | 120+ |
| 第三阶段完成 | 130+ | 25,000+ | 200+ |

---

## 下一步行动

**立即执行：**

1. 补充更多测试用例
2. 实现消息引用/转发
3. 实现文件上传
4. 添加更多 Narrow 操作符

---

*更新时间: 2026-04-10 15:40*