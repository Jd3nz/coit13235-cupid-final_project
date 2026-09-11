CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHECK (age BETWEEN 18 AND 120),
    bio VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_deactivation_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

-- Adds the column only when needed. This supports existing databases on MySQL
-- versions that do not accept ALTER TABLE ... ADD COLUMN IF NOT EXISTS.
SET @account_deactivation_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'account_deactivation_enabled'
);
SET @add_account_deactivation_column = IF(
    @account_deactivation_column_exists = 0,
    'ALTER TABLE users ADD COLUMN account_deactivation_enabled BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE add_account_deactivation_column FROM @add_account_deactivation_column;
EXECUTE add_account_deactivation_column;
DEALLOCATE PREPARE add_account_deactivation_column;

CREATE TABLE IF NOT EXISTS current_swipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    swiper_id BIGINT NOT NULL REFERENCES users(id),
    target_user_id BIGINT NOT NULL REFERENCES users(id),
    decision VARCHAR(10) NOT NULL CHECK (decision IN ('LIKE', 'DISLIKE')),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_current_swipe UNIQUE (swiper_id, target_user_id),
    CONSTRAINT chk_current_swipe_not_self CHECK (swiper_id <> target_user_id),
    INDEX idx_current_swipes_swiper (swiper_id),
    INDEX idx_current_swipes_target_decision (target_user_id, decision)
);

CREATE TABLE IF NOT EXISTS swipe_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    swiper_id BIGINT NOT NULL REFERENCES users(id),
    target_user_id BIGINT NOT NULL REFERENCES users(id),
    decision VARCHAR(10) NOT NULL CHECK (decision IN ('LIKE', 'DISLIKE')),
    swiped_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_swipe_history_not_self CHECK (swiper_id <> target_user_id),
    INDEX idx_swipe_history_swiper_time (swiper_id, swiped_at DESC)
);

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_one_id BIGINT NOT NULL REFERENCES users(id),
    user_two_id BIGINT NOT NULL REFERENCES users(id),
    matched_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'UNMATCHED')),
    CONSTRAINT uq_match_pair UNIQUE (user_one_id, user_two_id),
    CONSTRAINT chk_match_order CHECK (user_one_id < user_two_id),
    INDEX idx_matches_user_one (user_one_id),
    INDEX idx_matches_user_two (user_two_id)
);

-- FR_Messages_History: store messages between existing users.
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_text VARCHAR(2000) NOT NULL,
    sent_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(id),

    CONSTRAINT fk_messages_receiver
        FOREIGN KEY (receiver_id) REFERENCES users(id),

    INDEX idx_messages_conversation
        (sender_id, receiver_id, sent_at, id)
 );
CREATE TABLE IF NOT EXISTS profile_pictures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    primary_picture BOOLEAN NOT NULL DEFAULT FALSE,
    data LONGBLOB NOT NULL,
    uploaded_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_profile_pictures_user (user_id),
    INDEX idx_profile_pictures_primary (user_id, primary_picture)
);
