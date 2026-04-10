-- 插入初始数据

-- 默认 Realm（组织）
INSERT INTO realms (string_id, name, description) 
VALUES ('chatpass', 'ChatPass Demo', 'ChatPass 演示组织')
ON CONFLICT (string_id) DO NOTHING;

-- 获取 realm_id
DO $$
DECLARE
    realm_id_var BIGINT;
BEGIN
    SELECT id INTO realm_id_var FROM realms WHERE string_id = 'chatpass';
    
    -- 创建测试用户
    INSERT INTO user_profiles (realm_id, email, full_name, password_hash, is_admin, is_active)
    VALUES 
        (realm_id_var, 'admin@chatpass.com', 'Admin', 'hashed_password_placeholder', true, true),
        (realm_id_var, 'test@chatpass.com', 'Test User', 'hashed_password_placeholder', false, true)
    ON CONFLICT (email) DO NOTHING;
END $$;

-- 创建默认 Stream（频道）
DO $$
DECLARE
    realm_id_var BIGINT;
BEGIN
    SELECT id INTO realm_id_var FROM realms WHERE string_id = 'chatpass';
    
    INSERT INTO streams (realm_id, name, description, is_public, order_value)
    VALUES 
        (realm_id_var, 'general', 'General discussion', true, 1),
        (realm_id_var, 'announce', 'Announcements', true, 2),
        (realm_id_var, 'team', 'Team discussion', false, 3)
    ON CONFLICT (realm_id, name) DO NOTHING;
END $$;

-- 创建 Recipients
INSERT INTO recipients (type, stream_id)
SELECT 1, id FROM streams WHERE name IN ('general', 'announce', 'team')
ON CONFLICT DO NOTHING;