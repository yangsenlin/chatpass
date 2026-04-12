-- Message Flags (消息标记)
CREATE TABLE message_flags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    flagged_at TIMESTAMP NOT NULL,
    realm_id BIGINT,
    flag_type VARCHAR(20) DEFAULT 'star',
    
    CONSTRAINT fk_flag_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flag_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_flag_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_message_flag UNIQUE (user_id, message_id)
);

CREATE INDEX idx_flag_user ON message_flags(user_id);
CREATE INDEX idx_flag_message ON message_flags(message_id);
CREATE INDEX idx_flag_type ON message_flags(user_id, flag_type);
