-- =====================================================
-- Content Module - PostgreSQL Database Schema
-- 完全按照芋道框架规范和现有DO类生成
-- 编码: UTF-8
-- =====================================================

-- Drop existing tables
DROP TABLE IF EXISTS content_user_follow CASCADE;
DROP TABLE IF EXISTS content_topic_follow CASCADE;
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
DROP SEQUENCE IF EXISTS content_user_follow_seq;
DROP SEQUENCE IF EXISTS content_topic_follow_seq;
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
-- 1. content_topic (话题表)
-- 基于 TopicDO.java
-- =====================================================
CREATE SEQUENCE content_topic_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_topic (
    id                   int8         NOT NULL DEFAULT nextval('content_topic_seq'),
    name                 varchar(100) NOT NULL,
    description          varchar(500) NULL,
    icon                 varchar(255) NULL,
    cover                varchar(255) NULL,
    type                 int2         NULL     DEFAULT 0,  -- 话题类型：0=普通 1=活动 2=挑战
    color                varchar(20)  NULL,
    sort                 int4         NULL     DEFAULT 0,
    is_recommend         int2         NULL     DEFAULT 0,  -- 是否推荐：0=否 1=是
    status               int2         NULL     DEFAULT 1,  -- 状态：0=禁用 1=启用
    participant_count    int4         NULL     DEFAULT 0,  -- 参与人数
    content_count        int4         NULL     DEFAULT 0,  -- 内容数量
    today_content_count  int4         NULL     DEFAULT 0,  -- 今日内容数
    hot_score            float8       NULL     DEFAULT 0,  -- 热度分数
    tags                 jsonb        NULL,    -- 标签数组
    extra                jsonb        NULL,    -- 额外信息
    creator_id           int8         NULL,    -- 创建者ID
    last_active_time     timestamp    NULL,    -- 最后活跃时间

    -- BaseDO 字段（芋道标准）
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,  -- 逻辑删除：0=未删除 1=已删除

    CONSTRAINT pk_content_topic PRIMARY KEY (id)
);

CREATE INDEX idx_content_topic_status ON content_topic(status);
CREATE INDEX idx_content_topic_recommend ON content_topic(is_recommend);
CREATE INDEX idx_content_topic_hot_score ON content_topic(hot_score DESC);
CREATE INDEX idx_content_topic_deleted ON content_topic(deleted);

COMMENT ON TABLE content_topic IS '话题表';
COMMENT ON COLUMN content_topic.type IS '话题类型: 0=普通 1=活动 2=挑战';
COMMENT ON COLUMN content_topic.is_recommend IS '是否推荐: 0=否 1=是';
COMMENT ON COLUMN content_topic.status IS '状态: 0=禁用 1=启用';
COMMENT ON COLUMN content_topic.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 2. content_channel (内容频道配置)
-- 基于 ContentChannelDO.java
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

    -- BaseDO 字段
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

COMMENT ON TABLE content_channel IS '内容频道配置表';
COMMENT ON COLUMN content_channel.is_default IS '新用户默认频道: 0=否 1=是';
COMMENT ON COLUMN content_channel.is_required IS '必选频道(不可移除): 0=否 1=是';

-- =====================================================
-- 3. content_post (内容发布表)
-- 基于 ContentDO.java
-- =====================================================
CREATE SEQUENCE content_post_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_post (
    id                   int8         NOT NULL DEFAULT nextval('content_post_seq'),
    user_id              int8         NOT NULL,
    user_type            int2         NULL     DEFAULT 1,
    content_type         int2         NULL     DEFAULT 1,  -- 内容类型：1=图文 2=视频 3=音频
    title                varchar(200) NULL,
    content              text         NULL,
    publish_topic_id     int8         NULL,    -- 发布话题ID
    channel_id           int8         NULL,    -- 频道ID
    channel_name         varchar(100) NULL,    -- 频道名称快照
    images               jsonb        NULL,    -- 图片数组
    video_url            varchar(500) NULL,
    video_cover          varchar(500) NULL,
    video_duration       int4         NULL,    -- 视频时长(秒)
    video_width          int4         NULL,
    video_height         int4         NULL,
    video_file_size      int8         NULL,
    video_format         varchar(20)  NULL,
    video_quality        int2         NULL,
    audio_duration       int4         NULL,    -- 音频时长(秒)
    is_public            int2         NULL     DEFAULT 1,  -- 是否公开：0=私密 1=公开
    status               int2         NULL     DEFAULT 0,  -- 状态：0=草稿 1=已发布 2=已删除
    audit_status         int2         NULL     DEFAULT 0,  -- 审核状态：0=待审核 1=通过 2=拒绝
    audit_remark         varchar(500) NULL,
    auditor_id           int8         NULL,
    allow_comment        int2         NULL     DEFAULT 1,  -- 允许评论：0=否 1=是
    allow_download       int2         NULL     DEFAULT 0,  -- 允许下载：0=否 1=是
    is_top               int2         NULL     DEFAULT 0,  -- 是否置顶
    is_hot               int2         NULL     DEFAULT 0,  -- 是否热门
    is_recommend         int2         NULL     DEFAULT 0,  -- 是否推荐
    view_count           int4         NULL     DEFAULT 0,  -- 浏览量
    like_count           int4         NULL     DEFAULT 0,  -- 点赞数
    comment_count        int4         NULL     DEFAULT 0,  -- 评论数
    share_count          int4         NULL     DEFAULT 0,  -- 分享数
    collect_count        int4         NULL     DEFAULT 0,  -- 收藏数
    forward_count        int4         NULL     DEFAULT 0,  -- 转发数
    completion_rate      float8       NULL     DEFAULT 0,  -- 完成率
    avg_watch_time       int4         NULL     DEFAULT 0,  -- 平均观看时长
    last_play_time       timestamp    NULL,    -- 最后播放时间
    publish_time         timestamp    NULL,    -- 发布时间
    hot_score            float8       NULL     DEFAULT 0,  -- 热度分数
    recommend_score      float8       NULL     DEFAULT 0,  -- 推荐分数
    tags                 jsonb        NULL,    -- 标签数组
    extra                jsonb        NULL,    -- 额外信息
    cover_image          varchar(500) NULL,    -- 封面图
    summary              varchar(500) NULL,    -- 摘要

    -- BaseDO 字段（芋道标准）
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

COMMENT ON TABLE content_post IS '内容发布表';
COMMENT ON COLUMN content_post.content_type IS '内容类型: 1=图文 2=视频 3=音频';
COMMENT ON COLUMN content_post.status IS '状态: 0=草稿 1=已发布 2=已删除';
COMMENT ON COLUMN content_post.audit_status IS '审核状态: 0=待审核 1=通过 2=拒绝';
COMMENT ON COLUMN content_post.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 4. content_channel_user (用户频道设置)
-- 基于原 content_schema.sql
-- =====================================================
CREATE SEQUENCE content_channel_user_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_channel_user (
    id            int8         NOT NULL DEFAULT nextval('content_channel_user_seq'),
    user_id       int8         NOT NULL,
    channel_id    int8         NOT NULL,
    display_order int4         NULL     DEFAULT 0,
    pinned        int2         NULL     DEFAULT 0,

    -- BaseDO 字段
    creator       varchar(64)  NULL     DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  NULL     DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_channel_user PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_content_channel_user ON content_channel_user(user_id, channel_id) WHERE deleted = 0;
CREATE INDEX idx_content_channel_user_order ON content_channel_user(user_id, display_order);

COMMENT ON TABLE content_channel_user IS '用户频道偏好设置';
COMMENT ON COLUMN content_channel_user.pinned IS '固定在我的频道: 0=否 1=是';

-- =====================================================
-- 5. content_comment (评论表)
-- 基于 ContentCommentDO.java
-- =====================================================
CREATE SEQUENCE content_comment_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_comment (
    id                   int8         NOT NULL DEFAULT nextval('content_comment_seq'),
    content_id           int8         NOT NULL,  -- 内容ID
    user_id              int8         NOT NULL,  -- 评论用户ID
    parent_id            int8         NULL     DEFAULT 0,  -- 父评论ID，0表示一级评论
    root_id              int8         NULL     DEFAULT 0,  -- 根评论ID
    reply_user_id        int8         NULL,    -- 回复的用户ID
    content              text         NOT NULL,  -- 评论内容
    images               jsonb        NULL,    -- 图片数组
    is_anonymous         int2         NULL     DEFAULT 0,  -- 是否匿名：0=否 1=是
    like_count           int4         NULL     DEFAULT 0,  -- 点赞数
    reply_count          int4         NULL     DEFAULT 0,  -- 回复数
    report_count         int4         NULL     DEFAULT 0,  -- 举报数
    audit_status         int2         NULL     DEFAULT 1,  -- 审核状态：0=待审核 1=通过 2=拒绝
    audit_remark         varchar(500) NULL,
    audit_time           timestamp    NULL,
    status               int2         NULL     DEFAULT 1,  -- 状态：0=隐藏 1=正常
    ip_address           varchar(50)  NULL,
    user_agent           varchar(500) NULL,
    device_info          varchar(200) NULL,

    -- BaseDO 字段（芋道标准）
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

COMMENT ON TABLE content_comment IS '评论表';
COMMENT ON COLUMN content_comment.parent_id IS '父评论id, 0表示根评论';
COMMENT ON COLUMN content_comment.audit_status IS '审核状态: 0=待审核 1=通过 2=拒绝';
COMMENT ON COLUMN content_comment.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 6. content_interaction (互动记录表)
-- 基于 ContentInteractionDO.java
-- =====================================================
CREATE SEQUENCE content_interaction_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_interaction (
    id                   int8         NOT NULL DEFAULT nextval('content_interaction_seq'),
    content_id           int8         NOT NULL,  -- 内容ID
    user_id              int8         NOT NULL,  -- 用户ID
    interaction_type     int2         NOT NULL,  -- 互动类型：1=浏览 2=点赞 3=收藏 4=分享
    device_info          varchar(200) NULL,
    ip_address           varchar(50)  NULL,
    user_agent           varchar(500) NULL,
    source               int2         NULL,    -- 来源
    extra_data           varchar(1000) NULL,   -- 额外数据

    -- BaseDO 字段（芋道标准）
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

-- 复合唯一索引：用于去重
CREATE UNIQUE INDEX uk_content_interaction ON content_interaction(content_id, user_id, interaction_type)
WHERE deleted = 0;

COMMENT ON TABLE content_interaction IS '互动记录表';
COMMENT ON COLUMN content_interaction.interaction_type IS '互动类型: 1=浏览 2=点赞 3=收藏 4=分享';
COMMENT ON COLUMN content_interaction.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 7. content_favorite_group (收藏分组)
-- 基于 ContentFavoriteGroupDO.java
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

    -- BaseDO 字段（芋道标准）
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_favorite_group PRIMARY KEY (id)
);

CREATE INDEX idx_content_favorite_group_user ON content_favorite_group(user_id);
CREATE INDEX idx_content_favorite_group_deleted ON content_favorite_group(deleted);

COMMENT ON TABLE content_favorite_group IS '收藏分组表';
COMMENT ON COLUMN content_favorite_group.is_default IS '默认分组: 0=普通 1=系统默认';
COMMENT ON COLUMN content_favorite_group.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 8. content_favorite_record (收藏记录)
-- 基于 ContentFavoriteRecordDO.java
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

    -- BaseDO 字段（芋道标准）
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
WHERE deleted = 0;

COMMENT ON TABLE content_favorite_record IS '收藏记录表';
COMMENT ON COLUMN content_favorite_record.tags IS '收藏时的标签';
COMMENT ON COLUMN content_favorite_record.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 9. content_ad (内容流广告)
-- 基于 ContentAdDO.java
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

    -- BaseDO 字段（芋道标准）
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

COMMENT ON TABLE content_ad IS '内容流广告表';
COMMENT ON COLUMN content_ad.display_scene IS '展示场景: 1=首页 2=话题 3=搜索';
COMMENT ON COLUMN content_ad.deleted IS '逻辑删除标志: 0=未删除 1=已删除';

-- =====================================================
-- 10. content_user_follow (用户关注表)
-- 基于原 content_schema.sql
-- =====================================================
CREATE SEQUENCE content_user_follow_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_user_follow (
    id            int8         NOT NULL DEFAULT nextval('content_user_follow_seq'),
    follower_id   int8         NOT NULL,  -- 关注者ID
    target_id     int8         NOT NULL,  -- 被关注者ID
    status        int2         NULL     DEFAULT 0,  -- 关注状态：0=正常 1=取消
    source        int2         NULL     DEFAULT 0,  -- 关注来源
    remark        varchar(200) NULL,

    -- BaseDO 字段
    creator       varchar(64)  NULL     DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  NULL     DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_user_follow PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_user_follow_active ON content_user_follow(follower_id, target_id) WHERE status = 0 AND deleted = 0;
CREATE INDEX idx_user_follow_target ON content_user_follow(target_id) WHERE status = 0 AND deleted = 0;

COMMENT ON TABLE content_user_follow IS '用户关注表';
COMMENT ON COLUMN content_user_follow.status IS '关注状态: 0=正常 1=取消';

-- =====================================================
-- 11. content_topic_follow (话题关注表)
-- 基于原 content_schema.sql
-- =====================================================
CREATE SEQUENCE content_topic_follow_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE content_topic_follow (
    id            int8         NOT NULL DEFAULT nextval('content_topic_follow_seq'),
    user_id       int8         NOT NULL,
    topic_id      int8         NOT NULL,
    status        int2         NULL     DEFAULT 0,  -- 关注状态：0=正常 1=取消

    -- BaseDO 字段
    creator       varchar(64)  NULL     DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  NULL     DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,

    CONSTRAINT pk_content_topic_follow PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_topic_follow_active ON content_topic_follow(user_id, topic_id) WHERE status = 0 AND deleted = 0;

COMMENT ON TABLE content_topic_follow IS '话题关注表';
COMMENT ON COLUMN content_topic_follow.status IS '关注状态: 0=正常 1=取消';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认话题
INSERT INTO content_topic (name, description, icon, type, is_recommend, status, hot_score) VALUES
('日常分享', '分享生活中的点点滴滴', '📝', 0, 1, 1, 100.0),
('美食', '美食探店与烹饪分享', '🍝', 0, 1, 1, 95.0),
('旅行', '旅行攻略与风景分享', '✈️', 0, 1, 1, 90.0),
('摄影', '摄影作品与技巧分享', '📷', 0, 1, 1, 85.0),
('健身', '健身打卡与经验分享', '💪', 0, 1, 1, 80.0);

-- 插入默认频道
INSERT INTO content_channel (code, name, description, icon, color, sort, status, is_default, is_required, keyword_hints)
VALUES
('recommend', '推荐', '系统智能推荐', '', '#FF4D4F', 0, 1, 1, 1, NULL),
('video', '视频', '热门短视频与 Vlog', '', '#FF9A00', 1, 1, 1, 0, '["视频","vlog","短片","直播"]'::jsonb),
('life', '生活', '生活方式与日常记录', '', '#2DB7F5', 2, 1, 1, 0, '["生活","日常","家居","记录"]'::jsonb),
('fitness', '健身', '运动健身与减脂打卡', '', '#52C41A', 3, 1, 1, 0, '["健身","运动","瑜伽","撸铁","减脂"]'::jsonb),
('outdoor', '徒步', '户外徒步与露营', '', '#13C2C2', 4, 1, 0, 0, '["徒步","露营","户外","登山"]'::jsonb),
('food', '美食', '美食探店与烹饪', '', '#FADB14', 5, 1, 0, 0, '["美食","烹饪","餐厅","饮品"]'::jsonb),
('fashion', '穿搭', '穿搭灵感与时尚', '', '#EB2F96', 6, 1, 0, 0, '["穿搭","时尚","服饰","造型"]'::jsonb),
('hair', '头发', '发型设计与护发', '', '#722ED1', 7, 1, 0, 0, '["发型","头发","理发","染发"]'::jsonb),
('emotion', '情感', '情感故事与心理', '', '#FA541C', 8, 1, 0, 0, '["情感","恋爱","心理","婚姻"]'::jsonb),
('handcraft', '手工', '手工创意与工艺', '', '#1890FF', 9, 1, 0, 0, '["手工","DIY","编织","创意"]'::jsonb);

-- =====================================================
-- Grant permissions (如果需要)
-- =====================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;

-- =====================================================
-- 脚本执行完成
-- =====================================================
