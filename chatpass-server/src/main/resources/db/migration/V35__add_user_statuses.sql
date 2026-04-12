-- User Statuses (用户状态消息)
CREATE TABLE user_statuses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status_text VARCHAR(200),
    status_emoji VARCHAR(50),
    duration_seconds INTEGER,
    expires_at TIMESTAMP,
    realm_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_status_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_status_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
);

CREATE INDEX idx_status_realm ON user_statuses(realm_id);
CREATE INDEX idx_status_expires ON user_statuses(expires_at);
