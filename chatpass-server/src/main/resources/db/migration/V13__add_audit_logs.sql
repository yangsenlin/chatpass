CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100),
    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(500),
    resource_type VARCHAR(50),
    resource_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    realm_id BIGINT,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_id VARCHAR(100)
);

-- 创建索引
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_time ON audit_logs(event_time);
CREATE INDEX idx_audit_type ON audit_logs(event_type);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_realm ON audit_logs(realm_id);
CREATE INDEX idx_audit_result ON audit_logs(result);
CREATE INDEX idx_audit_request ON audit_logs(request_id);

-- 注释
COMMENT ON TABLE audit_logs IS '审计日志表 - 记录系统中的所有重要操作';
COMMENT ON COLUMN audit_logs.event_type IS '事件类型: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, SEND, etc.';
COMMENT ON COLUMN audit_logs.resource_type IS '资源类型: MESSAGE, STREAM, USER, etc.';
COMMENT ON COLUMN audit_logs.result IS '操作结果: SUCCESS or FAILURE';
COMMENT ON COLUMN audit_logs.old_value IS '操作前数据（JSON 格式）';
COMMENT ON COLUMN audit_logs.new_value IS '操作后数据（JSON 格式）';
COMMENT ON COLUMN audit_logs.request_id IS '请求 ID（用于追踪链）';