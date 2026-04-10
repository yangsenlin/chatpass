-- 初始化表结构（Flyway baseline）

-- 1. Realms（组织）
CREATE TABLE IF NOT EXISTS realms (
    id SERIAL PRIMARY KEY,
    string_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. UserProfiles（用户）
CREATE TABLE IF NOT EXISTS user_profiles (
    id SERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    email VARCHAR(254) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(200),
    api_key VARCHAR(64),
    timezone VARCHAR(80),
    default_language VARCHAR(10),
    enable_desktop_notifications BOOLEAN DEFAULT true,
    enable_sounds BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    is_bot BOOLEAN DEFAULT false,
    is_admin BOOLEAN DEFAULT false,
    password_hash VARCHAR(255),
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_users_realm ON user_profiles(realm_id);

-- 3. Streams（频道）
CREATE TABLE IF NOT EXISTS streams (
    id SERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    name VARCHAR(60) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT true,
    invite_only BOOLEAN DEFAULT false,
    history_public_to_subscribers BOOLEAN DEFAULT true,
    order_value BIGINT DEFAULT 0,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_streams_realm ON streams(realm_id);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_stream_name ON streams(realm_id, name);

-- 4. Recipients（消息接收者抽象）
CREATE TABLE IF NOT EXISTS recipients (
    id SERIAL PRIMARY KEY,
    type INTEGER NOT NULL,
    stream_id BIGINT REFERENCES streams(id)
);

CREATE INDEX IF NOT EXISTS idx_recipients_stream ON recipients(stream_id);
CREATE INDEX IF NOT EXISTS idx_recipients_type ON recipients(type);

-- 5. Subscriptions（订阅）
CREATE TABLE IF NOT EXISTS subscriptions (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    stream_id BIGINT NOT NULL REFERENCES streams(id),
    active BOOLEAN DEFAULT true,
    is_muted BOOLEAN DEFAULT false,
    pin_to_top BOOLEAN DEFAULT false,
    color VARCHAR(10),
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_user_stream UNIQUE (user_profile_id, stream_id)
);

CREATE INDEX IF NOT EXISTS idx_subs_user ON subscriptions(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_subs_stream ON subscriptions(stream_id);

-- 6. Messages（消息）
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    sender_id BIGINT NOT NULL REFERENCES user_profiles(id),
    recipient_id BIGINT NOT NULL REFERENCES recipients(id),
    subject VARCHAR(60),
    content TEXT NOT NULL,
    rendered_content TEXT,
    date_sent TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_edit_time TIMESTAMP,
    edit_history JSONB,
    has_attachment BOOLEAN DEFAULT false,
    has_image BOOLEAN DEFAULT false,
    has_link BOOLEAN DEFAULT false,
    is_channel_message BOOLEAN DEFAULT false,
    deleted BOOLEAN DEFAULT false,
    search_tsvector TSVECTOR
);

CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_messages_realm ON messages(realm_id);
CREATE INDEX IF NOT EXISTS idx_messages_tsvector ON messages USING GIN(search_tsvector);

-- 全文搜索触发器
CREATE OR REPLACE FUNCTION update_message_search_vector() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_tsvector := to_tsvector('english', coalesce(NEW.content, '') || ' ' || coalesce(NEW.subject, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER message_search_update
    BEFORE INSERT OR UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_message_search_vector();

-- 7. UserMessages（用户-消息关系）
CREATE TABLE IF NOT EXISTS user_messages (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    message_id BIGINT NOT NULL REFERENCES messages(id),
    flags BIGINT DEFAULT 0,
    timestamp_read TIMESTAMP,
    CONSTRAINT uniq_user_message UNIQUE (user_profile_id, message_id)
);

CREATE INDEX IF NOT EXISTS idx_user_messages_user ON user_messages(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_user_messages_message ON user_messages(message_id);

-- 8. Reactions（表情反应）
CREATE TABLE IF NOT EXISTS reactions (
    id SERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_profiles(id),
    emoji_code VARCHAR(30) NOT NULL,
    emoji_name VARCHAR(60),
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reactions_message ON reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_reactions_user ON reactions(user_id);

-- 9. AlertWords（关键词提醒）
CREATE TABLE IF NOT EXISTS alert_words (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    word VARCHAR(60) NOT NULL,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alert_words_user ON alert_words(user_profile_id);

-- 10. TypingEvents（输入状态）
CREATE TABLE IF NOT EXISTS typing_events (
    id SERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES user_profiles(id),
    recipient_id BIGINT NOT NULL REFERENCES recipients(id),
    topic VARCHAR(60),
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_typing_sender ON typing_events(sender_id);
CREATE INDEX IF NOT EXISTS idx_typing_recipient ON typing_events(recipient_id);