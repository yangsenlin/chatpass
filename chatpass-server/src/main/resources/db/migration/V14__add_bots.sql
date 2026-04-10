CREATE TABLE IF NOT EXISTS bots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bot_type VARCHAR(20) NOT NULL DEFAULT 'OUTGOING',
    bot_user_id BIGINT NOT NULL UNIQUE,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    realm_id BIGINT NOT NULL,
    avatar_url VARCHAR(500),
    config TEXT,
    endpoint_url VARCHAR(500),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bot_commands (
    id BIGSERIAL PRIMARY KEY,
    bot_id BIGINT NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
    command_name VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    handler VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT true,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_bot_owner ON bots(owner_id);
CREATE INDEX idx_bot_realm ON bots(realm_id);
CREATE INDEX idx_bot_api_key ON bots(api_key);
CREATE INDEX idx_bot_user ON bots(bot_user_id);
CREATE INDEX idx_cmd_bot ON bot_commands(bot_id);
CREATE UNIQUE INDEX uk_bot_name_realm ON bots(name, realm_id);

-- 注释
COMMENT ON TABLE bots IS '机器人表 - 系统机器人用户';
COMMENT ON COLUMN bots.bot_type IS 'Bot 类型: OUTGOING(发送消息), INCOMING(接收消息), GENERIC(通用)';
COMMENT ON COLUMN bots.api_key IS 'Bot API Key（用于调用 API）';
COMMENT ON COLUMN bots.endpoint_url IS 'Outgoing Bot 的服务端点 URL';
COMMENT ON TABLE bot_commands IS 'Bot 命令表 - Bot 可处理的命令';
COMMENT ON COLUMN bot_commands.command_name IS '命令名称（如 /weather）';
COMMENT ON COLUMN bot_commands.handler IS '命令处理器（方法名或 URL）';