-- Saved Snippets Table
-- User saved message snippets for quick replies

CREATE TABLE saved_snippets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    realm_id BIGINT NOT NULL,
    user_profile_id BIGINT NOT NULL,
    title VARCHAR(60) NOT NULL,
    content TEXT NOT NULL,
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_saved_snippet_realm 
        FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_snippet_user 
        FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for queries
CREATE INDEX idx_saved_snippet_user ON saved_snippets(user_profile_id);
CREATE INDEX idx_saved_snippet_realm ON saved_snippets(realm_id);
CREATE INDEX idx_saved_snippet_created ON saved_snippets(date_created);