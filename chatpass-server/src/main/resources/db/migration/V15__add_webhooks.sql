CREATE TABLE IF NOT EXISTS webhooks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    webhook_key VARCHAR(64) NOT NULL UNIQUE,
    webhook_url VARCHAR(500) NOT NULL,
    owner_id BIGINT NOT NULL,
    realm_id BIGINT NOT NULL,
    bot_id BIGINT,
    target_stream_id BIGINT,
    default_topic VARCHAR(60),
    event_types TEXT,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    request_method VARCHAR(10) DEFAULT 'POST',
    request_headers TEXT,
    request_body_template TEXT,
    retry_count INTEGER DEFAULT 3,
    retry_interval INTEGER DEFAULT 5,
    last_invoked TIMESTAMP,
    last_result VARCHAR(20),
    invoke_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS webhook_logs (
    id BIGSERIAL PRIMARY KEY,
    webhook_id BIGINT NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    event_type VARCHAR(50),
    event_data TEXT,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    request_headers TEXT,
    request_body TEXT,
    response_status INTEGER,
    response_body TEXT,
    result VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    retry_attempt INTEGER DEFAULT 0,
    invoke_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_ms BIGINT
);

-- 创建索引
CREATE INDEX idx_webhook_owner ON webhooks(owner_id);
CREATE INDEX idx_webhook_realm ON webhooks(realm_id);
CREATE INDEX idx_webhook_key ON webhooks(webhook_key);
CREATE INDEX idx_webhook_bot ON webhooks(bot_id);
CREATE INDEX idx_log_webhook ON webhook_logs(webhook_id);
CREATE INDEX idx_log_time ON webhook_logs(invoke_time);
CREATE INDEX idx_log_result ON webhook_logs(result);

-- 注释
COMMENT ON TABLE webhooks IS 'Webhook 表 - 外部系统集成';
COMMENT ON COLUMN webhooks.webhook_key IS 'Webhook Key（用于验证调用）';
COMMENT ON COLUMN webhooks.event_types IS '订阅的事件类型列表（JSON 数组）';
COMMENT ON COLUMN webhooks.target_stream_id IS '目标 Stream ID（可选，用于发送消息）';
COMMENT ON TABLE webhook_logs IS 'Webhook 调用日志表';
COMMENT ON COLUMN webhook_logs.result IS '调用结果: SUCCESS, FAILURE, TIMEOUT';