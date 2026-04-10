-- V7__add_permissions.sql
-- 权限系统表

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role INTEGER NOT NULL,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    
    CONSTRAINT unique_role_permission UNIQUE (role, permission_id)
);

-- 索引
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_category ON permissions(category);
CREATE INDEX idx_role_permissions_role ON role_permissions(role);

COMMENT ON TABLE permissions IS '权限定义';
COMMENT ON TABLE role_permissions IS '角色-权限关联';

-- 默认权限数据
INSERT INTO permissions (code, name, description, category) VALUES
('send_message', '发送消息', '允许发送消息', 'message'),
('delete_message', '删除消息', '允许删除消息', 'message'),
('edit_message', '编辑消息', '允许编辑消息', 'message'),
('create_stream', '创建频道', '允许创建频道', 'stream'),
('manage_streams', '管理频道', '允许管理频道', 'stream'),
('manage_users', '管理用户', '允许管理用户', 'user'),
('admin_all', '超级管理员', '所有权限', 'admin');

-- 默认角色权限分配
-- 普通用户（100）
INSERT INTO role_permissions (role, permission_id) VALUES
(100, 1), -- send_message
(100, 3); -- edit_message

-- 版主（200）
INSERT INTO role_permissions (role, permission_id) VALUES
(200, 1), -- send_message
(200, 2), -- delete_message
(200, 3), -- edit_message
(200, 4); -- create_stream

-- 管理员（300）
INSERT INTO role_permissions (role, permission_id) VALUES
(300, 1), -- send_message
(300, 2), -- delete_message
(300, 3), -- edit_message
(300, 4), -- create_stream
(300, 5), -- manage_streams
(300, 6), -- manage_users
(300, 7); -- admin_all

-- 所有者（400）
INSERT INTO role_permissions (role, permission_id) VALUES
(400, 7); -- admin_all