-- Typing Statuses (输入状态)
CREATE TABLE typing_statuses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stream_id BIGINT,
    topic VARCHAR(100),
    to_user_ids VARCHAR(500),
    realm_id BIGINT,
    started_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_typing_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_typing_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE CASCADE,
    CONSTRAINT fk_typing_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
);

CREATE INDEX idx_typing_user ON typing_statuses(user_id);
CREATE INDEX idx_typing_stream ON typing_statuses(stream_id);
CREATE INDEX idx_typing_topic ON typing_statuses(stream_id, topic);
