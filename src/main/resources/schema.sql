CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHECK (age BETWEEN 18 AND 120),
    bio VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_deactivation_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    swipe_encouragement_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    message_coercion_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    show_me_on_cupid BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_language VARCHAR(30) NOT NULL DEFAULT 'English',
    max_distance_km INTEGER NOT NULL DEFAULT 80 CHECK (max_distance_km BETWEEN 1 AND 500),
    preferred_min_age INTEGER NOT NULL DEFAULT 18 CHECK (preferred_min_age BETWEEN 18 AND 120),
    preferred_max_age INTEGER NOT NULL DEFAULT 60 CHECK (preferred_max_age BETWEEN 18 AND 120),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

-- Adds columns only when needed. Supports existing databases on MySQL
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

-- FR_Swipe_More_Ethics: per-user opt-out column for coercive swipe reminders.
SET @swipe_encouragement_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'swipe_encouragement_enabled'
);
SET @add_swipe_encouragement_column = IF(
    @swipe_encouragement_column_exists = 0,
    'ALTER TABLE users ADD COLUMN swipe_encouragement_enabled BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE add_swipe_encouragement_column FROM @add_swipe_encouragement_column;
EXECUTE add_swipe_encouragement_column;
DEALLOCATE PREPARE add_swipe_encouragement_column;

-- FR_Message_More_Ethics: per-user opt-out column for coercive messaging.
SET @message_coercion_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'message_coercion_enabled'
);
SET @add_message_coercion_column = IF(
    @message_coercion_column_exists = 0,
    'ALTER TABLE users ADD COLUMN message_coercion_enabled BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE add_message_coercion_column FROM @add_message_coercion_column;
EXECUTE add_message_coercion_column;
DEALLOCATE PREPARE add_message_coercion_column;

-- Non-functional preference: profile visibility on Cupid discovery.
SET @show_me_on_cupid_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'show_me_on_cupid'
);
SET @add_show_me_on_cupid_column = IF(
    @show_me_on_cupid_column_exists = 0,
    'ALTER TABLE users ADD COLUMN show_me_on_cupid BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE add_show_me_on_cupid_column FROM @add_show_me_on_cupid_column;
EXECUTE add_show_me_on_cupid_column;
DEALLOCATE PREPARE add_show_me_on_cupid_column;

-- Non-functional preference: preferred UI language.
SET @preferred_language_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'preferred_language'
);
SET @add_preferred_language_column = IF(
    @preferred_language_column_exists = 0,
    'ALTER TABLE users ADD COLUMN preferred_language VARCHAR(30) NOT NULL DEFAULT ''English''',
    'SELECT 1'
);
PREPARE add_preferred_language_column FROM @add_preferred_language_column;
EXECUTE add_preferred_language_column;
DEALLOCATE PREPARE add_preferred_language_column;

-- Non-functional preference: maximum discovery distance in kilometres.
SET @max_distance_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'max_distance_km'
);
SET @add_max_distance_column = IF(
    @max_distance_column_exists = 0,
    'ALTER TABLE users ADD COLUMN max_distance_km INTEGER NOT NULL DEFAULT 80',
    'SELECT 1'
);
PREPARE add_max_distance_column FROM @add_max_distance_column;
EXECUTE add_max_distance_column;
DEALLOCATE PREPARE add_max_distance_column;

-- Non-functional preference: preferred minimum age of discovered profiles.
SET @preferred_min_age_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'preferred_min_age'
);
SET @add_preferred_min_age_column = IF(
    @preferred_min_age_column_exists = 0,
    'ALTER TABLE users ADD COLUMN preferred_min_age INTEGER NOT NULL DEFAULT 18',
    'SELECT 1'
);
PREPARE add_preferred_min_age_column FROM @add_preferred_min_age_column;
EXECUTE add_preferred_min_age_column;
DEALLOCATE PREPARE add_preferred_min_age_column;

-- Non-functional preference: preferred maximum age of discovered profiles.
SET @preferred_max_age_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'preferred_max_age'
);
SET @add_preferred_max_age_column = IF(
    @preferred_max_age_column_exists = 0,
    'ALTER TABLE users ADD COLUMN preferred_max_age INTEGER NOT NULL DEFAULT 60',
    'SELECT 1'
);
PREPARE add_preferred_max_age_column FROM @add_preferred_max_age_column;
EXECUTE add_preferred_max_age_column;
DEALLOCATE PREPARE add_preferred_max_age_column;

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
