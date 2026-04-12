-- Pinned Messages (固定消息)
CREATE TABLE pinned_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    stream_id BIGINT,
    topic VARCHAR(100),
    realm_id BIGINT NOT NULL,
    pinned_by BIGINT,
    pinned_at TIMESTAMP NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_expired BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP,
    
    CONSTRAINT fk_pinned_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_pinned_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE CASCADE,
    CONSTRAINT fk_pinned_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT fk_pinned_by FOREIGN KEY (pinned_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_pinned_message UNIQUE (message_id)
);

CREATE INDEX idx_pinned_realm ON pinned_messages(realm_id, is_expired, sort_order);
CREATE INDEX idx_pinned_stream ON pinned_messages(stream_id, is_expired, sort_order);
CREATE INDEX idx_pinned_topic ON pinned_messages(stream_id, topic, is_expired, sort_order);
