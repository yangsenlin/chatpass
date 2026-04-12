-- User Mutes (用户静音)
CREATE TABLE user_mutes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    muted_user_id BIGINT NOT NULL,
    realm_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_mute_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mute_muted_user FOREIGN KEY (muted_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mute_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_mute UNIQUE (user_id, muted_user_id)
);

CREATE INDEX idx_mute_user ON user_mutes(user_id);
CREATE INDEX idx_mute_muted_user ON user_mutes(muted_user_id);
