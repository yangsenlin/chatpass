-- Message Drafts (消息草稿)
CREATE TABLE message_drafts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stream_id BIGINT,
    to_user_ids VARCHAR(500),
    topic VARCHAR(100),
    content TEXT,
    type VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_draft_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_draft_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE CASCADE
);

CREATE INDEX idx_draft_user ON message_drafts(user_id);
CREATE INDEX idx_draft_stream ON message_drafts(user_id, stream_id);
CREATE INDEX idx_draft_updated ON message_drafts(user_id, updated_at DESC);
