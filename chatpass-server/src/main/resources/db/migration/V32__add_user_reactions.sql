-- User Reactions (用户反应)
CREATE TABLE user_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type VARCHAR(50) NOT NULL,
    reacted_at TIMESTAMP NOT NULL,
    realm_id BIGINT,
    
    CONSTRAINT fk_reaction_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_message_user_reaction UNIQUE (message_id, user_id, reaction_type)
);

CREATE INDEX idx_reaction_message ON user_reactions(message_id);
CREATE INDEX idx_reaction_user ON user_reactions(user_id);
CREATE INDEX idx_reaction_type ON user_reactions(message_id, reaction_type);
