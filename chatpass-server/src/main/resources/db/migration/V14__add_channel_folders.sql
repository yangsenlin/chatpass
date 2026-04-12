-- Channel Folders (频道分类)
CREATE TABLE channel_folders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    realm_id BIGINT NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_default BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_channel_folder_realm FOREIGN KEY (realm_id) REFERENCES realms(id),
    CONSTRAINT fk_channel_folder_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_channel_folder_realm ON channel_folders(realm_id);
CREATE INDEX idx_channel_folder_sort ON channel_folders(realm_id, sort_order);
