CREATE TABLE IF NOT EXISTS message_queues (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    queue_name VARCHAR(60) NOT NULL UNIQUE,
    queue_type VARCHAR(20) NOT NULL,
    broker_url VARCHAR(200) NOT NULL,
    exchange_name VARCHAR(60),
    routing_key VARCHAR(60),
    is_active BOOLEAN DEFAULT true,
    max_retry INTEGER NOT NULL DEFAULT 3,
    retry_delay_seconds INTEGER DEFAULT 30,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS queued_messages (
    id BIGSERIAL PRIMARY KEY,
    queue_id BIGINT NOT NULL REFERENCES message_queues(id) ON DELETE CASCADE,
    message_id BIGINT NOT NULL,
    payload TEXT,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    error_message VARCHAR(500),
    date_queued TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_sent TIMESTAMP,
    last_retry TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mobile_push_configs (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    push_type VARCHAR(20) NOT NULL,
    project_id VARCHAR(100),
    api_key VARCHAR(200),
    sender_id VARCHAR(100),
    certificate_path VARCHAR(200),
    is_active BOOLEAN DEFAULT true,
    batch_size INTEGER DEFAULT 100,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS push_notifications (
    id BIGSERIAL PRIMARY KEY,
    push_config_id BIGINT NOT NULL REFERENCES mobile_push_configs(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    message_id BIGINT,
    device_token VARCHAR(200) NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    title VARCHAR(100),
    body TEXT,
    data_payload TEXT,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(500),
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_sent TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message_archives (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    archive_id VARCHAR(50) NOT NULL UNIQUE,
    message_id BIGINT NOT NULL,
    original_content TEXT,
    compressed_content TEXT,
    storage_path VARCHAR(200),
    storage_type VARCHAR(20),
    archive_reason VARCHAR(30),
    archive_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_date TIMESTAMP,
    is_deleted BOOLEAN DEFAULT false,
    restore_count INTEGER DEFAULT 0,
    last_restored TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message_translations (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    target_language VARCHAR(10) NOT NULL,
    original_text TEXT,
    translated_text TEXT,
    translation_provider VARCHAR(30),
    translation_status VARCHAR(20),
    confidence_score DOUBLE PRECISION,
    translator_id BIGINT,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_queue_realm ON message_queues(realm_id);
CREATE INDEX idx_queue_name ON message_queues(queue_name);
CREATE INDEX idx_queue_active ON message_queues(is_active);
CREATE INDEX idx_queued_msg_queue ON queued_messages(queue_id);
CREATE INDEX idx_queued_msg_status ON queued_messages(status);
CREATE INDEX idx_push_config_realm ON mobile_push_configs(realm_id);
CREATE INDEX idx_push_config_type ON mobile_push_configs(push_type);
CREATE INDEX idx_push_notification_user ON push_notifications(user_id);
CREATE INDEX idx_push_notification_status ON push_notifications(status);
CREATE INDEX idx_archive_realm ON message_archives(realm_id);
CREATE INDEX idx_archive_msg ON message_archives(message_id);
CREATE INDEX idx_archive_archive_id ON message_archives(archive_id);
CREATE INDEX idx_translation_msg ON message_translations(message_id);
CREATE INDEX idx_translation_lang ON message_translations(target_language);
CREATE INDEX idx_translation_status ON message_translations(translation_status);

-- 注释
COMMENT ON TABLE message_queues IS '消息队列配置表';
COMMENT ON COLUMN message_queues.queue_type IS '队列类型: RABBITMQ、KAFKA、SQS';
COMMENT ON TABLE queued_messages IS '队列消息记录表';
COMMENT ON COLUMN queued_messages.status IS '状态: PENDING、SENT、FAILED、RETRYING';
COMMENT ON TABLE mobile_push_configs IS '移动推送配置表';
COMMENT ON COLUMN mobile_push_configs.push_type IS '推送类型: FCM、APNS';
COMMENT ON TABLE push_notifications IS '推送通知记录表';
COMMENT ON COLUMN push_notifications.notification_type IS '通知类型: MESSAGE、MENTION、STREAM、PRIVATE';
COMMENT ON TABLE message_archives IS '消息归档表';
COMMENT ON COLUMN message_archives.storage_type IS '存储类型: DATABASE、FILE、S3';
COMMENT ON COLUMN message_archives.archive_reason IS '归档原因: EXPIRED、DELETED、USER_REQUEST、POLICY';
COMMENT ON TABLE message_translations IS '消息翻译表';
COMMENT ON COLUMN message_translations.translation_provider IS '翻译服务: GOOGLE、DEEP_L、AZURE、MANUAL';