-- Realm Settings (组织配置)
CREATE TABLE realm_settings (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(20) DEFAULT 'string',
    description VARCHAR(500),
    editable BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_settings_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_realm_setting UNIQUE (realm_id, setting_key)
);

CREATE INDEX idx_settings_realm ON realm_settings(realm_id);
CREATE INDEX idx_settings_key ON realm_settings(realm_id, setting_key);
CREATE INDEX idx_settings_public ON realm_settings(realm_id, is_public);
