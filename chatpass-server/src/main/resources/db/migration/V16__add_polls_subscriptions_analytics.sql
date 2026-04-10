CREATE TABLE IF NOT EXISTS message_polls (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    question VARCHAR(200) NOT NULL,
    options TEXT NOT NULL,
    poll_type VARCHAR(20) DEFAULT 'SINGLE',
    is_anonymous BOOLEAN DEFAULT false,
    allow_change BOOLEAN DEFAULT true,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'OPEN',
    total_votes BIGINT DEFAULT 0,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS poll_votes (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES message_polls(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    option_index INTEGER NOT NULL,
    option_text VARCHAR(200),
    vote_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_cancelled BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS topic_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stream_id BIGINT NOT NULL,
    topic_name VARCHAR(60) NOT NULL,
    subscription_type VARCHAR(20) DEFAULT 'NOTIFY',
    desktop_notifications BOOLEAN DEFAULT true,
    email_notifications BOOLEAN DEFAULT false,
    push_notifications BOOLEAN DEFAULT true,
    sound_notifications BOOLEAN DEFAULT true,
    date_subscribed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP,
    CONSTRAINT uk_topic_subscription UNIQUE (user_id, stream_id, topic_name)
);

CREATE TABLE IF NOT EXISTS analytics_reports (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    period VARCHAR(20) DEFAULT 'DAILY',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    report_data TEXT,
    summary TEXT,
    creator_id BIGINT,
    report_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_poll_message ON message_polls(message_id);
CREATE INDEX idx_poll_status ON message_polls(status);
CREATE INDEX idx_poll_creator ON message_polls(creator_id);
CREATE INDEX idx_vote_poll ON poll_votes(poll_id);
CREATE INDEX idx_vote_user ON poll_votes(user_id);
CREATE INDEX idx_vote_option ON poll_votes(option_index);
CREATE INDEX idx_topic_sub_user ON topic_subscriptions(user_id);
CREATE INDEX idx_topic_sub_stream ON topic_subscriptions(stream_id);
CREATE INDEX idx_topic_sub_topic ON topic_subscriptions(topic_name);
CREATE INDEX idx_report_realm ON analytics_reports(realm_id);
CREATE INDEX idx_report_type ON analytics_reports(report_type);
CREATE INDEX idx_report_time ON analytics_reports(report_time);

-- 注释
COMMENT ON TABLE message_polls IS '消息投票表';
COMMENT ON COLUMN message_polls.poll_type IS '投票类型: SINGLE（单选）、MULTIPLE（多选）';
COMMENT ON COLUMN message_polls.status IS '投票状态: OPEN、CLOSED、ENDED';
COMMENT ON TABLE poll_votes IS '投票记录表';
COMMENT ON TABLE topic_subscriptions IS '话题订阅表';
COMMENT ON COLUMN topic_subscriptions.subscription_type IS '订阅类型: NOTIFY、MENTION_ONLY、MUTE';
COMMENT ON TABLE analytics_reports IS '数据分析报告表';
COMMENT ON COLUMN analytics_reports.report_type IS '报告类型: USER_ACTIVITY、STREAM_USAGE、MESSAGE_STATS、REACTION_STATS';