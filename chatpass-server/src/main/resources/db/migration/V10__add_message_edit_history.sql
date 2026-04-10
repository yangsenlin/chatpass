CREATE TABLE IF NOT EXISTS message_edit_history (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    editor_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    prev_content TEXT,
    prev_topic VARCHAR(60),
    new_content TEXT,
    new_topic VARCHAR(60),
    edit_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edit_type INTEGER NOT NULL DEFAULT 1
);

-- 创建索引
CREATE INDEX idx_edit_history_message ON message_edit_history(message_id);
CREATE INDEX idx_edit_history_editor ON message_edit_history(editor_id);
CREATE INDEX idx_edit_history_time ON message_edit_history(edit_time);

-- 注释
COMMENT ON TABLE message_edit_history IS '消息编辑历史表 - 记录消息的每次编辑';
COMMENT ON COLUMN message_edit_history.edit_type IS '编辑类型: 1=content, 2=topic, 3=both';
COMMENT ON COLUMN message_edit_history.prev_content IS '编辑前的内容';
COMMENT ON COLUMN message_edit_history.new_content IS '编辑后的内容';
COMMENT ON COLUMN message_edit_history.prev_topic IS '编辑前的 Topic';
COMMENT ON COLUMN message_edit_history.new_topic IS '编辑后的 Topic';