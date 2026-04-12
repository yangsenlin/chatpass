-- Realm Domains (组织域名)
CREATE TABLE realm_domains (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(255) NOT NULL UNIQUE,
    realm_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    allow_subdomains BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'pending',
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_realm_domain_realm FOREIGN KEY (realm_id) REFERENCES realms(id)
);

CREATE INDEX idx_realm_domain_realm ON realm_domains(realm_id);
CREATE INDEX idx_realm_domain_status ON realm_domains(realm_id, status);
