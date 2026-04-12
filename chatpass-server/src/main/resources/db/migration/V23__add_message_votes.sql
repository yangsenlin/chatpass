-- Message Votes (消息投票)
CREATE TABLE message_votes (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(20) DEFAULT 'upvote',
    voted_at TIMESTAMP NOT NULL,
    realm_id BIGINT,
    
    CONSTRAINT fk_vote_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_realm FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE,
    CONSTRAINT uq_message_user_vote UNIQUE (message_id, user_id)
);

CREATE INDEX idx_vote_message ON message_votes(message_id);
CREATE INDEX idx_vote_user ON message_votes(user_id);
CREATE INDEX idx_vote_type ON message_votes(message_id, vote_type);
