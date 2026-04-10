# ChatPass 后续工作计划

基于差距分析，制定务实的改进计划。

---

## 阶段一：让系统可用（优先级最高）

### 1.1 认证系统（必须完成）

**目标：** 系统可以安全登录使用

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| JWT 认证完整实现 | 4h | P0 |
| SecurityContext 集成 | 2h | P0 |
| 权限检查拦截器 | 2h | P0 |
| 用户注册 API | 2h | P1 |
| 密码重置流程 | 3h | P2 |

**关键文件：**
- `security/JwtAuthenticationFilter.java` - 完善
- `security/UserDetailsServiceImpl.java` - 新增
- `controller/api/v1/AuthController.java` - 完善

---

### 1.2 核心功能补全

**目标：** 消息发送/查询流程完整可用

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 完善 Narrow 查询（添加缺失操作符） | 4h | P0 |
| 实现消息软删除 | 2h | P1 |
| 实现 UserMessage 批量创建 | 2h | P0 |
| 实现 WebSocket 连接管理 | 3h | P1 |
| 实现心跳机制 | 1h | P1 |

---

### 1.3 单元测试（必须）

**目标：** 核心功能有测试保障

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| MessageService 测试 | 3h | P0 |
| NarrowService 测试 | 3h | P0 |
| UserService 测试 | 2h | P0 |
| Repository 测试 | 2h | P1 |

---

## 阶段二：生产就绪

### 2.1 异步任务系统

**目标：** 支持邮件、推送等异步操作

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 集成 Spring Async | 2h | P1 |
| 邮件发送服务 | 3h | P1 |
| 推送通知框架 | 4h | P2 |

**技术选型：**
- Spring `@Async` + ThreadPool
- 邮件：Spring Mail + Thymeleaf 模板
- 推送：预留接口，后期接入 FCM/APNs

---

### 2.2 性能优化

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| Redis 缓存集成 | 3h | P1 |
| 批量查询优化 | 2h | P1 |
| 数据库索引优化 | 2h | P1 |
| 分页性能优化 | 2h | P2 |

---

### 2.3 文件上传

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 文件上传 API | 3h | P1 |
| 文件存储服务 | 2h | P1 |
| 图片处理（缩略图） | 2h | P2 |

---

## 阶段三：功能完善

### 3.1 Emoji 反应

| 任务 | 工作量 |
|------|--------|
| Reaction 实体设计 | 1h |
| Reaction API | 2h |
| WebSocket 推送 | 1h |

### 3.2 消息引用

| 任务 | 工作量 |
|------|--------|
| 消息引用解析 | 2h |
| 引用预览渲染 | 1h |

### 3.3 输入状态

| 任务 | 工作量 |
|------|--------|
| Typing Event | 1h |
| WebSocket 广播 | 1h |

---

## 执行计划

### Week 1：让系统可用

| 天 | 任务 |
|----|------|
| Day 1-2 | JWT 认证完整实现 + SecurityContext |
| Day 3 | Narrow 查询完善 |
| Day 4 | UserMessage 批量创建 + 测试 |
| Day 5 | WebSocket 连接管理 + 测试 |

**产出：** 可以安全登录、发送消息、查询消息

---

### Week 2：生产就绪

| 天 | 任务 |
|----|------|
| Day 1 | Spring Async + 邮件服务 |
| Day 2 | Redis 缓存 + 性能优化 |
| Day 3 | 文件上传 |
| Day 4 | 更多单元测试 |
| Day 5 | 集成测试 + 文档 |

**产出：** 系统达到生产可用级别

---

## 当前最紧迫的任务

### 1. 认证系统（立即开始）

```java
// 当前的硬编码
Long userId = 1L;  // ❌ 这必须改掉

// 应该是
Long userId = SecurityContextHolder
    .getContext()
    .getAuthentication()
    .getPrincipal()
    .getUserId();
```

### 2. 测试（立即开始）

```java
// 当前
0 个测试文件  // ❌ 不可接受

// 目标
至少每个 Service 有 1 个测试类
```

### 3. TODO 清零（逐步进行）

当前 17 个 TODO 必须全部解决或转为 Issue 追踪。

---

## 质量标准

每个功能完成的标准：

1. ✅ 代码实现完成
2. ✅ 单元测试覆盖
3. ✅ 无 TODO 注释
4. ✅ API 文档完整
5. ✅ 代码审查通过

---

## 下一步行动

**立即执行：**

1. 先完善 JWT 认证系统
2. 集成 SecurityContext 到所有 Controller
3. 为 MessageService 和 NarrowService 写测试

---

*计划制定时间: 2026-04-10*