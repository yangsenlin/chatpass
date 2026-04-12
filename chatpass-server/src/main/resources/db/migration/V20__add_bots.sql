-- Bots (机器人)
CREATE TABLE bots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    bot_type VARCHAR(20) DEFAULT 'generic',
    api_key VARCHAR(100) NOT NULL UNIQUE,
    realm_id BIGINT NOT NULL,
    owner_id BIGINT,
    avatar_url VARCHAR(500),
    description VARCHAR(500),
    webhook_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_bot_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bot_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT fk_bot_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_bot_realm ON bots(realm_id, is_active);
CREATE INDEX idx_bot_api_key ON bots(api_key);
CREATE INDEX idx_bot_owner ON bots(owner_id);
CREATE INDEX idx_bot_user ON bots(user_id);

-- 添加 UserProfile 的 is_bot 字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_bot BOOLEAN DEFAULT FALSE;
