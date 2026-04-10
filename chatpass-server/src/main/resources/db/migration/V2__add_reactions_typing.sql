-- 新增表：reactions（表情反应）
CREATE TABLE IF NOT EXISTS reactions (
    id SERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    emoji_code VARCHAR(50) NOT NULL,
    emoji_name VARCHAR(100),
    emoji_type VARCHAR(20) DEFAULT 'unicode',
    reaction_type INTEGER DEFAULT 1,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_user_message_emoji UNIQUE (user_profile_id, message_id, emoji_code)
);

CREATE INDEX IF NOT EXISTS idx_reactions_message ON reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_reactions_user ON reactions(user_profile_id);

-- 新增表：typing_events（输入状态）
CREATE TABLE IF NOT EXISTS typing_events (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    recipient_id BIGINT NOT NULL REFERENCES recipients(id),
    event_type VARCHAR(10),
    topic VARCHAR(60),
    expires_at TIMESTAMP,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_typing_user ON typing_events(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_typing_recipient ON typing_events(recipient_id);

-- 更新 messages 表：添加软删除字段
ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT false;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS deleted_by_id BIGINT;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS topic_resolved_time TIMESTAMP;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS topic_resolved_by_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_messages_deleted ON messages(is_deleted);