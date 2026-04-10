# PostgreSQL 初始化脚本

## 1. 安装 PostgreSQL

```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# 启动服务
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

## 2. 创建数据库和用户

```bash
# 以 postgres 用户身份登录
sudo -u postgres psql

# 执行初始化脚本
\i src/main/resources/db/init.sql
```

或者直接执行：

```bash
sudo -u postgres psql -f src/main/resources/db/init.sql
```

## 3. 验证连接

```bash
# 测试连接
psql -U chatpass -d chatpass -h localhost

# 应该能成功连接
```

## 4. 配置 Redis

```bash
# 安装 Redis
sudo apt install redis-server

# 启动 Redis
sudo systemctl start redis
sudo systemctl enable redis

# 测试连接
redis-cli ping
# 应返回 PONG
```

## 5. 启动应用

```bash
cd chatpass-server
./gradlew bootRun
```

---

## 数据库表结构

| 表名 | 说明 |
|------|------|
| realms | 组织 |
| user_profiles | 用户 |
| streams | 频道 |
| recipients | 消息接收者 |
| subscriptions | 订阅 |
| messages | 消息 |
| user_messages | 用户-消息关系 |
| reactions | 表情反应 |
| alert_words | 关键词提醒 |
| typing_events | 输入状态 |

---

## Flyway 迁移文件

- V0__baseline_schema.sql - 基础表结构
- V0.1__initial_data.sql - 初始数据
- V1__*.sql - 后续迁移

---

*更新时间: 2026-04-10*