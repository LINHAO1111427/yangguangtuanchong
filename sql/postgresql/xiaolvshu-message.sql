-- =============================================
-- Message module tables (PostgreSQL, NO DATA LOSS)
-- Safe to re-run: uses IF NOT EXISTS
-- =============================================

-- 1) Private messages
CREATE TABLE IF NOT EXISTS message_private (
  id BIGSERIAL PRIMARY KEY,
  from_user_id BIGINT NOT NULL,
  to_user_id BIGINT NOT NULL,
  type INTEGER NOT NULL DEFAULT 1,
  content TEXT NULL,
  extra_data TEXT NULL,
  status INTEGER NOT NULL DEFAULT 0,
  deleted INTEGER NOT NULL DEFAULT 0,
  read_time TIMESTAMPTZ NULL,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_message_private_from_user ON message_private(from_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_private_to_user ON message_private(to_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_private_conversation ON message_private(from_user_id, to_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_private_status ON message_private(status, create_time DESC);

-- 2) Conversations
CREATE TABLE IF NOT EXISTS message_conversation (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  type INTEGER NOT NULL DEFAULT 1,
  last_message_id BIGINT NULL,
  last_message_content VARCHAR(500) NULL,
  last_message_time TIMESTAMPTZ NULL,
  unread_count INTEGER NOT NULL DEFAULT 0,
  is_top INTEGER NOT NULL DEFAULT 0,
  is_mute INTEGER NOT NULL DEFAULT 0,
  deleted INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_message_conversation UNIQUE (user_id, target_id, type)
);

CREATE INDEX IF NOT EXISTS idx_conversation_user ON message_conversation(user_id, last_message_time DESC);
CREATE INDEX IF NOT EXISTS idx_conversation_user_type ON message_conversation(user_id, type, last_message_time DESC);

-- 3) Group info
CREATE TABLE IF NOT EXISTS group_info (
  id BIGSERIAL PRIMARY KEY,
  group_name VARCHAR(64) NOT NULL,
  avatar VARCHAR(512) NULL,
  owner_user_id BIGINT NOT NULL,
  announcement VARCHAR(500) NULL,
  description VARCHAR(500) NULL,
  member_count INTEGER NOT NULL DEFAULT 1,
  max_member_count INTEGER NOT NULL DEFAULT 200,
  join_type INTEGER NOT NULL DEFAULT 0,
  status INTEGER NOT NULL DEFAULT 0,
  mute_all INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  dissolve_time TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_group_info_owner ON group_info(owner_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_group_info_status ON group_info(status, update_time DESC);

-- 4) Group members
CREATE TABLE IF NOT EXISTS group_member (
  id BIGSERIAL PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  nickname VARCHAR(64) NULL,
  role INTEGER NOT NULL DEFAULT 3,
  status INTEGER NOT NULL DEFAULT 0,
  muted INTEGER NOT NULL DEFAULT 0,
  mute_end_time TIMESTAMPTZ NULL,
  join_time TIMESTAMPTZ NULL,
  quit_time TIMESTAMPTZ NULL,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_group_member UNIQUE (group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_group_member_group ON group_member(group_id, status, update_time DESC);
CREATE INDEX IF NOT EXISTS idx_group_member_user ON group_member(user_id, update_time DESC);

-- 5) Group messages
CREATE TABLE IF NOT EXISTS group_message (
  id BIGSERIAL PRIMARY KEY,
  group_id BIGINT NOT NULL,
  from_user_id BIGINT NOT NULL,
  type INTEGER NOT NULL DEFAULT 1,
  content TEXT NULL,
  extra_data TEXT NULL,
  status INTEGER NOT NULL DEFAULT 0,
  deleted INTEGER NOT NULL DEFAULT 0,
  recall_time TIMESTAMPTZ NULL,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_group_message_group ON group_message(group_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_group_message_from_user ON group_message(from_user_id, create_time DESC);

-- 6) Notifications (system)
CREATE TABLE IF NOT EXISTS message_notification (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type INTEGER NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  related_data TEXT NULL,
  link VARCHAR(500) NULL,
  is_read INTEGER NOT NULL DEFAULT 0,
  read_time TIMESTAMPTZ NULL,
  deleted INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  update_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON message_notification(user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON message_notification(user_id, is_read, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_notification_type ON message_notification(type, create_time DESC);
