CREATE TABLE IF NOT EXISTS message_stars (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    starred_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(500)
);

-- 创建索引
CREATE INDEX idx_stars_message ON message_stars(message_id);
CREATE INDEX idx_stars_user ON message_stars(user_id);
CREATE INDEX idx_stars_time ON message_stars(starred_time);

-- 创建唯一约束（消息 + 用户）
CREATE UNIQUE INDEX uk_message_user ON message_stars(message_id, user_id);

-- 注释
COMMENT ON TABLE message_stars IS '消息收藏表 - 用户收藏的消息';
COMMENT ON COLUMN message_stars.note IS '收藏备注（可选）';
COMMENT ON COLUMN message_stars.starred_time IS '收藏时间';