CREATE TABLE IF NOT EXISTS custom_emojis (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id) ON DELETE CASCADE,
    name VARCHAR(64) NOT NULL,
    display_name VARCHAR(128),
    image_url TEXT NOT NULL,
    author_id BIGINT REFERENCES user_profiles(id) ON DELETE SET NULL,
    deactivated BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_emojis_realm ON custom_emojis(realm_id);
CREATE INDEX idx_emojis_name ON custom_emojis(name);
CREATE INDEX idx_emojis_author ON custom_emojis(author_id);

-- 创建唯一约束（realm + name + active）
CREATE UNIQUE INDEX idx_emojis_unique_name ON custom_emojis(realm_id, name) WHERE deactivated = FALSE;

-- 注释
COMMENT ON TABLE custom_emojis IS '自定义表情表 - Realm 级别的自定义 Emoji';
COMMENT ON COLUMN custom_emojis.name IS '表情名称，用于消息中引用（如 :custom_emoji:）';
COMMENT ON COLUMN custom_emojis.display_name IS '表情显示名称';
COMMENT ON COLUMN custom_emojis.image_url IS '表情图片 URL';
COMMENT ON COLUMN custom_emojis.deactivated IS '是否已删除（软删除）';