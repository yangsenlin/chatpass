-- Stream Permissions (Stream权限)
CREATE TABLE stream_permissions (
    id BIGSERIAL PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission_type VARCHAR(20) DEFAULT 'member',
    can_read BOOLEAN DEFAULT TRUE,
    can_write BOOLEAN DEFAULT TRUE,
    can_modify_topic BOOLEAN DEFAULT TRUE,
    can_manage_members BOOLEAN DEFAULT FALSE,
    can_delete_messages BOOLEAN DEFAULT FALSE,
    realm_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_permission_stream FOREIGN KEY (stream_id) REFERENCES streams(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_stream_permission UNIQUE (stream_id, user_id)
);

CREATE INDEX idx_permission_stream ON stream_permissions(stream_id);
CREATE INDEX idx_permission_user ON stream_permissions(user_id);
CREATE INDEX idx_permission_type ON stream_permissions(stream_id, permission_type);
