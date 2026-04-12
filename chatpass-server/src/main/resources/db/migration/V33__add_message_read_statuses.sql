-- Message Read Statuses (消息阅读状态)
CREATE TABLE message_read_statuses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL,
    realm_id BIGINT,
    
    CONSTRAINT fk_read_status_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_read_status_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_read_status_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_message_read UNIQUE (user_id, message_id)
);

CREATE INDEX idx_read_status_user ON message_read_statuses(user_id);
CREATE INDEX idx_read_status_message ON message_read_statuses(message_id);
