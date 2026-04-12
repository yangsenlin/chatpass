-- User Groups (用户组)
CREATE TABLE user_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    realm_id BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_user_group_realm FOREIGN KEY (realm_id) REFERENCES realms(id),
    CONSTRAINT fk_user_group_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uq_user_group_name UNIQUE (realm_id, name)
);

CREATE INDEX idx_user_group_realm ON user_groups(realm_id);
CREATE INDEX idx_user_group_public ON user_groups(realm_id, is_public);

-- User Group Members (组成员)
CREATE TABLE user_group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'member',
    is_owner BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_group_member UNIQUE (group_id, user_id)
);

CREATE INDEX idx_group_member_group ON user_group_members(group_id);
CREATE INDEX idx_group_member_user ON user_group_members(user_id);
CREATE INDEX idx_group_member_owner ON user_group_members(group_id, is_owner);
