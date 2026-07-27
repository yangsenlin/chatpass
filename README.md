# ChatPass - 消息处理与路由平台

ChatPass V1 原型实现了消息标准化、触发规则匹配、动作执行和输出适配的最小闭环。

## 当前能力

- 统一消息接收：通过 `POST /api/messages/ingress` 接收 MQTT、Facebook、WhatsApp、LINE、Instagram 或 API 标准化消息。
- 触发器规则引擎：支持启用状态、优先级、ALL/ANY 条件组合，以及 `CONTAINS`、`EQUALS`、`IN`、`REGEX` 等匹配操作。
- 工作流动作执行：已支持 `AUTO_REPLY`、`DIFY_CHAT`、`TRANSFER_TO_AGENT` 和 `NOOP`。
- DAG 工作流引擎：支持 Start/End/Action/Condition/Wait/Loop/HTTP 节点、边条件、ANY/ALL/FIRST 合流、异步节点提交、节点输出变量、挂起状态和恢复执行。
- MQTT broker：基于 Netty MQTT codec 实现 TCP/WebSocket MQTT 接入、CONNECT、SUBSCRIBE、UNSUBSCRIBE、PUBLISH、PING、DISCONNECT、QoS1 ACK、QoS2 基础握手、retain 消息、订阅分发、离线消息和集群总线抽象。
- Dify 对接：复用 `dify-api-java-sdk`，配置 `chatpass.dify.enabled=true` 后可调用 Dify Chat App。
- 输出适配层：当前为内存 outbox，可通过 `GET /api/outbox` 查看，后续可替换为 MQTT、reachlink 或 Redis 总线。

## 启动

```bash
mvn spring-boot:run
```

## 消息入站示例

```bash
curl -X POST http://localhost:8080/api/messages/ingress \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "m-10001",
    "channel": "MQTT",
    "senderId": "visitor-1",
    "receiverId": "bot",
    "conversationId": "c-1",
    "text": "hello ai",
    "attributes": {
      "org": "demo",
      "app": "webim"
    }
  }'
```

## 规则管理

- `GET /api/rules`：查询规则。
- `GET /api/rules/{id}`：查询单条规则。
- `POST /api/rules`：新增或更新规则。
- `DELETE /api/rules/{id}`：删除规则。

## 工作流管理

- `GET /api/workflows`：查询工作流。
- `POST /api/workflows`：新增或更新 DAG 工作流定义。
- `POST /api/workflows/{id}/execute`：执行工作流。
- `POST /api/workflows/executions/{executionId}/resume`：恢复 WAIT 挂起的工作流执行。
- `DELETE /api/workflows/{id}`：删除工作流。

内置默认工作流 `default-auto-reply`，结构为 `start -> reply(AUTO_REPLY) -> end`。
保存工作流时会做流程校验：节点 ID 唯一、边端点存在、存在起点、图无环。

## MQTT Broker

默认配置：

```yaml
chatpass:
  mqtt:
    enabled: true
    node-id: chatpass-node-1
    host: 0.0.0.0
    port: 1883
    websocket-enabled: true
    websocket-port: 8083
    websocket-path: /mqtt
    cluster-enabled: true
    offline-enabled: true
    bridge-ingress: true
```

客户端发布的 MQTT PUBLISH 会被转换为 `UnifiedMessage` 并进入 `MessageIngressService`。topic 会写入 `attributes.mqtt.topic`，payload 默认按 UTF-8 文本处理。
WebSocket MQTT 入口默认监听 `ws://localhost:8083/mqtt`，子协议为 `mqtt`。`cleanSession=false` 的客户端断线后会保留订阅，QoS1/QoS2 消息会进入离线消息存储，并在下次 CONNECT 后回放。

## 设计映射

- `message` 包对应统一消息接收层的标准化消息模型。
- `routing` 包对应路由中枢和触发器匹配引擎。
- `workflow` 包对应动作执行抽象和 MoJarvis 风格 DAG 节点执行。
- `mqtt` 包对应 kefu-gateway MQTT broker 主协议链路。
- `output` 包对应输出渠道适配层。

## 后续扩展

- MQTT 生产化：将当前本地事件集群总线替换为 Redis/Kafka，将内存 session/subscription/retain/offline store 替换为 Redis/DB，并补齐 kefu-gateway 的 JWT payload、EasemobMessage 编解码。
- reachlink 对接：将其海外渠道消息转换为 `UnifiedMessage` 后投递到 `MessageIngressService`。
- DAG 工作流生产化：将当前内存工作流定义和执行上下文替换为数据库持久化，并接入可视化编排器。
