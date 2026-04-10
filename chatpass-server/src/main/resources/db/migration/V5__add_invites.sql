-- 添加邀请系统表

CREATE TABLE IF NOT EXISTS realm_invites (
    id SERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL REFERENCES realms(id),
    invited_by_user_id BIGINT NOT NULL REFERENCES user_profiles(id),
    invite_link VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(254),
    status INTEGER NOT NULL DEFAULT 1,
    expires_at TIMESTAMP,
    accepted_at TIMESTAMP,
    accepted_user_id BIGINT REFERENCES user_profiles(id),
    max_uses INTEGER DEFAULT 1,
    current_uses INTEGER DEFAULT 0,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_invites_realm ON realm_invites(realm_id);
CREATE INDEX IF NOT EXISTS idx_invites_link ON realm_invites(invite_link);
CREATE INDEX IF NOT EXISTS idx_invites_status ON realm_invites(status);

-- 状态说明：
-- 1 = PENDING（待使用）
-- 2 = ACCEPTED（已使用）
-- 3 = EXPIRED（已过期）
-- 4 = REVOKED（已撤销）