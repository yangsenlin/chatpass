-- Realm Filters Table (Linkifiers)
-- Auto-link text patterns in messages

CREATE TABLE realm_filters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    pattern TEXT NOT NULL,
    url_template TEXT NOT NULL,
    example_input TEXT,
    reverse_template TEXT,
    alternative_url_templates JSON,
    order INT NOT NULL DEFAULT 0,
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uniq_realm_pattern (realm_id, pattern(255)),
    
    CONSTRAINT fk_realm_filter_realm 
        FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_realm_filter_realm ON realm_filters(realm_id);
CREATE INDEX idx_realm_filter_order ON realm_filters(realm_id, order);