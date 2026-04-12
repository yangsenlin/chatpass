-- User Presences (用户在线状态)
CREATE TABLE user_presences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'offline',
    status_message VARCHAR(200),
    last_active TIMESTAMP,
    last_offline TIMESTAMP,
    realm_id BIGINT,
    updated_at TIMESTAMP,
    push_notifications BOOLEAN DEFAULT TRUE,
    show_offline BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_presence_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_presence_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
);

CREATE INDEX idx_presence_realm ON user_presences(realm_id);
CREATE INDEX idx_presence_status ON user_presences(realm_id, status);
CREATE INDEX idx_presence_active ON user_presences(realm_id, last_active DESC);
