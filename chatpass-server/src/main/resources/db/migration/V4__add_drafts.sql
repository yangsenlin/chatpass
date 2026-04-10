-- 新增表：drafts（消息草稿）
CREATE TABLE IF NOT EXISTS drafts (
    id SERIAL PRIMARY KEY,
    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
    recipient_id BIGINT REFERENCES recipients(id),
    topic VARCHAR(60),
    content TEXT,
    last_edit_time TIMESTAMP,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_user_recipient_topic UNIQUE (user_profile_id, recipient_id, topic)
);

CREATE INDEX IF NOT EXISTS idx_drafts_user ON drafts(user_profile_id);