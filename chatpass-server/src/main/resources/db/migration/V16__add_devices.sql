-- Devices (设备管理)
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_type VARCHAR(20),
    device_name VARCHAR(100),
    os VARCHAR(50),
    browser VARCHAR(50),
    ip_address VARCHAR(45),
    device_id VARCHAR(100) UNIQUE,
    last_active TIMESTAMP,
    last_login TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    push_notifications_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_device_user ON devices(user_id);
CREATE INDEX idx_device_last_active ON devices(user_id, last_active);
CREATE INDEX idx_device_current ON devices(user_id, is_current);
