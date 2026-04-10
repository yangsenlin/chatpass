-- V8__add_user_groups.sql
-- 用户组管理表

CREATE TABLE IF NOT EXISTS user_groups (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT unique_realm_group_name UNIQUE (realm_id, name)
);

CREATE TABLE IF NOT EXISTS user_group_memberships (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES user_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_group_user UNIQUE (group_id, user_id)
);

-- 索引
CREATE INDEX idx_user_groups_realm ON user_groups(realm_id);
CREATE INDEX idx_user_groups_name ON user_groups(name);
CREATE INDEX idx_user_group_memberships_group ON user_group_memberships(group_id);
CREATE INDEX idx_user_group_memberships_user ON user_group_memberships(user_id);

COMMENT ON TABLE user_groups IS '用户组定义';
COMMENT ON TABLE user_group_memberships IS '用户-组关联';

-- 默认系统组（为 realm_id=1）
INSERT INTO user_groups (realm_id, name, description, is_system) VALUES
(1, 'Administrators', '管理员组', true),
(1, 'Moderators', '版主组', true),
(1, 'Members', '成员组', true);

-- 将第一个用户加入管理员组
INSERT INTO user_group_memberships (group_id, user_id) VALUES
(1, 1);