-- ChatPass 数据库初始化脚本
-- 基于 Zulip 数据库设计

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- realms 表
CREATE TABLE IF NOT EXISTS realms (
    id SERIAL PRIMARY KEY,
    string_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    invite_required BOOLEAN DEFAULT true,
    create_stream_policy INTEGER DEFAULT 1,
    invite_to_stream_policy INTEGER DEFAULT 1,
    default_language VARCHAR(10) DEFAULT 'en',
    default_twenty_four_hour_time BOOLEAN DEFAULT false,
    message_content_allowed_in_email_notifications BOOLEAN DEFAULT true,
    allow_message_editing BOOLEAN DEFAULT true,
    message_edit_time_limit_seconds INTEGER DEFAULT 600,
    message_retention_days INTEGER,
    email_auth_enabled BOOLEAN DEFAULT true,
    password_auth_enabled BOOLEAN DEFAULT true,
    plan_type INTEGER DEFAULT 1,
    max_users INTEGER,
    deactivated BOOLEAN DEFAULT false,
    domain VARCHAR(255),
    custom_profile_fields JSONB DEFAULT '[]',
    default_user_settings JSONB DEFAULT '{}',
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_realms_string_id ON realms(string_id);

-- user_profiles 表
CREATE TABLE IF NOT EXISTS user_profiles (
    id SERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    api_key VARCHAR(64),
    full_name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    avatar_source INTEGER DEFAULT 0,
    avatar_url VARCHAR(512),
    avatar_version INTEGER DEFAULT 1,
    role INTEGER DEFAULT 100,
    timezone VARCHAR(50) DEFAULT 'UTC',
    default_language VARCHAR(10) DEFAULT 'en',
    delivery_email BOOLEAN DEFAULT true,
    enable_desktop_notifications BOOLEAN DEFAULT true,
    enable_sounds BOOLEAN DEFAULT true,
    enable_offline_email_notifications BOOLEAN DEFAULT true,
    enable_offline_push_notifications BOOLEAN DEFAULT true,
    enter_sends BOOLEAN DEFAULT false,
    fluid_layout_width BOOLEAN DEFAULT false,
    twenty_four_hour_time BOOLEAN DEFAULT false,
    color_scheme INTEGER DEFAULT 1,
    web_home_view VARCHAR(50) DEFAULT 'inbox',
    bot_type INTEGER,
    bot_owner_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    is_mirror_dummy BOOLEAN DEFAULT false,
    is_system_bot BOOLEAN DEFAULT false,
    is_guest BOOLEAN DEFAULT false,
    custom_profile_data JSONB DEFAULT '{}',
    last_login TIMESTAMP,
    referred_by_id BIGINT,
    invited_by_id BIGINT,
    is_billing_admin BOOLEAN DEFAULT false,
    onboarding_steps JSONB,
    date_joined TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(realm_id, email)
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_realm ON user_profiles(realm_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_profiles(email);

-- recipients 表
CREATE TABLE IF NOT EXISTS recipients (
    id SERIAL PRIMARY KEY,
    type INTEGER NOT NULL DEFAULT 1,
    stream_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_recipients_type ON recipients(type);

-- streams 表
CREATE TABLE IF NOT EXISTS streams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    creator_id BIGINT,
    deactivated BOOLEAN DEFAULT false,
    description VARCHAR(1024) DEFAULT '',
    rendered_description TEXT,
    subscriber_count INTEGER DEFAULT 0,
    invite_only BOOLEAN DEFAULT false,
    history_public_to_subscribers BOOLEAN DEFAULT true,
    is_web_public BOOLEAN DEFAULT false,
    stream_post_policy INTEGER DEFAULT 1,
    message_retention_days INTEGER,
    topics_policy INTEGER DEFAULT 1,
    recipient_id BIGINT REFERENCES recipients(id),
    folder_id BIGINT,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_streams_realm ON streams(realm_id);
CREATE INDEX IF NOT EXISTS idx_streams_name ON streams(name);

-- clients 表
CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- messages 表
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES user_profiles(id),
    recipient_id BIGINT NOT NULL REFERENCES recipients(id),
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    type INTEGER DEFAULT 1,
    subject VARCHAR(60),
    content TEXT,
    rendered_content TEXT,
    rendered_content_version INTEGER,
    date_sent TIMESTAMP,
    sending_client_id BIGINT REFERENCES clients(id),
    last_edit_time TIMESTAMP,
    edit_history TEXT,
    has_attachment BOOLEAN DEFAULT false,
    has_image BOOLEAN DEFAULT false,
    has_link BOOLEAN DEFAULT false,
    is_channel_message BOOLEAN DEFAULT true,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_recipient ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_realm ON messages(realm_id);
CREATE INDEX IF NOT EXISTS idx_messages_date_sent ON messages(date_sent);
CREATE INDEX IF NOT EXISTS idx_messages_subject ON messages(subject);

-- subscriptions 表
CREATE TABLE IF NOT EXISTS subscriptions (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    stream_id BIGINT NOT NULL REFERENCES streams(id),
    recipient_id BIGINT REFERENCES recipients(id),
    color VARCHAR(20) DEFAULT '#c2c2c2',
    pinned_to_top BOOLEAN DEFAULT false,
    desktop_notifications BOOLEAN,
    email_notifications BOOLEAN,
    push_notifications BOOLEAN,
    is_muted BOOLEAN DEFAULT false,
    in_home_view BOOLEAN DEFAULT true,
    active BOOLEAN DEFAULT true,
    last_updated TIMESTAMP,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_user_stream UNIQUE (user_profile_id, stream_id)
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user ON subscriptions(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_stream ON subscriptions(stream_id);

-- user_messages 表
CREATE TABLE IF NOT EXISTS user_messages (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    message_id BIGINT NOT NULL REFERENCES messages(id),
    flags BIGINT DEFAULT 0,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_messages_user ON user_messages(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_user_messages_message ON user_messages(message_id);

-- alert_words 表
CREATE TABLE IF NOT EXISTS alert_words (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    word VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    match_mode INTEGER DEFAULT 1,
    notify_email BOOLEAN DEFAULT false,
    notify_push BOOLEAN DEFAULT true,
    notify_desktop BOOLEAN DEFAULT true,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_user_word UNIQUE (user_profile_id, word)
);

CREATE INDEX IF NOT EXISTS idx_alert_words_user ON alert_words(user_profile_id);
CREATE INDEX IF NOT EXISTS idx_alert_words_realm ON alert_words(realm_id);