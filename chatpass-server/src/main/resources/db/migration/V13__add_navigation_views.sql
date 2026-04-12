-- Navigation Views Table
-- User pinned/hidden views in left sidebar

CREATE TABLE navigation_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT NOT NULL,
    fragment TEXT NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    name TEXT,
    view_type VARCHAR(50),
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uniq_user_fragment (user_profile_id, fragment(255)),
    
    CONSTRAINT fk_navigation_view_user 
        FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_navigation_view_user ON navigation_views(user_profile_id);
CREATE INDEX idx_navigation_view_pinned ON navigation_views(user_profile_id, is_pinned);