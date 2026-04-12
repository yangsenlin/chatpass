-- Topic Subscriptions (话题订阅)
CREATE TABLE topic_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stream_id BIGINT NOT NULL,
    topic VARCHAR(100) NOT NULL,
    realm_id BIGINT,
    is_muted BOOLEAN DEFAULT FALSE,
    notification_settings VARCHAR(20) DEFAULT 'all',
    subscribed_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_topic_sub_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_sub_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_sub_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_topic_subscription UNIQUE (user_id, stream_id, topic)
);

CREATE INDEX idx_topic_sub_user ON topic_subscriptions(user_id);
CREATE INDEX idx_topic_sub_stream ON topic_subscriptions(stream_id);
CREATE INDEX idx_topic_sub_topic ON topic_subscriptions(stream_id, topic);
CREATE INDEX idx_topic_sub_active ON topic_subscriptions(stream_id, topic, is_muted);
