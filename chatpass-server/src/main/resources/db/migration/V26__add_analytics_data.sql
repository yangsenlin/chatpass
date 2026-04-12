-- Analytics Data (数据分析)
CREATE TABLE analytics_data (
    id BIGSERIAL PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    period VARCHAR(20),
    timestamp TIMESTAMP NOT NULL,
    metric_value BIGINT,
    details TEXT,
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_analytics_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
);

CREATE INDEX idx_analytics_realm ON analytics_data(realm_id, timestamp DESC);
CREATE INDEX idx_analytics_type ON analytics_data(realm_id, data_type, timestamp DESC);
CREATE INDEX idx_analytics_period ON analytics_data(realm_id, period, timestamp DESC);
