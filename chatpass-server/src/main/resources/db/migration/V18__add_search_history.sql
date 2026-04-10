CREATE TABLE IF NOT EXISTS search_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    realm_id BIGINT NOT NULL,
    query VARCHAR(200) NOT NULL,
    search_type VARCHAR(20),
    results_count INTEGER,
    date_searched TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_search_history_user ON search_histories(user_id);
CREATE INDEX idx_search_history_realm ON search_histories(realm_id);
CREATE INDEX idx_search_history_date ON search_histories(date_searched);

COMMENT ON TABLE search_histories IS '用户搜索历史记录表';
COMMENT ON COLUMN search_histories.search_type IS '搜索类型: MESSAGES、STREAMS、USERS、GLOBAL';