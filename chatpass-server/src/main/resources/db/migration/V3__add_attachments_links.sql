-- 新增表：attachments（文件附件）
CREATE TABLE IF NOT EXISTS attachments (
    id SERIAL PRIMARY KEY,
    message_id BIGINT REFERENCES messages(id),
    owner_id BIGINT NOT NULL REFERENCES user_profiles(id),
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255),
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    file_type INTEGER DEFAULT 1,
    width INTEGER,
    height INTEGER,
    thumbnail_path VARCHAR(512),
    thumbnail_url VARCHAR(512),
    url VARCHAR(512),
    path_id VARCHAR(255),
    is_deleted BOOLEAN DEFAULT false,
    is_ready BOOLEAN DEFAULT true,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attachments_message ON attachments(message_id);
CREATE INDEX IF NOT EXISTS idx_attachments_owner ON attachments(owner_id);

-- 新增表：message_links（消息引用）
CREATE TABLE IF NOT EXISTS message_links (
    id SERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    target_message_id BIGINT NOT NULL REFERENCES messages(id),
    link_type VARCHAR(20) DEFAULT 'reference',
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_message_target UNIQUE (message_id, target_message_id)
);

CREATE INDEX IF NOT EXISTS idx_message_links_message ON message_links(message_id);
CREATE INDEX IF NOT EXISTS idx_message_links_target ON message_links(target_message_id);