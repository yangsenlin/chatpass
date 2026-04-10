CREATE TABLE IF NOT EXISTS typing_status (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    recipient_id BIGINT REFERENCES recipients(id) ON DELETE CASCADE,
    stream_id BIGINT,
    topic VARCHAR(60),
    typing_type VARCHAR(20) NOT NULL DEFAULT 'direct',
    last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_in INTEGER NOT NULL DEFAULT 30
);

-- 创建索引
CREATE INDEX idx_typing_user ON typing_status(user_id);
CREATE INDEX idx_typing_recipient ON typing_status(recipient_id);
CREATE INDEX idx_typing_time ON typing_status(last_update);
CREATE INDEX idx_typing_stream_topic ON typing_status(stream_id, topic);

-- 注释
COMMENT ON TABLE typing_status IS '输入提示状态表 - 记录用户正在输入的状态';
COMMENT ON COLUMN typing_status.typing_type IS '输入类型: direct=私信, stream=频道';
COMMENT ON COLUMN typing_status.expires_in IS '状态过期时间（秒）';
COMMENT ON COLUMN typing_status.last_update IS '最后更新时间（用于判断是否仍在输入）';