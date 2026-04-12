-- Message Archives (消息归档)
CREATE TABLE message_archives (
    id BIGSERIAL PRIMARY KEY,
    original_message_id BIGINT,
    content TEXT,
    rendered_content TEXT,
    sender_id BIGINT,
    stream_id BIGINT,
    topic VARCHAR(100),
    realm_id BIGINT NOT NULL,
    original_date_sent TIMESTAMP,
    archived_at TIMESTAMP NOT NULL,
    archive_policy VARCHAR(50),
    archived_by BIGINT,
    is_recoverable BOOLEAN DEFAULT TRUE,
    recover_until TIMESTAMP,
    
    CONSTRAINT fk_archive_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_archive_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE SET NULL,
    CONSTRAINT fk_archive_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT fk_archive_by FOREIGN KEY (archived_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_archive_realm ON message_archives(realm_id, archived_at DESC);
CREATE INDEX idx_archive_stream ON message_archives(stream_id, archived_at DESC);
CREATE INDEX idx_archive_original ON message_archives(original_message_id);
CREATE INDEX idx_archive_recoverable ON message_archives(realm_id, is_recoverable, recover_until);
