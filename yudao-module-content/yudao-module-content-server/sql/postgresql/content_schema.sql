-- =====================================================
-- Content Module - PostgreSQL Database Schema
-- Generated for xiaolvshu content module
-- =====================================================

-- Drop existing tables if exist
DROP TABLE IF EXISTS content_favorite_record CASCADE;
DROP TABLE IF EXISTS content_favorite_group CASCADE;
DROP TABLE IF EXISTS content_ad CASCADE;
DROP TABLE IF EXISTS content_interaction CASCADE;
DROP TABLE IF EXISTS content_comment CASCADE;
DROP TABLE IF EXISTS content_post CASCADE;
DROP TABLE IF EXISTS content_channel_user CASCADE;
DROP TABLE IF EXISTS content_channel CASCADE;
DROP TABLE IF EXISTS content_topic CASCADE;

-- Drop sequences
DROP SEQUENCE IF EXISTS content_favorite_record_seq;
DROP SEQUENCE IF EXISTS content_favorite_group_seq;
DROP SEQUENCE IF EXISTS content_ad_seq;
DROP SEQUENCE IF EXISTS content_interaction_seq;
DROP SEQUENCE IF EXISTS content_comment_seq;
DROP SEQUENCE IF EXISTS content_post_seq;
DROP SEQUENCE IF EXISTS content_channel_user_seq;
DROP SEQUENCE IF EXISTS content_channel_seq;
DROP SEQUENCE IF EXISTS content_topic_seq;

-- =====================================================
-- 1. content_topic (璇濋琛?
-- =====================================================
CREATE SEQUENCE content_topic_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_topic (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_topic_seq'),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(255),
    cover VARCHAR(255),
    type INTEGER DEFAULT 0,  -- 璇濋绫诲瀷锛?=鏅€?1=娲诲姩 2=鎸戞垬
    color VARCHAR(20),
    sort INTEGER DEFAULT 0,
    is_recommend INTEGER DEFAULT 0,  -- 鏄惁鎺ㄨ崘锛?=鍚?1=鏄?
    status INTEGER DEFAULT 1,  -- 鐘舵€侊細0=绂佺敤 1=鍚敤
    participant_count INTEGER DEFAULT 0,  -- 鍙備笌浜烘暟
    content_count INTEGER DEFAULT 0,  -- 鍐呭鏁伴噺
    today_content_count INTEGER DEFAULT 0,  -- 浠婃棩鍐呭鏁?
    hot_score DOUBLE PRECISION DEFAULT 0,  -- 鐑害鍒嗘暟
    tags JSONB,  -- 鏍囩鏁扮粍
    extra JSONB,  -- 棰濆淇℃伅
    creator_id BIGINT,  -- 鍒涘缓鑰匢D
    last_active_time TIMESTAMP,  -- 鏈€鍚庢椿璺冩椂闂?

    -- BaseDO 瀛楁
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE  -- 閫昏緫鍒犻櫎锛欶ALSE=鏈垹闄?TRUE=宸插垹闄?
);

CREATE INDEX idx_topic_status ON content_topic(status);
CREATE INDEX idx_topic_recommend ON content_topic(is_recommend);
CREATE INDEX idx_topic_hot_score ON content_topic(hot_score DESC);
CREATE INDEX idx_topic_deleted ON content_topic(deleted);

COMMENT ON TABLE content_topic IS 'Topic table';
COMMENT ON COLUMN content_topic.type IS 'Topic type: 0=normal 1=event 2=challenge';
COMMENT ON COLUMN content_topic.is_recommend IS 'Is recommended: 0=no 1=yes';
COMMENT ON COLUMN content_topic.status IS 'Status: 0=disabled 1=enabled';

-- =====================================================
-- 2. content_channel (鍐呭棰戦亾閰嶇疆)
-- =====================================================
CREATE SEQUENCE content_channel_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_channel (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_channel_seq'),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(255),
    color VARCHAR(32),
    sort INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    is_default SMALLINT DEFAULT 0,
    is_required SMALLINT DEFAULT 0,
    keyword_hints JSONB,
    extra JSONB,
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_content_channel_code ON content_channel(code);
CREATE INDEX idx_content_channel_status ON content_channel(status);
CREATE INDEX idx_content_channel_sort ON content_channel(sort);

COMMENT ON TABLE content_channel IS 'Content channel config';
COMMENT ON COLUMN content_channel.is_default IS 'Default for new users: 0=no 1=yes';
COMMENT ON COLUMN content_channel.is_required IS 'Required channel (cannot remove): 0=no 1=yes';

-- =====================================================
-- 3. content_post (鍐呭鍙戝竷琛?
-- =====================================================
CREATE SEQUENCE content_post_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_post (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_post_seq'),
    user_id BIGINT NOT NULL,
    user_type INTEGER DEFAULT 1,  -- 鐢ㄦ埛绫诲瀷
    content_type INTEGER DEFAULT 1,  -- 鍐呭绫诲瀷锛?=鍥炬枃 2=瑙嗛 3=闊抽
    title VARCHAR(200),
    content TEXT,
    publish_topic_id BIGINT,  -- 鍙戝竷璇濋ID
    channel_id BIGINT,  -- 棰戦亾ID
    channel_name VARCHAR(100),  -- 棰戦亾鍚嶇О蹇収
    images JSONB,  -- 鍥剧墖鏁扮粍
    video_url VARCHAR(500),
    video_cover VARCHAR(500),
    video_duration INTEGER,  -- 瑙嗛鏃堕暱(绉?
    video_width INTEGER,
    video_height INTEGER,
    video_file_size BIGINT,
    video_format VARCHAR(20),
    video_quality INTEGER,
    audio_duration INTEGER,  -- 闊抽鏃堕暱(绉?
    is_public INTEGER DEFAULT 1,  -- 鏄惁鍏紑锛?=绉佸瘑 1=鍏紑
    status INTEGER DEFAULT 0,  -- 鐘舵€侊細0=鑽夌 1=宸插彂甯?2=宸插垹闄?
    audit_status INTEGER DEFAULT 0,  -- 瀹℃牳鐘舵€侊細0=寰呭鏍?1=閫氳繃 2=鎷掔粷
    audit_remark VARCHAR(500),
    auditor_id BIGINT,
    allow_comment INTEGER DEFAULT 1,  -- 鍏佽璇勮锛?=鍚?1=鏄?
    allow_download INTEGER DEFAULT 0,  -- 鍏佽涓嬭浇锛?=鍚?1=鏄?
    is_top INTEGER DEFAULT 0,  -- 鏄惁缃《
    is_hot INTEGER DEFAULT 0,  -- 鏄惁鐑棬
    is_recommend INTEGER DEFAULT 0,  -- 鏄惁鎺ㄨ崘
    view_count INTEGER DEFAULT 0,  -- 娴忚閲?
    like_count INTEGER DEFAULT 0,  -- 鐐硅禐鏁?
    comment_count INTEGER DEFAULT 0,  -- 璇勮鏁?
    share_count INTEGER DEFAULT 0,  -- 鍒嗕韩鏁?
    collect_count INTEGER DEFAULT 0,  -- 鏀惰棌鏁?
    forward_count INTEGER DEFAULT 0,  -- 杞彂鏁?
    completion_rate DOUBLE PRECISION DEFAULT 0,  -- 瀹屾垚鐜?
    avg_watch_time INTEGER DEFAULT 0,  -- 骞冲潎瑙傜湅鏃堕暱
    last_play_time TIMESTAMP,  -- 鏈€鍚庢挱鏀炬椂闂?
    publish_time TIMESTAMP,  -- 鍙戝竷鏃堕棿
    hot_score DOUBLE PRECISION DEFAULT 0,  -- 鐑害鍒嗘暟
    recommend_score DOUBLE PRECISION DEFAULT 0,  -- 鎺ㄨ崘鍒嗘暟
    tags JSONB,  -- 鏍囩鏁扮粍
    extra JSONB,  -- 棰濆淇℃伅
    cover_image VARCHAR(500),  -- 灏侀潰鍥?
    summary VARCHAR(500),  -- 鎽樿

    -- BaseDO 瀛楁
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_post_user_id ON content_post(user_id);
CREATE INDEX idx_post_channel_id ON content_post(channel_id);
CREATE INDEX idx_post_topic_id ON content_post(publish_topic_id);
CREATE INDEX idx_post_status ON content_post(status);
CREATE INDEX idx_post_audit_status ON content_post(audit_status);
CREATE INDEX idx_post_publish_time ON content_post(publish_time DESC);
CREATE INDEX idx_post_hot_score ON content_post(hot_score DESC);
CREATE INDEX idx_post_deleted ON content_post(deleted);

COMMENT ON TABLE content_post IS 'Content post';
COMMENT ON COLUMN content_post.content_type IS 'Content type: 1=picture 2=video 3=audio';
COMMENT ON COLUMN content_post.status IS 'Status: 0=draft 1=published 2=deleted';
COMMENT ON COLUMN content_post.audit_status IS 'Audit status: 0=pending 1=approved 2=rejected';

-- =====================================================
-- 4. content_channel_user (鐢ㄦ埛棰戦亾璁剧疆)
-- =====================================================
CREATE SEQUENCE content_channel_user_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_channel_user (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_channel_user_seq'),
    user_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    display_order INTEGER DEFAULT 0,
    pinned SMALLINT DEFAULT 0,
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_content_channel_user ON content_channel_user(user_id, channel_id) WHERE deleted = false;
CREATE INDEX idx_content_channel_user_order ON content_channel_user(user_id, display_order);

COMMENT ON TABLE content_channel_user IS 'User channel preference';
COMMENT ON COLUMN content_channel_user.pinned IS 'Pinned in my channels: 0=no 1=yes';

-- =====================================================
-- 5. content_comment (璇勮琛?
-- =====================================================
CREATE SEQUENCE content_comment_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_comment (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_comment_seq'),
    content_id BIGINT NOT NULL,  -- 鍐呭ID
    user_id BIGINT NOT NULL,  -- 璇勮鐢ㄦ埛ID
    parent_id BIGINT DEFAULT 0,  -- 鐖惰瘎璁篒D锛?琛ㄧず涓€绾ц瘎璁?
    root_id BIGINT DEFAULT 0,  -- 鏍硅瘎璁篒D
    reply_user_id BIGINT,  -- 鍥炲鐨勭敤鎴稩D
    content TEXT NOT NULL,  -- 璇勮鍐呭
    images JSONB,  -- 鍥剧墖鏁扮粍
    is_anonymous INTEGER DEFAULT 0,  -- 鏄惁鍖垮悕锛?=鍚?1=鏄?
    like_count INTEGER DEFAULT 0,  -- 鐐硅禐鏁?
    reply_count INTEGER DEFAULT 0,  -- 鍥炲鏁?
    report_count INTEGER DEFAULT 0,  -- 涓炬姤鏁?
    audit_status INTEGER DEFAULT 1,  -- 瀹℃牳鐘舵€侊細0=寰呭鏍?1=閫氳繃 2=鎷掔粷
    audit_remark VARCHAR(500),
    audit_time TIMESTAMP,
    status INTEGER DEFAULT 1,  -- 鐘舵€侊細0=闅愯棌 1=姝ｅ父
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    device_info VARCHAR(200),

    -- BaseDO 瀛楁
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_comment_content_id ON content_comment(content_id);
CREATE INDEX idx_comment_user_id ON content_comment(user_id);
CREATE INDEX idx_comment_parent_id ON content_comment(parent_id);
CREATE INDEX idx_comment_root_id ON content_comment(root_id);
CREATE INDEX idx_comment_status ON content_comment(status);
CREATE INDEX idx_comment_deleted ON content_comment(deleted);

COMMENT ON TABLE content_comment IS 'Comment table';
COMMENT ON COLUMN content_comment.parent_id IS 'Parent comment id, 0 means root';
COMMENT ON COLUMN content_comment.audit_status IS 'Audit status: 0=pending 1=approved 2=rejected';

-- =====================================================
-- 6. content_interaction (浜掑姩璁板綍琛?
-- =====================================================
CREATE SEQUENCE content_interaction_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_interaction (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_interaction_seq'),
    content_id BIGINT NOT NULL,  -- 鍐呭ID
    user_id BIGINT NOT NULL,  -- 鐢ㄦ埛ID
    interaction_type INTEGER NOT NULL,  -- 浜掑姩绫诲瀷锛?=娴忚 2=鐐硅禐 3=鏀惰棌 4=鍒嗕韩
    device_info VARCHAR(200),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    source INTEGER,  -- 鏉ユ簮
    extra_data VARCHAR(1000),  -- 棰濆鏁版嵁

    -- BaseDO 瀛楁
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_interaction_content_id ON content_interaction(content_id);
CREATE INDEX idx_interaction_user_id ON content_interaction(user_id);
CREATE INDEX idx_interaction_type ON content_interaction(interaction_type);
CREATE INDEX idx_interaction_create_time ON content_interaction(create_time DESC);
CREATE INDEX idx_interaction_deleted ON content_interaction(deleted);

-- 澶嶅悎绱㈠紩锛氱敤浜庡幓閲嶅拰缁熻
CREATE UNIQUE INDEX idx_interaction_unique ON content_interaction(content_id, user_id, interaction_type, deleted)
WHERE deleted = false;

COMMENT ON TABLE content_interaction IS 'Interaction record';
COMMENT ON COLUMN content_interaction.interaction_type IS 'Interaction type: 1=view 2=like 3=favorite 4=share';

-- =====================================================
-- =====================================================

-- 7. content_favorite_group (鏀惰棌鍒嗙粍)
-- =====================================================

CREATE SEQUENCE content_favorite_group_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_favorite_group (

    id BIGINT PRIMARY KEY DEFAULT nextval('content_favorite_group_seq'),

    user_id BIGINT NOT NULL,

    group_name VARCHAR(60) NOT NULL,

    description VARCHAR(200),

    color VARCHAR(20),

    sort INTEGER DEFAULT 0,

    is_default INTEGER DEFAULT 0,

    cover_image VARCHAR(255),

    tag_list JSONB,

    extra JSONB,

    creator VARCHAR(64) DEFAULT '',

    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater VARCHAR(64) DEFAULT '',

    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted BOOLEAN NOT NULL DEFAULT FALSE

);



CREATE INDEX idx_favorite_group_user ON content_favorite_group(user_id);

CREATE INDEX idx_favorite_group_deleted ON content_favorite_group(deleted);



COMMENT ON TABLE content_favorite_group IS 'Favorite group';

COMMENT ON COLUMN content_favorite_group.is_default IS 'Default group: 0=normal 1=system default';



-- =====================================================

-- 8. content_favorite_record (鏀惰棌璁板綍)
-- =====================================================

CREATE SEQUENCE content_favorite_record_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_favorite_record (

    id BIGINT PRIMARY KEY DEFAULT nextval('content_favorite_record_seq'),

    content_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    group_id BIGINT,

    source INTEGER DEFAULT 1,

    note VARCHAR(200),

    tags JSONB,

    extra JSONB,

    creator VARCHAR(64) DEFAULT '',

    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater VARCHAR(64) DEFAULT '',

    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted BOOLEAN NOT NULL DEFAULT FALSE

);



CREATE INDEX idx_favorite_record_user ON content_favorite_record(user_id);

CREATE INDEX idx_favorite_record_content ON content_favorite_record(content_id);

CREATE INDEX idx_favorite_record_group ON content_favorite_record(group_id);

CREATE UNIQUE INDEX idx_favorite_record_unique ON content_favorite_record(content_id, user_id, deleted)

WHERE deleted = false;



COMMENT ON TABLE content_favorite_record IS 'Favorite record';

COMMENT ON COLUMN content_favorite_record.tags IS 'Tags when favorited';



-- =====================================================

-- 9. content_ad (鍐呭娴佸箍鍛?
-- =====================================================

CREATE SEQUENCE content_ad_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_ad (

    id BIGINT PRIMARY KEY DEFAULT nextval('content_ad_seq'),

    title VARCHAR(120) NOT NULL,

    sub_title VARCHAR(200),

    card_type VARCHAR(40),

    media_type VARCHAR(40),

    cover_image VARCHAR(500),

    video_url VARCHAR(500),

    jump_url VARCHAR(500),

    display_scene INTEGER DEFAULT 1,

    status INTEGER DEFAULT 1,

    priority INTEGER DEFAULT 0,

    frequency_cap INTEGER DEFAULT 3,

    call_to_action VARCHAR(60),

    start_time TIMESTAMP,

    end_time TIMESTAMP,

    advertiser_name VARCHAR(120),

    target_tags JSONB,

    style_meta JSONB,

    extra JSONB,

    creator VARCHAR(64) DEFAULT '',

    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater VARCHAR(64) DEFAULT '',

    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted BOOLEAN NOT NULL DEFAULT FALSE

);



CREATE INDEX idx_content_ad_status ON content_ad(status);

CREATE INDEX idx_content_ad_scene ON content_ad(display_scene);

CREATE INDEX idx_content_ad_time ON content_ad(start_time, end_time);



COMMENT ON TABLE content_ad IS 'Content feed advertisement';

COMMENT ON COLUMN content_ad.display_scene IS 'Display scene: 1=home 2=topic 3=search';



-- Insert initial data
-- =====================================================

-- 鎻掑叆榛樿璇濋
INSERT INTO content_topic (name, description, icon, type, is_recommend, status, hot_score) VALUES
('鏃ュ父鍒嗕韩', '鍒嗕韩鐢熸椿涓殑鐐圭偣婊存淮', '馃摑', 0, 1, 1, 100.0),
('缇庨', '缇庨鎺㈠簵涓庣児楗垎浜?, '馃崝', 0, 1, 1, 95.0),
('鏃呰', '鏃呰鏀荤暐涓庨鏅垎浜?, '鉁堬笍', 0, 1, 1, 90.0),
('鎽勫奖', '鎽勫奖浣滃搧涓庢妧宸у垎浜?, '馃摲', 0, 1, 1, 85.0),
('鍋ヨ韩', '鍋ヨ韩鎵撳崱涓庣粡楠屽垎浜?, '馃挭', 0, 1, 1, 80.0);

-- 鎻掑叆榛樿棰戦亾
INSERT INTO content_channel (code, name, description, icon, color, sort, status, is_default, is_required, keyword_hints)
VALUES
('recommend', '鎺ㄨ崘', '绯荤粺鏅鸿兘鎺ㄨ崘', '', '#FF4D4F', 0, 1, 1, 1, NULL),
('video', '瑙嗛', '鐑棬鐭棰戜笌 Vlog', '', '#FF9A00', 1, 1, 1, 0, '["瑙嗛","vlog","鐭墖","鐩存挱"]'::jsonb),
('life', '鐢熸椿', '鐢熸椿鏂瑰紡涓庢棩甯歌褰?, '', '#2DB7F5', 2, 1, 1, 0, '["鐢熸椿","鏃ュ父","瀹跺眳","璁板綍"]'::jsonb),
('fitness', '鍋ヨ韩', '杩愬姩鍋ヨ韩涓庡噺鑴傛墦鍗?, '', '#52C41A', 3, 1, 1, 0, '["鍋ヨ韩","杩愬姩","鐟滀冀","鎾搁搧","鍑忚剛"]'::jsonb),
('outdoor', '寰掓', '鎴峰寰掓涓庨湶钀?, '', '#13C2C2', 4, 1, 0, 0, '["寰掓","闇茶惀","鎴峰","鐧诲北"]'::jsonb),
('food', '缇庨', '缇庨鎺㈠簵涓庣児楗?, '', '#FADB14', 5, 1, 0, 0, '["缇庨","鐑归オ","椁愬巺","楗搧"]'::jsonb),
('fashion', '绌挎惌', '绌挎惌鐏垫劅涓庢椂灏?, '', '#EB2F96', 6, 1, 0, 0, '["绌挎惌","鏃跺皻","鏈嶉グ","閫犲瀷"]'::jsonb),
('hair', '澶村彂', '鍙戝瀷璁捐涓庢姢鍙?, '', '#722ED1', 7, 1, 0, 0, '["鍙戝瀷","澶村彂","鐞嗗彂","鏌撳彂"]'::jsonb),
('emotion', '鎯呮劅', '鎯呮劅鏁呬簨涓庡績鐞?, '', '#FA541C', 8, 1, 0, 0, '["鎯呮劅","鎭嬬埍","蹇冪悊","濠氬Щ"]'::jsonb),
('handcraft', '鎵嬪伐', '鎵嬪伐鍒涙剰涓庡伐鑹?, '', '#1890FF', 9, 1, 0, 0, '["鎵嬪伐","DIY","缂栫粐","鍒涙剰"]'::jsonb);

-- =====================================================
-- Grant permissions (濡傛灉闇€瑕?
-- =====================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;
-- =====================================================
-- Extra: content_user_follow / content_topic_follow
-- =====================================================
CREATE SEQUENCE IF NOT EXISTS content_user_follow_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS content_user_follow (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_user_follow_seq'),
    follower_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    status SMALLINT DEFAULT 0,
    source SMALLINT DEFAULT 0,
    remark VARCHAR(200),
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_follow_active ON content_user_follow(follower_id, target_id) WHERE status = 0 AND deleted = false;
CREATE INDEX IF NOT EXISTS idx_user_follow_target ON content_user_follow(target_id) WHERE status = 0 AND deleted = false;

CREATE SEQUENCE IF NOT EXISTS content_topic_follow_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS content_topic_follow (
    id BIGINT PRIMARY KEY DEFAULT nextval('content_topic_follow_seq'),
    user_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status SMALLINT DEFAULT 0,
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_topic_follow_active ON content_topic_follow(user_id, topic_id) WHERE status = 0 AND deleted = false;
