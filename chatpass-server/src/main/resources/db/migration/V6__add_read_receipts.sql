-- V6__add_read_receipts.sql
-- 阅读回执表

CREATE TABLE IF NOT EXISTS read_receipts (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_message_user_read UNIQUE (message_id, user_id)
);

-- 索引
CREATE INDEX idx_read_receipts_message ON read_receipts(message_id);
CREATE INDEX idx_read_receipts_user ON read_receipts(user_id);
CREATE INDEX idx_read_receipts_read_at ON read_receipts(read_at);

COMMENT ON TABLE read_receipts IS '消息阅读回执';
COMMENT ON COLUMN read_receipts.message_id IS '消息 ID';
COMMENT ON COLUMN read_receipts.user_id IS '阅读用户 ID';
COMMENT ON COLUMN read_receipts.read_at IS '阅读时间';