-- Custom Emojis (自定义表情)
CREATE TABLE custom_emojis (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    aliases VARCHAR(500),
    image_url VARCHAR(500) NOT NULL,
    image_path VARCHAR(500),
    realm_id BIGINT NOT NULL,
    author_id BIGINT,
    deactivated BOOLEAN DEFAULT FALSE,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_custom_emoji_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT fk_custom_emoji_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_custom_emoji_name UNIQUE (realm_id, name)
);

CREATE INDEX idx_custom_emoji_realm ON custom_emojis(realm_id, deactivated);
CREATE INDEX idx_custom_emoji_usage ON custom_emojis(realm_id, usage_count DESC);
CREATE INDEX idx_custom_emoji_author ON custom_emojis(author_id);
