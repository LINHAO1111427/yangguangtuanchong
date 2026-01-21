-- =====================================================
-- Content Module - PostgreSQL Database Schema
-- 瀹屽叏鎸夌収鑺嬮亾妗嗘灦瑙勮寖鍜岀幇鏈塂O绫荤敓鎴?
-- =====================================================

-- Drop existing tables
DROP TABLE IF EXISTS content_favorite_record CASCADE;
DROP TABLE IF EXISTS content_favorite_group CASCADE;
DROP TABLE IF EXISTS content_ad CASCADE;
DROP TABLE IF EXISTS content_ad_event CASCADE;
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
DROP SEQUENCE IF EXISTS content_ad_event_seq;
DROP SEQUENCE IF EXISTS content_interaction_seq;
DROP SEQUENCE IF EXISTS content_comment_seq;
DROP SEQUENCE IF EXISTS content_post_seq;
DROP SEQUENCE IF EXISTS content_channel_user_seq;
DROP SEQUENCE IF EXISTS content_channel_seq;
DROP SEQUENCE IF EXISTS content_topic_seq;

-- =====================================================
-- 1. content_topic (璇濋琛?
-- 鍩轰簬 TopicDO.java
-- =====================================================
CREATE SEQUENCE content_topic_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_topic (
    id                   int8         NOT NULL DEFAULT nextval('content_topic_seq'),
    name                 varchar(100) NOT NULL,
    description          varchar(500) NULL,
    icon                 varchar(255) NULL,
    cover                varchar(255) NULL,
    type                 int2         NULL     DEFAULT 0,
    color                varchar(20)  NULL,
    sort                 int4         NULL     DEFAULT 0,
    is_recommend         int2         NULL     DEFAULT 0,
    status               int2         NULL     DEFAULT 1,
    participant_count    int4         NULL     DEFAULT 0,
    content_count        int4         NULL     DEFAULT 0,
    today_content_count  int4         NULL     DEFAULT 0,
    hot_score            float8       NULL     DEFAULT 0,
    tags                 jsonb        NULL,
    extra                jsonb        NULL,
    creator_id           int8         NULL,
    last_active_time     timestamp    NULL,

    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_topic PRIMARY KEY (id)
);

CREATE INDEX idx_content_topic_status ON content_topic(status);
CREATE INDEX idx_content_topic_recommend ON content_topic(is_recommend);
CREATE INDEX idx_content_topic_hot_score ON content_topic(hot_score DESC);
CREATE INDEX idx_content_topic_deleted ON content_topic(deleted);

COMMENT ON TABLE content_topic IS 'Topic table';
COMMENT ON COLUMN content_topic.type IS 'Topic type: 0=normal 1=event 2=challenge';
COMMENT ON COLUMN content_topic.is_recommend IS 'Is recommended: 0=no 1=yes';
COMMENT ON COLUMN content_topic.status IS 'Status: 0=disabled 1=enabled';
COMMENT ON COLUMN content_topic.deleted IS 'Logical delete flag: false=active true=deleted';

-- =====================================================
-- 2. content_channel (鍐呭棰戦亾閰嶇疆)
-- 鍩轰簬 ContentChannelDO.java
-- =====================================================
CREATE SEQUENCE content_channel_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_channel (
    id            int8         NOT NULL DEFAULT nextval('content_channel_seq'),
    code          varchar(64)  NOT NULL,
    name          varchar(100) NOT NULL,
    description   varchar(500) NULL,
    icon          varchar(255) NULL,
    color         varchar(32)  NULL,
    sort          int4         NULL     DEFAULT 0,
    status        int2         NULL     DEFAULT 1,
    is_default    int2         NULL     DEFAULT 0,
    is_required   int2         NULL     DEFAULT 0,
    keyword_hints jsonb        NULL,
    extra         jsonb        NULL,

    -- BaseDO 瀛楁
    creator       varchar(64)  NULL     DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  NULL     DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_channel PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_content_channel_code ON content_channel(code);
CREATE INDEX idx_content_channel_status ON content_channel(status);
CREATE INDEX idx_content_channel_sort ON content_channel(sort);

COMMENT ON TABLE content_channel IS 'Content channel config';
COMMENT ON COLUMN content_channel.is_default IS 'Default for new users: 0=no 1=yes';
COMMENT ON COLUMN content_channel.is_required IS 'Required channel (cannot remove): 0=no 1=yes';

-- =====================================================
-- 3. content_post (鍐呭鍙戝竷琛?
-- 鍩轰簬 ContentDO.java
-- =====================================================
CREATE SEQUENCE content_post_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_post (
    id                   int8         NOT NULL DEFAULT nextval('content_post_seq'),
    user_id              int8         NOT NULL,
    user_type            int2         NULL     DEFAULT 1,
    content_type         int2         NULL     DEFAULT 1,
    title                varchar(200) NULL,
    content              text         NULL,
    publish_topic_id     int8         NULL,
    channel_id           int8         NULL,
    channel_name         varchar(100) NULL,
    images               jsonb        NULL,
    video_url            varchar(500) NULL,
    video_cover          varchar(500) NULL,
    video_duration       int4         NULL,
    video_width          int4         NULL,
    video_height         int4         NULL,
    video_file_size      int8         NULL,
    video_format         varchar(20)  NULL,
    video_quality        int2         NULL,
    audio_duration       int4         NULL,
    is_public            int2         NULL     DEFAULT 1,
    status               int2         NULL     DEFAULT 0,
    audit_status         int2         NULL     DEFAULT 0,
    audit_remark         varchar(500) NULL,
    auditor_id           int8         NULL,
    allow_comment        int2         NULL     DEFAULT 1,
    allow_download       int2         NULL     DEFAULT 0,
    is_top               int2         NULL     DEFAULT 0,
    is_hot               int2         NULL     DEFAULT 0,
    is_recommend         int2         NULL     DEFAULT 0,
    view_count           int4         NULL     DEFAULT 0,
    like_count           int4         NULL     DEFAULT 0,
    comment_count        int4         NULL     DEFAULT 0,
    share_count          int4         NULL     DEFAULT 0,
    collect_count        int4         NULL     DEFAULT 0,
    forward_count        int4         NULL     DEFAULT 0,
    completion_rate      float8       NULL     DEFAULT 0,
    avg_watch_time       int4         NULL     DEFAULT 0,
    last_play_time       timestamp    NULL,
    publish_time         timestamp    NULL,
    hot_score            float8       NULL     DEFAULT 0,
    recommend_score      float8       NULL     DEFAULT 0,
    tags                 jsonb        NULL,
    extra                jsonb        NULL,
    cover_image          varchar(500) NULL,
    summary              varchar(500) NULL,

    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_post PRIMARY KEY (id)
);

CREATE INDEX idx_content_post_user_id ON content_post(user_id);
CREATE INDEX idx_content_post_channel_id ON content_post(channel_id);
CREATE INDEX idx_content_post_topic_id ON content_post(publish_topic_id);
CREATE INDEX idx_content_post_status ON content_post(status);
CREATE INDEX idx_content_post_audit_status ON content_post(audit_status);
CREATE INDEX idx_content_post_publish_time ON content_post(publish_time DESC);
CREATE INDEX idx_content_post_hot_score ON content_post(hot_score DESC);
CREATE INDEX idx_content_post_deleted ON content_post(deleted);

COMMENT ON TABLE content_post IS 'Content post';
COMMENT ON COLUMN content_post.content_type IS 'Content type: 1=picture 2=video 3=audio';
COMMENT ON COLUMN content_post.status IS 'Status: 0=draft 1=published 2=deleted';
COMMENT ON COLUMN content_post.audit_status IS 'Audit status: 0=pending 1=approved 2=rejected';
COMMENT ON COLUMN content_post.deleted IS 'Logical delete flag: false=active true=deleted';

-- =====================================================
-- 3. content_comment (璇勮琛?
-- 鍩轰簬 ContentCommentDO.java
-- =====================================================
CREATE SEQUENCE content_comment_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_comment (
    id                   int8         NOT NULL DEFAULT nextval('content_comment_seq'),
    content_id           int8         NOT NULL,
    user_id              int8         NOT NULL,
    parent_id            int8         NULL     DEFAULT 0,
    root_id              int8         NULL     DEFAULT 0,
    reply_user_id        int8         NULL,
    content              text         NOT NULL,
    images               jsonb        NULL,
    is_anonymous         int2         NULL     DEFAULT 0,
    like_count           int4         NULL     DEFAULT 0,
    reply_count          int4         NULL     DEFAULT 0,
    report_count         int4         NULL     DEFAULT 0,
    audit_status         int2         NULL     DEFAULT 1,
    audit_remark         varchar(500) NULL,
    audit_time           timestamp    NULL,
    status               int2         NULL     DEFAULT 1,
    ip_address           varchar(50)  NULL,
    user_agent           varchar(500) NULL,
    device_info          varchar(200) NULL,

    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_comment PRIMARY KEY (id)
);

CREATE INDEX idx_content_comment_content_id ON content_comment(content_id);
CREATE INDEX idx_content_comment_user_id ON content_comment(user_id);
CREATE INDEX idx_content_comment_parent_id ON content_comment(parent_id);
CREATE INDEX idx_content_comment_root_id ON content_comment(root_id);
CREATE INDEX idx_content_comment_status ON content_comment(status);
CREATE INDEX idx_content_comment_deleted ON content_comment(deleted);

COMMENT ON TABLE content_comment IS 'Comment table';
COMMENT ON COLUMN content_comment.parent_id IS 'Parent comment id, 0 means root';
COMMENT ON COLUMN content_comment.audit_status IS 'Audit status: 0=pending 1=approved 2=rejected';
COMMENT ON COLUMN content_comment.deleted IS 'Logical delete flag: false=active true=deleted';

-- =====================================================
-- 4. content_interaction (浜掑姩璁板綍琛?
-- 鍩轰簬 ContentInteractionDO.java
-- =====================================================
CREATE SEQUENCE content_interaction_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_interaction (
    id                   int8         NOT NULL DEFAULT nextval('content_interaction_seq'),
    content_id           int8         NOT NULL,
    user_id              int8         NOT NULL,
    interaction_type     int2         NOT NULL,
    device_info          varchar(200) NULL,
    ip_address           varchar(50)  NULL,
    user_agent           varchar(500) NULL,
    source               int2         NULL,
    extra_data           varchar(1000) NULL,

    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_interaction PRIMARY KEY (id)
);

CREATE INDEX idx_content_interaction_content_id ON content_interaction(content_id);
CREATE INDEX idx_content_interaction_user_id ON content_interaction(user_id);
CREATE INDEX idx_content_interaction_type ON content_interaction(interaction_type);
CREATE INDEX idx_content_interaction_create_time ON content_interaction(create_time DESC);
CREATE INDEX idx_content_interaction_deleted ON content_interaction(deleted);

-- 澶嶅悎鍞竴绱㈠紩锛氱敤浜庡幓閲?
CREATE UNIQUE INDEX uk_content_interaction ON content_interaction(content_id, user_id, interaction_type)
WHERE deleted = false;

COMMENT ON TABLE content_interaction IS 'Interaction record';
COMMENT ON COLUMN content_interaction.interaction_type IS 'Interaction type: 1=view 2=like 3=favorite 4=share';
COMMENT ON COLUMN content_interaction.deleted IS 'Logical delete flag: false=active true=deleted';

-- =====================================================
-- =====================================================

-- 5. content_favorite_group (鏀惰棌鍒嗙粍)

-- 鍩轰簬 ContentFavoriteGroupDO.java

-- =====================================================

CREATE SEQUENCE content_favorite_group_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_favorite_group (

    id                   int8         NOT NULL DEFAULT nextval('content_favorite_group_seq'),

    user_id              int8         NOT NULL,

    group_name           varchar(60)  NOT NULL,

    description          varchar(200) NULL,

    color                varchar(20)  NULL,

    sort                 int4         NULL     DEFAULT 0,

    is_default           int2         NULL     DEFAULT 0,

    cover_image          varchar(255) NULL,

    tag_list             jsonb        NULL,

    extra                jsonb        NULL,



    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級

    creator              varchar(64)  NULL     DEFAULT '',

    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater              varchar(64)  NULL     DEFAULT '',

    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted              int2         NOT NULL DEFAULT 0,



    CONSTRAINT pk_content_favorite_group PRIMARY KEY (id)

);



CREATE INDEX idx_content_favorite_group_user ON content_favorite_group(user_id);

CREATE INDEX idx_content_favorite_group_deleted ON content_favorite_group(deleted);



COMMENT ON TABLE content_favorite_group IS 'Favorite group';

COMMENT ON COLUMN content_favorite_group.is_default IS 'Default group: 0=normal 1=system default';

COMMENT ON COLUMN content_favorite_group.deleted IS 'Logical delete flag: false=active true=deleted';



-- =====================================================

-- 6. content_favorite_record (鏀惰棌璁板綍)

-- 鍩轰簬 ContentFavoriteRecordDO.java

-- =====================================================

CREATE SEQUENCE content_favorite_record_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_favorite_record (

    id                   int8         NOT NULL DEFAULT nextval('content_favorite_record_seq'),

    content_id           int8         NOT NULL,

    user_id              int8         NOT NULL,

    group_id             int8         NULL,

    source               int2         NULL     DEFAULT 1,

    note                 varchar(200) NULL,

    tags                 jsonb        NULL,

    extra                jsonb        NULL,



    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級

    creator              varchar(64)  NULL     DEFAULT '',

    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater              varchar(64)  NULL     DEFAULT '',

    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted              int2         NOT NULL DEFAULT 0,



    CONSTRAINT pk_content_favorite_record PRIMARY KEY (id)

);



CREATE INDEX idx_content_favorite_record_user ON content_favorite_record(user_id);

CREATE INDEX idx_content_favorite_record_content ON content_favorite_record(content_id);

CREATE INDEX idx_content_favorite_record_group ON content_favorite_record(group_id);

CREATE UNIQUE INDEX uk_content_favorite_record ON content_favorite_record(content_id, user_id)

WHERE deleted = false;



COMMENT ON TABLE content_favorite_record IS 'Favorite record';

COMMENT ON COLUMN content_favorite_record.tags IS 'Tags when favorited';

COMMENT ON COLUMN content_favorite_record.deleted IS 'Logical delete flag: false=active true=deleted';



-- =====================================================

-- 7. content_ad (鍐呭娴佸箍鍛?

-- 鍩轰簬 ContentAdDO.java

-- =====================================================

CREATE SEQUENCE content_ad_seq START WITH 1 INCREMENT BY 1;



CREATE TABLE content_ad (

    id                   int8         NOT NULL DEFAULT nextval('content_ad_seq'),

    title                varchar(120) NOT NULL,

    sub_title            varchar(200) NULL,

    card_type            varchar(40)  NULL,

    media_type           varchar(40)  NULL,

    cover_image          varchar(500) NULL,

    video_url            varchar(500) NULL,

    jump_url             varchar(500) NULL,

    display_scene        int2         NULL     DEFAULT 1,

    status               int2         NULL     DEFAULT 1,

    priority             int4         NULL     DEFAULT 0,

    frequency_cap        int4         NULL     DEFAULT 3,

    call_to_action       varchar(60)  NULL,

    start_time           timestamp    NULL,

    end_time             timestamp    NULL,

    advertiser_name      varchar(120) NULL,

    target_tags          jsonb        NULL,

    style_meta           jsonb        NULL,

    extra                jsonb        NULL,



    -- BaseDO 瀛楁锛堣妺閬撴爣鍑嗭級

    creator              varchar(64)  NULL     DEFAULT '',

    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updater              varchar(64)  NULL     DEFAULT '',

    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted              int2         NOT NULL DEFAULT 0,



    CONSTRAINT pk_content_ad PRIMARY KEY (id)

);



CREATE INDEX idx_content_ad_status ON content_ad(status);

CREATE INDEX idx_content_ad_scene ON content_ad(display_scene);

CREATE INDEX idx_content_ad_time ON content_ad(start_time, end_time);



COMMENT ON TABLE content_ad IS 'Content feed advertisement';

COMMENT ON COLUMN content_ad.display_scene IS 'Display scene: 1=home 2=topic 3=search';

COMMENT ON COLUMN content_ad.deleted IS 'Logical delete flag: false=active true=deleted';





-- =====================================================
-- 8. content_ad_event (Ad event record)
-- Based on ContentAdEventDO.java
-- =====================================================
CREATE SEQUENCE content_ad_event_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_ad_event (
    id          int8         NOT NULL DEFAULT nextval('content_ad_event_seq'),
    ad_id       int8         NOT NULL,
    user_id     int8         NULL,
    event_type  int2         NOT NULL,
    scene       int2         NULL,

    -- BaseDO fields
    creator     varchar(64)  NULL     DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  NULL     DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_ad_event PRIMARY KEY (id)
);

CREATE INDEX idx_content_ad_event_ad_id ON content_ad_event(ad_id);
CREATE INDEX idx_content_ad_event_user_id ON content_ad_event(user_id);
CREATE INDEX idx_content_ad_event_event_type ON content_ad_event(event_type);
CREATE INDEX idx_content_ad_event_scene ON content_ad_event(scene);
CREATE INDEX idx_content_ad_event_create_time ON content_ad_event(create_time DESC);
CREATE INDEX idx_content_ad_event_deleted ON content_ad_event(deleted);

COMMENT ON TABLE content_ad_event IS 'Ad event record';
COMMENT ON COLUMN content_ad_event.event_type IS 'Event type: 1=impression 2=click';
COMMENT ON COLUMN content_ad_event.deleted IS 'Logical delete flag: false=active true=deleted';
-- Insert initial data
-- =====================================================

-- 鎻掑叆榛樿璇濋
INSERT INTO content_topic (name, description, icon, type, is_recommend, status, hot_score) VALUES
('鏃ュ父鍒嗕韩', '鍒嗕韩鐢熸椿涓殑鐐圭偣婊存淮', '馃摑', 0, 1, 1, 100.0),
('缇庨', '缇庨鎺㈠簵涓庣児楗垎浜?, '馃崝', 0, 1, 1, 95.0),
('鏃呰', '鏃呰鏀荤暐涓庨鏅垎浜?, '鉁堬笍', 0, 1, 1, 90.0),
('鎽勫奖', '鎽勫奖浣滃搧涓庢妧宸у垎浜?, '馃摲', 0, 1, 1, 85.0),
('鍋ヨ韩', '鍋ヨ韩鎵撳崱涓庣粡楠屽垎浜?, '馃挭', 0, 1, 1, 80.0);

-- =====================================================
-- Grant permissions (濡傛灉闇€瑕?
-- =====================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;
