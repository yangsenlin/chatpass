# P0 生产保护能力

本文档描述 ChatPass 当前已落地的 P0 生产保护能力：Prometheus Metrics、限流保护、连接数限制和消息大小限制。

## Prometheus Metrics

系统引入 Micrometer Prometheus registry，并通过 Actuator 暴露：

```text
GET /actuator/prometheus
```

当前核心指标：

| 指标 | 说明 |
| --- | --- |
| `chatpass_ingress_messages_total` | 标准消息入口接收数量 |
| `chatpass_webhook_messages_total` | 渠道 webhook 接收数量 |
| `chatpass_mqtt_publishes_total` | MQTT PUBLISH 成功处理数量 |
| `chatpass_mqtt_rejected_connections_total` | MQTT CONNECT 被拒绝数量 |
| `chatpass_mqtt_rejected_messages_total` | MQTT PUBLISH 被拒绝数量 |
| `chatpass_rate_limited_requests_total` | 被限流请求数量 |
| `chatpass_oversized_messages_total` | 超过大小限制的消息数量 |
| `chatpass_mqtt_active_connections` | 当前节点活跃 MQTT 连接数 |

## 限流保护

限流使用 Redis 固定窗口计数，key 前缀为：

```text
chatpass:rate-limit:
```

已接入入口：

- `POST /api/messages/ingress`
- `POST /api/tenants/{tenantId}/messages/ingress`
- `POST /api/channels/{channel}/webhook`
- `POST /api/channels/tenants/{tenantId}/{channel}/webhook`
- MQTT `PUBLISH`

超限后：

- REST 返回 `429 TOO_MANY_REQUESTS`
- MQTT 连接关闭并记录拒绝指标

## 连接数限制

MQTT `CONNECT` 阶段会检查：

- 节点总连接数：`chatpass.mqtt.max-connections`
- 租户连接数：`chatpass.mqtt.max-connections-per-tenant`

当前租户标识优先使用 MQTT `username`，缺省为 `anonymous`。

超限后返回 MQTT `CONNECTION_REFUSED_SERVER_UNAVAILABLE` 并关闭连接。

## 消息大小限制

系统在多个层面限制消息大小：

- REST ingress 根据原始 JSON body 大小校验。
- Webhook 根据原始 body 大小校验。
- TCP MQTT decoder 使用 `chatpass.mqtt.max-payload-bytes`。
- WebSocket MQTT aggregator 和 decoder 使用 `chatpass.mqtt.max-payload-bytes`。
- MQTT PUBLISH 业务处理再次校验 payload 大小。

REST 超限返回 `413 PAYLOAD_TOO_LARGE`。

## 配置示例

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus

chatpass:
  mqtt:
    max-connections: 10000
    max-connections-per-tenant: 1000
    max-payload-bytes: 1048576
    publish-rate-limit-per-minute: 6000
  protection:
    rate-limit-enabled: true
    ingress-rate-limit-per-minute: 3000
    webhook-rate-limit-per-minute: 3000
    max-message-bytes: 1048576
```

## 后续 P1

P0 只解决生产基本保护和可观测性。P1 建议继续实现：

- JWT 认证
- 审计日志
- 链路追踪
- MQTT Keep Alive 超时
- MQTT Will Message
- 数据备份恢复
