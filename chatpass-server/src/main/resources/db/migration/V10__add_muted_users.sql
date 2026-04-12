-- Muted Users Table
-- User muting functionality

CREATE TABLE muted_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    muted_user_id BIGINT NOT NULL,
    date_muted DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uniq_muter_muted (user_profile_id, muted_user_id),
    
    CONSTRAINT fk_muted_user_user 
        FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_muted_user_muted 
        FOREIGN KEY (muted_user_id) REFERENCES user_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for queries
CREATE INDEX idx_muted_user_user ON muted_users(user_profile_id);
CREATE INDEX idx_muted_user_muted ON muted_users(muted_user_id);