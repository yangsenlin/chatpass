-- Audit Logs (审计日志)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    realm_id BIGINT,
    object_type VARCHAR(50),
    object_id BIGINT,
    extra_data TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    result VARCHAR(20) DEFAULT 'success',
    error_message VARCHAR(500),
    
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_realm ON audit_logs(realm_id, event_time DESC);
CREATE INDEX idx_audit_actor ON audit_logs(actor_id, event_time DESC);
CREATE INDEX idx_audit_event ON audit_logs(event_type, event_time DESC);
CREATE INDEX idx_audit_object ON audit_logs(object_type, object_id, event_time DESC);
