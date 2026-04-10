-- 创建数据库和用户
-- 注意：此脚本需要以 superuser (postgres) 身份执行

-- 创建数据库
CREATE DATABASE chatpass;

-- 创建用户
CREATE USER chatpass WITH PASSWORD 'chatpass123';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE chatpass TO chatpass;

-- 连接到 chatpass 数据库后执行
\c chatpass;

-- 授权 schema 权限
GRANT ALL ON SCHEMA public TO chatpass;

-- 创建 schema（可选，用于 Flyway）
CREATE SCHEMA IF NOT EXISTS flyway;
GRANT ALL ON SCHEMA flyway TO chatpass;

-- 设置默认 schema
ALTER USER chatpass SET search_path TO public, flyway;