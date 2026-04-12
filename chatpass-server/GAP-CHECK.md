# ChatPass vs Zulip 详细差距核对

## 核对时间：2026-04-12 00:15

---

## 一、已实现功能对照表

| Zulip Model | ChatPass Entity | 状态 | 备注 |
|-------------|-----------------|------|------|
| AlertWord | AlertWord | ✅ 完整 | |
| Client | Client | ✅ 完整 | |
| CustomProfileField | CustomProfileField | ✅ 完整 | 新增 |
| CustomProfileFieldValue | CustomProfileFieldValue | ✅ 完整 | 新增 |
| Draft | Draft | ✅ 完整 | |
| UserGroup | UserGroup | ✅ 完整 | |
| UserGroupMembership | UserGroupMembership | ✅ 完整 | |
| UserPresence | UserPresence | ✅ 完整 | |
| UserTopic | UserTopic | ✅ 完整 | |
| Realm | Realm | ✅ 完整 | |
| Recipient | Recipient | ✅ 完整 | |
| Stream | Stream | ✅ 完整 | |
| Subscription | Subscription | ✅ 完整 | |
| UserProfile | UserProfile | ✅ 完整 | |
| Message | Message | ✅ 完整 | |
| Reaction | Reaction | ✅ 完整 | (AbstractEmoji) |
| UserMessage | UserMessage | ✅ 完整 | |
| Attachment | Attachment | ✅ 完整 | |
| Bot | Bot | ✅ 部分 | 缺 Service/BotConfig |
| RealmEmoji | CustomEmoji | ✅ 完整 | |
| UserPresence | UserPresence | ✅ 完整 | |
| AbstractRealmAuditLog | AuditLog | ✅ 部分 | 结构简化 |
| ScheduledMessage | QueuedMessage | ⚠️ 部分 | 功能简化 |
| AbstractPushDeviceToken | PushNotification/MobilePush | ⚠️ 部分 | 结构简化 |

---

## 二、缺失功能详细核对（按优先级）

### 🔴 高优先级（核心功能）

#### 1. Device 设备管理
**Zulip 文件**: `devices.py`
**类**: `Device`
**功能**: 多设备管理、E2EE推送加密
**依赖**: UserProfile
**复杂度**: 高（涉及加密）
**状态**: ❌ 未实现

#### 2. RealmDomain 组织域名
**Zulip 文件**: `realms.py`
**类**: `RealmDomain`
**功能**: 组织自定义域名管理
**依赖**: Realm
**复杂度**: 中
**状态**: ❌ 未实现

#### 3. ScheduledMessage 定时消息
**Zulip 文件**: `scheduled_jobs.py`
**类**: `ScheduledMessage`
**功能**: 定时发送消息、提醒
**依赖**: Message, UserProfile, Stream
**复杂度**: 中
**状态**: ⚠️ 简化实现（QueuedMessage）

#### 4. MutedUser 用户屏蔽
**Zulip 文件**: `muted_users.py`
**类**: `MutedUser`
**功能**: 屏蔽特定用户消息
**依赖**: UserProfile
**复杂度**: 低
**状态**: ❌ 未实现

#### 5. ChannelFolder 频道分类
**Zulip 文件**: `channel_folders.py`
**类**: `ChannelFolder`
**功能**: 频道文件夹/分类
**依赖**: Realm, Stream
**复杂度**: 中
**状态**: ❌ 未实现

---

### 🟡 中优先级（增强功能）

#### 6. GroupGroupMembership 子群组
**Zulip 文件**: `groups.py`
**类**: `GroupGroupMembership`
**功能**: 用户组的嵌套关系
**依赖**: UserGroup
**复杂度**: 低
**状态**: ❌ 未实现

#### 7. SavedSnippet 保存片段
**Zulip 文件**: `saved_snippets.py`
**类**: `SavedSnippet`
**功能**: 预设回复片段
**依赖**: UserProfile, Realm
**复杂度**: 低
**状态**: ❌ 未实现

#### 8. Linkifier 链接转换器
**Zulip 文件**: `linkifiers.py`
**类**: `RealmFilter`
**功能**: 自动链接转换规则
**依赖**: Realm
**复杂度**: 低
**状态**: ❌ 未实现

#### 9. UserActivity 用户活动
**Zulip 文件**: `user_activity.py`
**类**: `UserActivity`, `UserActivityInterval`
**功能**: 用户活动记录、统计分析
**依赖**: UserProfile
**复杂度**: 中
**状态**: ❌ 未实现

#### 10. NavigationView 导航视图
**Zulip 文件**: `navigation_views.py`
**类**: `NavigationView`
**功能**: 用户导航状态保存
**依赖**: UserProfile
**复杂度**: 低
**状态**: ❌ 未实现

---

### 🟢 低优先级（辅助功能）

#### 11. ExternalAuthID 外部认证
**Zulip 文件**: `users.py`
**类**: `ExternalAuthID`
**功能**: 外部身份认证集成
**依赖**: UserProfile
**复杂度**: 中
**状态**: ❌ 未实现

#### 12. OnboardingStep 引导步骤
**Zulip 文件**: `onboarding_steps.py`
**类**: `OnboardingStep`
**功能**: 新用户引导流程
**依赖**: UserProfile
**复杂度**: 低
**状态**: ❌ 未实现

#### 13. PresenceSequence 在线序列
**Zulip 文件**: `presence.py`
**类**: `PresenceSequence`
**功能**: 在线状态序列号
**依赖**: Realm
**复杂度**: 低
**状态**: ❌ 未实现

#### 14. RealmPlayground 运行环境
**Zulip 文件**: `realm_playgrounds.py`
**类**: `RealmPlayground`
**功能**: 代码运行环境配置
**依赖**: Realm
**复杂度**: 中
**状态**: ❌ 未实现

#### 15. RealmExport 导出记录
**Zulip 文件**: `realms.py`
**类**: `RealmExport`
**功能**: 组织数据导出历史
**依赖**: Realm
**复杂度**: 中
**状态**: ❌ 未实现

---

## 三、Bot 系统缺失功能

**Zulip 文件**: `bots.py`
**缺失类**:
- `Service` - Bot服务配置
- `BotStorageData` - Bot存储数据
- `BotConfigData` - Bot配置数据

**ChatPass 状态**: 有 Bot, BotCommand，缺少复杂配置

---

## 四、消息系统缺失功能

**Zulip 文件**: `messages.py`
**缺失类**:
- `ArchiveTransaction` - 归档事务（⚠️ ChatPass 有 MessageArchive）
- `AbstractSubMessage` - 子消息基类
- `ImageAttachment` - 图片附件详情
- `OnboardingUserMessage` - 引导消息

---

## 五、预注册系统缺失功能

**Zulip 文件**: `prereg_users.py`
**缺失类**:
- `PreregistrationRealm` - 预注册组织
- `PreregistrationUser` - 预注册用户
- `MultiuseInvite` - 多次使用邀请（⚠️ ChatPass 有 RealmInvite）
- `EmailChangeStatus` - 邮箱变更状态
- `RealmReactivationStatus` - 组织重新激活
- `RealmCreationStatus` - 组织创建状态

---

## 六、Recipient 系统缺失功能

**Zulip 文件**: `recipients.py`
**缺失类**:
- `DirectMessageGroup` - 私聊群组

---

## 七、认证系统缺失功能

**Zulip 文件**: `realms.py`
**缺失类**:
- `RealmAuthenticationMethod` - 认证方法配置

---

## 八、实现建议（按批次）

### 批次一：低复杂度高价值（5个）
1. **MutedUser** - 用户屏蔽（低复杂度）
2. **SavedSnippet** - 保存片段（低复杂度）
3. **Linkifier/RealmFilter** - 链接转换器（低复杂度）
4. **NavigationView** - 导航视图（低复杂度）
5. **GroupGroupMembership** - 子群组（低复杂度）

### 批次二：中复杂度核心功能（3个）
1. **ScheduledMessage** - 定时消息增强
2. **ChannelFolder** - 频道分类
3. **RealmDomain** - 组织域名

### 批次三：高复杂度功能（2个）
1. **Device** - 设备管理（涉及加密）
2. **UserActivity** - 用户活动追踪

---

## 九、统计数据

| 分类 | 数量 |
|------|------|
| **已完整实现** | 21个 |
| **部分实现** | 5个 |
| **高优先级缺失** | 5个 |
| **中优先级缺失** | 5个 |
| **低优先级缺失** | 5个 |
| **Bot系统缺失** | 3个类 |
| **消息系统缺失** | 4个类 |
| **预注册系统缺失** | 6个类 |
| **其他缺失** | 8个类 |

**总缺失类数**: 约36个

---

*核对完成时间: 2026-04-12 00:15*
*核对人: 随行*