-- =============================================
-- 阳光团宠平台数据库设计 - PostgreSQL版本
-- 基于芋道框架扩展，支持分库分区分表
-- 创建时间: 2025-01-08
-- =============================================

-- =============================================
-- 1. 用户库 (xiaolvshu_member_db)
-- =============================================

-- 用户基础信息表
DROP TABLE IF EXISTS member_user CASCADE;
DROP SEQUENCE IF EXISTS member_custom_id_seq;

-- 创建custom_id序列，起始值100000（6位），最大值999999999（9位）
CREATE SEQUENCE member_custom_id_seq
    START WITH 1000000 
    INCREMENT BY 1 
    MAXVALUE 999999999 
    CACHE 100;

CREATE TABLE member_user (
    id                     BIGINT        PRIMARY KEY,
    custom_id              BIGINT        NOT NULL,
    username               VARCHAR(32)   NULL,
    mobile                 VARCHAR(20)   NULL,
    email                  VARCHAR(64)   NULL,
    password_hash          VARCHAR(128)  NOT NULL,
    password_algo          VARCHAR(32)   NOT NULL DEFAULT 'bcrypt',
    register_ip            INET          NULL,
    register_location      JSONB         NULL,
    register_weather       JSONB         NULL,
    register_solar_term    VARCHAR(16)   NULL,
    status                 SMALLINT      NOT NULL DEFAULT 1,
    tenant_id              BIGINT        NOT NULL DEFAULT 0,
    register_time          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    last_login_time        TIMESTAMPTZ   NULL,
    creator                VARCHAR(64)   NULL DEFAULT '',
    create_time            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updater                VARCHAR(64)   NULL DEFAULT '',
    update_time            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted                SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_member_user_custom UNIQUE (custom_id),
    CONSTRAINT uk_member_user_username UNIQUE (username),
    CONSTRAINT uk_member_user_mobile UNIQUE (mobile),
    CONSTRAINT uk_member_user_email UNIQUE (email)
) PARTITION BY HASH (custom_id);

-- 创建分区表（16个分区）
CREATE TABLE member_user_p0 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE member_user_p1 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE member_user_p2 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE member_user_p3 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE member_user_p4 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE member_user_p5 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE member_user_p6 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE member_user_p7 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE member_user_p8 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE member_user_p9 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE member_user_p10 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE member_user_p11 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE member_user_p12 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE member_user_p13 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE member_user_p14 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE member_user_p15 PARTITION OF member_user FOR VALUES WITH (MODULUS 16, REMAINDER 15);

-- 创建索引
CREATE INDEX idx_member_user_mobile ON member_user (mobile) WHERE mobile IS NOT NULL;
CREATE INDEX idx_member_user_email ON member_user (email) WHERE email IS NOT NULL;
CREATE INDEX idx_member_user_register_time ON member_user (register_time);
CREATE INDEX idx_member_user_status ON member_user (status, register_time DESC);

COMMENT ON TABLE member_user IS '用户基础信息表';
COMMENT ON COLUMN member_user.id IS '用户ID（雪花ID）';
COMMENT ON COLUMN member_user.custom_id IS '用户自定义ID（7-9位数字）';
COMMENT ON COLUMN member_user.username IS '登录账号';
COMMENT ON COLUMN member_user.mobile IS '手机号';
COMMENT ON COLUMN member_user.email IS '邮箱';
COMMENT ON COLUMN member_user.password_hash IS '密码哈希';
COMMENT ON COLUMN member_user.password_algo IS '密码算法';
COMMENT ON COLUMN member_user.register_ip IS '注册IP';
COMMENT ON COLUMN member_user.register_location IS '注册位置信息';
COMMENT ON COLUMN member_user.register_weather IS '注册时天气';
COMMENT ON COLUMN member_user.register_solar_term IS '注册时节气';
COMMENT ON COLUMN member_user.status IS '状态：1正常0禁用-1注销';

-- 用户扩展信息表
DROP TABLE IF EXISTS member_profile CASCADE;
CREATE TABLE member_profile (
    id                       BIGINT       PRIMARY KEY,
    user_id                  BIGINT       NOT NULL,
    nickname                 VARCHAR(32)  NULL,
    avatar                   VARCHAR(255) NULL,
    gender                   SMALLINT     NOT NULL DEFAULT 0,
    birthday                 DATE         NULL,
    bio                      VARCHAR(256) NULL,
    tags                     JSONB        NULL,
    vip_level               SMALLINT     NOT NULL DEFAULT 0,
    privacy_level           SMALLINT     NOT NULL DEFAULT 0,
    visitor_unlock_expire_at TIMESTAMPTZ  NULL,
    ext_json                JSONB        NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_member_profile_user UNIQUE (user_id)
);

CREATE INDEX idx_member_profile_nickname ON member_profile (nickname) WHERE nickname IS NOT NULL;
CREATE INDEX idx_member_profile_vip ON member_profile (vip_level, updated_at DESC);

COMMENT ON TABLE member_profile IS '用户扩展信息表';
COMMENT ON COLUMN member_profile.gender IS '性别：0未知1男2女';
COMMENT ON COLUMN member_profile.vip_level IS 'VIP等级';
COMMENT ON COLUMN member_profile.privacy_level IS '隐私级别';
COMMENT ON COLUMN member_profile.visitor_unlock_expire_at IS '访客查看过期时间';

-- 第三方账号绑定表
DROP TABLE IF EXISTS member_social_bind CASCADE;
CREATE TABLE member_social_bind (
    id           BIGINT       PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    provider     VARCHAR(32)  NOT NULL,
    open_id      VARCHAR(64)  NOT NULL,
    union_id     VARCHAR(64)  NULL,
    access_token VARCHAR(512) NULL,
    refresh_token VARCHAR(512) NULL,
    expires_in   INTEGER      NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_member_social_provider UNIQUE (provider, open_id)
);

CREATE INDEX idx_member_social_user ON member_social_bind (user_id);

COMMENT ON TABLE member_social_bind IS '第三方账号绑定表';
COMMENT ON COLUMN member_social_bind.provider IS '提供商：wechat/douyin/alipay/qq';

-- 隐私设置表
DROP TABLE IF EXISTS member_privacy_setting CASCADE;
CREATE TABLE member_privacy_setting (
    user_id         BIGINT      PRIMARY KEY,
    profile_visibility SMALLINT NOT NULL DEFAULT 0,
    follow_policy   SMALLINT    NOT NULL DEFAULT 0,
    message_policy  SMALLINT    NOT NULL DEFAULT 0,
    allow_comment   BOOLEAN     NOT NULL DEFAULT TRUE,
    allow_follow    BOOLEAN     NOT NULL DEFAULT TRUE,
    blacklist_mode  SMALLINT    NOT NULL DEFAULT 0,
    visitors_access BOOLEAN     NOT NULL DEFAULT FALSE,
    recommend_enabled BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE member_privacy_setting IS '用户隐私设置表';
COMMENT ON COLUMN member_privacy_setting.profile_visibility IS '资料可见范围：0开放1仅粉丝2仅互相关注';
COMMENT ON COLUMN member_privacy_setting.follow_policy IS '关注策略：0开放1需审核2拒绝所有';
COMMENT ON COLUMN member_privacy_setting.message_policy IS '消息策略：0任何人1关注名单2互相关注';

-- 访客记录表（按月分区）
DROP TABLE IF EXISTS member_visitor_log CASCADE;
CREATE TABLE member_visitor_log (
    id            BIGINT      PRIMARY KEY,
    owner_id      BIGINT      NOT NULL,
    visitor_id    BIGINT      NOT NULL,
    visit_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source        VARCHAR(32) NULL,
    is_paid_unlock BOOLEAN    NOT NULL DEFAULT FALSE,
    ip_address    INET        NULL,
    user_agent    VARCHAR(255) NULL
) PARTITION BY RANGE (visit_time);

-- 创建2025年分区
CREATE TABLE member_visitor_log_2025_01 PARTITION OF member_visitor_log 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE member_visitor_log_2025_02 PARTITION OF member_visitor_log 
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE member_visitor_log_2025_03 PARTITION OF member_visitor_log 
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');

CREATE INDEX idx_member_visitor_owner_time ON member_visitor_log (owner_id, visit_time DESC);
CREATE INDEX idx_member_visitor_visitor ON member_visitor_log (visitor_id, visit_time DESC);

COMMENT ON TABLE member_visitor_log IS '用户访客记录表';

-- 黑名单表
DROP TABLE IF EXISTS member_blacklist CASCADE;
CREATE TABLE member_blacklist (
    id         BIGINT      PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    target_id  BIGINT      NOT NULL,
    reason     VARCHAR(128) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_member_blacklist UNIQUE (user_id, target_id)
);

CREATE INDEX idx_member_blacklist_user ON member_blacklist (user_id, created_at DESC);
CREATE INDEX idx_member_blacklist_target ON member_blacklist (target_id);

COMMENT ON TABLE member_blacklist IS '用户黑名单表';

-- 好友/关注关系表
DROP TABLE IF EXISTS member_friend_relation CASCADE;
CREATE TABLE member_friend_relation (
    id             BIGINT      PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    friend_id      BIGINT      NOT NULL,
    relation_type  SMALLINT    NOT NULL DEFAULT 0,
    state          SMALLINT    NOT NULL DEFAULT 0,
    source         SMALLINT    NOT NULL DEFAULT 0,
    request_message VARCHAR(255) NULL,
    remark         VARCHAR(64) NULL,
    deleted        SMALLINT    NOT NULL DEFAULT 0,
    last_action_at TIMESTAMPTZ NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_member_friend UNIQUE (user_id, friend_id)
);

CREATE INDEX idx_friend_relation_user_state ON member_friend_relation (user_id, state, deleted, created_at DESC);
CREATE INDEX idx_friend_relation_friend ON member_friend_relation (friend_id, state, deleted);
CREATE INDEX idx_friend_relation_type ON member_friend_relation (relation_type, state);

COMMENT ON TABLE member_friend_relation IS '社交关系表';
COMMENT ON COLUMN member_friend_relation.relation_type IS '关系类型：0关注1好友';
COMMENT ON COLUMN member_friend_relation.state IS '状态：0待审核1已通过2已拒绝';

-- 可能认识的人接触统计
DROP TABLE IF EXISTS member_contact_stat CASCADE;
CREATE TABLE member_contact_stat (
    id               BIGINT      PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    target_user_id   BIGINT      NOT NULL,
    same_city        BOOLEAN     NOT NULL DEFAULT FALSE,
    same_district    BOOLEAN     NOT NULL DEFAULT FALSE,
    near_distance_count INTEGER  NOT NULL DEFAULT 0,
    same_wifi_count  INTEGER     NOT NULL DEFAULT 0,
    same_ip_count    INTEGER     NOT NULL DEFAULT 0,
    last_seen_time   TIMESTAMPTZ NULL,
    score            NUMERIC(10,4) NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_member_contact UNIQUE (user_id, target_user_id)
);

CREATE INDEX idx_member_contact_user ON member_contact_stat (user_id, score DESC, updated_at DESC);
CREATE INDEX idx_member_contact_target ON member_contact_stat (target_user_id, updated_at DESC);

COMMENT ON TABLE member_contact_stat IS '可能认识的人统计';

-- =============================================
-- 2. 内容库 (xiaolvshu_content_db)
-- =============================================

-- 作品主表（按月分区）
DROP TABLE IF EXISTS content_post CASCADE;
CREATE TABLE content_post (
    id            BIGINT       PRIMARY KEY,
    author_id     BIGINT       NOT NULL,
    type          SMALLINT     NOT NULL,
    title         VARCHAR(120) NULL,
    content       TEXT         NULL,
    media_count   SMALLINT     NOT NULL DEFAULT 0,
    cover_image   VARCHAR(255) NULL,
    media_meta    JSONB        NULL,
    topic_id      BIGINT       NULL,
    tags          JSONB        NULL,
    location      JSONB        NULL,
    audit_status  VARCHAR(16)  NOT NULL DEFAULT 'pending',
    audit_result  JSONB        NULL,
    audit_time    TIMESTAMPTZ  NULL,
    auditor_id    BIGINT       NULL,
    is_ad         BOOLEAN      NOT NULL DEFAULT FALSE,
    boost_level   SMALLINT     NOT NULL DEFAULT 0,
    boost_expire_at TIMESTAMPTZ NULL,
    publish_time  TIMESTAMPTZ  NULL,
    expire_time   TIMESTAMPTZ  NULL,
    view_count    INTEGER      NOT NULL DEFAULT 0,
    like_count    INTEGER      NOT NULL DEFAULT 0,
    comment_count INTEGER      NOT NULL DEFAULT 0,
    share_count   INTEGER      NOT NULL DEFAULT 0,
    collect_count INTEGER      NOT NULL DEFAULT 0,
    ext_json      JSONB        NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted       SMALLINT     NOT NULL DEFAULT 0
) PARTITION BY RANGE (created_at);

-- 创建2025年分区
CREATE TABLE content_post_2025_01 PARTITION OF content_post 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE content_post_2025_02 PARTITION OF content_post 
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE content_post_2025_03 PARTITION OF content_post 
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');

CREATE INDEX idx_content_post_author_created ON content_post (author_id, created_at DESC);
CREATE INDEX idx_content_post_topic ON content_post (topic_id) WHERE topic_id IS NOT NULL;
CREATE INDEX idx_content_post_status ON content_post (audit_status, created_at DESC);
CREATE INDEX idx_content_post_type_publish ON content_post (type, publish_time DESC) WHERE publish_time IS NOT NULL;
CREATE INDEX idx_content_post_boost ON content_post (boost_level, boost_expire_at DESC) WHERE boost_level > 0;

COMMENT ON TABLE content_post IS '作品主表';
COMMENT ON COLUMN content_post.type IS '类型：0图文1视频2广告';
COMMENT ON COLUMN content_post.audit_status IS '审核状态：pending/approved/rejected';
COMMENT ON COLUMN content_post.boost_level IS '热推等级：0-10';

-- 媒体文件表
DROP TABLE IF EXISTS content_media CASCADE;
CREATE TABLE content_media (
    id               BIGINT       PRIMARY KEY,
    post_id          BIGINT       NOT NULL,
    media_type       SMALLINT     NOT NULL,
    file_url         VARCHAR(255) NOT NULL,
    file_hash        VARCHAR(64)  NULL,
    file_size        BIGINT       NULL,
    duration         INTEGER      NULL,
    width            INTEGER      NULL,
    height           INTEGER      NULL,
    bitrate          INTEGER      NULL,
    transcode_status SMALLINT     NOT NULL DEFAULT 0,
    transcode_result JSONB        NULL,
    sort_order       SMALLINT     NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_content_media_post ON content_media (post_id, sort_order);
CREATE INDEX idx_content_media_hash ON content_media (file_hash) WHERE file_hash IS NOT NULL;
CREATE INDEX idx_content_media_transcode ON content_media (transcode_status) WHERE transcode_status = 0;

COMMENT ON TABLE content_media IS '媒体文件表';
COMMENT ON COLUMN content_media.media_type IS '媒体类型：1图片2视频3音频';
COMMENT ON COLUMN content_media.transcode_status IS '转码状态：0待处理1处理中2成功3失败';

-- 话题表
DROP TABLE IF EXISTS content_topic CASCADE;
CREATE TABLE content_topic (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    category    VARCHAR(32)  NULL,
    icon        VARCHAR(255) NULL,
    cover       VARCHAR(255) NULL,
    description TEXT         NULL,
    post_count  INTEGER      NOT NULL DEFAULT 0,
    follow_count INTEGER     NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    is_hot      BOOLEAN      NOT NULL DEFAULT FALSE,
    creator_id  BIGINT       NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_content_topic_name UNIQUE (name)
);

CREATE INDEX idx_content_topic_category ON content_topic (category, sort_order DESC);
CREATE INDEX idx_content_topic_hot ON content_topic (is_hot, post_count DESC);
CREATE INDEX idx_content_topic_status ON content_topic (status, post_count DESC);

COMMENT ON TABLE content_topic IS '话题表';

-- 标签表
DROP TABLE IF EXISTS content_tag CASCADE;
CREATE TABLE content_tag (
    id         BIGINT      PRIMARY KEY,
    name       VARCHAR(32) NOT NULL,
    color      VARCHAR(16) NULL,
    post_count INTEGER     NOT NULL DEFAULT 0,
    status     SMALLINT    NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_content_tag_name UNIQUE (name)
);

CREATE INDEX idx_content_tag_status ON content_tag (status, post_count DESC);

COMMENT ON TABLE content_tag IS '标签表';

-- 标签关联表
DROP TABLE IF EXISTS content_post_tag_relation CASCADE;
CREATE TABLE content_post_tag_relation (
    id         BIGINT      PRIMARY KEY,
    post_id    BIGINT      NOT NULL,
    tag_id     BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_content_post_tag UNIQUE (post_id, tag_id)
);

CREATE INDEX idx_content_post_tag_post ON content_post_tag_relation (post_id);
CREATE INDEX idx_content_post_tag_tag ON content_post_tag_relation (tag_id, created_at DESC);

COMMENT ON TABLE content_post_tag_relation IS '作品标签关联表';

-- 广告位配置表
DROP TABLE IF EXISTS content_ad_slot CASCADE;
CREATE TABLE content_ad_slot (
    id           BIGINT       PRIMARY KEY,
    code         VARCHAR(32)  NOT NULL,
    name         VARCHAR(64)  NOT NULL,
    position     VARCHAR(32)  NOT NULL,
    support_type JSONB        NOT NULL,
    pricing_mode SMALLINT     NOT NULL DEFAULT 1,
    base_price   DECIMAL(10,2) NOT NULL DEFAULT 0,
    status       SMALLINT     NOT NULL DEFAULT 1,
    description  TEXT         NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_content_ad_slot_code UNIQUE (code)
);

CREATE INDEX idx_content_ad_slot_position ON content_ad_slot (position, status);

COMMENT ON TABLE content_ad_slot IS '广告位配置表';
COMMENT ON COLUMN content_ad_slot.pricing_mode IS '计费模式：1CPC2CPM3CPA';

-- =============================================
-- 3. 互动库 (xiaolvshu_interaction_db)
-- =============================================

-- 点赞表（按用户哈希分区）
DROP TABLE IF EXISTS interaction_like CASCADE;
CREATE TABLE interaction_like (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    target_type SMALLINT    NOT NULL,
    target_id   BIGINT      NOT NULL,
    state       SMALLINT    NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_interaction_like UNIQUE (user_id, target_type, target_id)
) PARTITION BY HASH (user_id);

-- 创建16个分区
CREATE TABLE interaction_like_p0 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE interaction_like_p1 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE interaction_like_p2 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE interaction_like_p3 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE interaction_like_p4 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE interaction_like_p5 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE interaction_like_p6 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE interaction_like_p7 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE interaction_like_p8 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE interaction_like_p9 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE interaction_like_p10 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE interaction_like_p11 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE interaction_like_p12 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE interaction_like_p13 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE interaction_like_p14 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE interaction_like_p15 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 15);

CREATE INDEX idx_interaction_like_target ON interaction_like (target_type, target_id) WHERE state = 1;

COMMENT ON TABLE interaction_like IS '点赞表';
COMMENT ON COLUMN interaction_like.target_type IS '目标类型：0作品1评论';
COMMENT ON COLUMN interaction_like.state IS '状态：1点赞0取消';

-- 评论表（按日分区）
DROP TABLE IF EXISTS interaction_comment CASCADE;
CREATE TABLE interaction_comment (
    id           BIGINT      PRIMARY KEY,
    post_id      BIGINT      NOT NULL,
    parent_id    BIGINT      NULL,
    root_id      BIGINT      NULL,
    user_id      BIGINT      NOT NULL,
    content      TEXT        NOT NULL,
    media        JSONB       NULL,
    ip_location  VARCHAR(64) NULL,
    status       SMALLINT    NOT NULL DEFAULT 1,
    like_count   INTEGER     NOT NULL DEFAULT 0,
    reply_count  INTEGER     NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted      SMALLINT    NOT NULL DEFAULT 0
) PARTITION BY RANGE (created_at);

-- 创建分区（按天）
CREATE TABLE interaction_comment_2025_01_01 PARTITION OF interaction_comment 
    FOR VALUES FROM ('2025-01-01') TO ('2025-01-02');
CREATE TABLE interaction_comment_2025_01_02 PARTITION OF interaction_comment 
    FOR VALUES FROM ('2025-01-02') TO ('2025-01-03');

CREATE INDEX idx_interaction_comment_post_created ON interaction_comment (post_id, created_at DESC);
CREATE INDEX idx_interaction_comment_root ON interaction_comment (root_id, created_at ASC) WHERE root_id IS NOT NULL;
CREATE INDEX idx_interaction_comment_user ON interaction_comment (user_id, created_at DESC);

COMMENT ON TABLE interaction_comment IS '评论表';

-- 收藏表
DROP TABLE IF EXISTS interaction_favorite CASCADE;
CREATE TABLE interaction_favorite (
    id         BIGINT      PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL,
    folder_id  BIGINT      NULL,
    state      SMALLINT    NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_interaction_favorite UNIQUE (user_id, post_id)
);

CREATE INDEX idx_interaction_favorite_user_folder ON interaction_favorite (user_id, folder_id, created_at DESC);
CREATE INDEX idx_interaction_favorite_post ON interaction_favorite (post_id) WHERE state = 1;

COMMENT ON TABLE interaction_favorite IS '收藏表';

-- 收藏夹表
DROP TABLE IF EXISTS interaction_favorite_folder CASCADE;
CREATE TABLE interaction_favorite_folder (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    name        VARCHAR(32) NOT NULL,
    cover       VARCHAR(255) NULL,
    is_default  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_public   BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    post_count  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_favorite_folder_user ON interaction_favorite_folder (user_id, sort_order);

COMMENT ON TABLE interaction_favorite_folder IS '收藏夹表';

-- 打赏订单表
DROP TABLE IF EXISTS interaction_reward_order CASCADE;
CREATE TABLE interaction_reward_order (
    id         BIGINT         PRIMARY KEY,
    order_no   VARCHAR(32)    NOT NULL,
    user_id    BIGINT         NOT NULL,
    post_id    BIGINT         NOT NULL,
    author_id  BIGINT         NOT NULL,
    amount     DECIMAL(10,2)  NOT NULL,
    coin       INTEGER        NOT NULL,
    channel    VARCHAR(16)    NOT NULL,
    status     SMALLINT       NOT NULL DEFAULT 0,
    paid_at    TIMESTAMPTZ    NULL,
    refund_at  TIMESTAMPTZ    NULL,
    message    VARCHAR(128)   NULL,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_interaction_reward_order_no UNIQUE (order_no)
);

CREATE INDEX idx_reward_order_user ON interaction_reward_order (user_id, created_at DESC);
CREATE INDEX idx_reward_order_post ON interaction_reward_order (post_id, status, created_at DESC);
CREATE INDEX idx_reward_order_author ON interaction_reward_order (author_id, status, paid_at DESC);

COMMENT ON TABLE interaction_reward_order IS '打赏订单表';
COMMENT ON COLUMN interaction_reward_order.status IS '状态：0待支付1已支付2已退款';

-- 币钱包表
DROP TABLE IF EXISTS coin_wallet CASCADE;
CREATE TABLE coin_wallet (
    user_id        BIGINT      PRIMARY KEY,
    balance        INTEGER     NOT NULL DEFAULT 0,
    frozen_balance INTEGER     NOT NULL DEFAULT 0,
    total_income   INTEGER     NOT NULL DEFAULT 0,
    total_expense  INTEGER     NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version        INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE coin_wallet IS '币钱包表';
COMMENT ON COLUMN coin_wallet.balance IS '可用余额（单位：分）';
COMMENT ON COLUMN coin_wallet.frozen_balance IS '冻结余额（单位：分）';

-- 币钱包流水表（按月分区）
DROP TABLE IF EXISTS coin_wallet_log CASCADE;
CREATE TABLE coin_wallet_log (
    id            BIGINT      PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    change_type   VARCHAR(16) NOT NULL,
    amount        INTEGER     NOT NULL,
    balance_after INTEGER     NOT NULL,
    ref_no        VARCHAR(32) NULL,
    memo          VARCHAR(128) NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- 创建分区
CREATE TABLE coin_wallet_log_2025_01 PARTITION OF coin_wallet_log 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE coin_wallet_log_2025_02 PARTITION OF coin_wallet_log 
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

CREATE INDEX idx_coin_wallet_log_user_time ON coin_wallet_log (user_id, created_at DESC);
CREATE INDEX idx_coin_wallet_log_type ON coin_wallet_log (change_type, created_at DESC);

COMMENT ON TABLE coin_wallet_log IS '币钱包流水表';
COMMENT ON COLUMN coin_wallet_log.change_type IS '变动类型：task/reward/boost/withdraw';

-- 任务规则表
DROP TABLE IF EXISTS task_daily_rule CASCADE;
CREATE TABLE task_daily_rule (
    id            BIGINT      PRIMARY KEY,
    code          VARCHAR(32) NOT NULL,
    name          VARCHAR(64) NOT NULL,
    description   TEXT        NULL,
    trigger_event VARCHAR(32) NOT NULL,
    target_count  INTEGER     NOT NULL DEFAULT 1,
    reward_coin   INTEGER     NOT NULL DEFAULT 0,
    reward_exp    INTEGER     NOT NULL DEFAULT 0,
    status        SMALLINT    NOT NULL DEFAULT 1,
    sort_order    INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_task_daily_rule_code UNIQUE (code)
);

CREATE INDEX idx_task_daily_rule_status ON task_daily_rule (status, sort_order);

COMMENT ON TABLE task_daily_rule IS '每日任务规则表';

-- 任务记录表
DROP TABLE IF EXISTS task_daily_record CASCADE;
CREATE TABLE task_daily_record (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    rule_code   VARCHAR(32) NOT NULL,
    biz_date    DATE        NOT NULL,
    progress    INTEGER     NOT NULL DEFAULT 0,
    status      SMALLINT    NOT NULL DEFAULT 0,
    reward_time TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_task_daily UNIQUE (user_id, rule_code, biz_date)
);

CREATE INDEX idx_task_daily_record_user_date ON task_daily_record (user_id, biz_date DESC);
CREATE INDEX idx_task_daily_record_status ON task_daily_record (status, biz_date DESC);

COMMENT ON TABLE task_daily_record IS '每日任务记录表';
COMMENT ON COLUMN task_daily_record.status IS '状态：0进行中1已完成2已领取';

-- =============================================
-- 4. 消息库 (xiaolvshu_message_db)
-- =============================================

-- 消息会话表
DROP TABLE IF EXISTS message_thread CASCADE;
CREATE TABLE message_thread (
    id                BIGINT       PRIMARY KEY,
    type              SMALLINT     NOT NULL,
    biz_id            BIGINT       NULL,
    title             VARCHAR(64)  NULL,
    avatar            VARCHAR(255) NULL,
    last_message_id   BIGINT       NULL,
    last_message_time TIMESTAMPTZ  NULL,
    participant_count INTEGER      NOT NULL DEFAULT 0,
    status            SMALLINT     NOT NULL DEFAULT 1,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_message_thread_type ON message_thread (type, last_message_time DESC);

COMMENT ON TABLE message_thread IS '消息会话表';
COMMENT ON COLUMN message_thread.type IS '类型：0系统1私信2群组';

-- 会话参与者表
DROP TABLE IF EXISTS message_participant CASCADE;
CREATE TABLE message_participant (
    id         BIGINT      PRIMARY KEY,
    thread_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    role       SMALLINT    NOT NULL DEFAULT 0,
    join_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    leave_time TIMESTAMPTZ NULL,
    mute       BOOLEAN     NOT NULL DEFAULT FALSE,
    top        BOOLEAN     NOT NULL DEFAULT FALSE,
    status     SMALLINT    NOT NULL DEFAULT 1,
    CONSTRAINT uk_message_participant UNIQUE (thread_id, user_id)
);

CREATE INDEX idx_message_participant_user ON message_participant (user_id, status, top DESC, join_time DESC);

COMMENT ON TABLE message_participant IS '消息会话参与者表';
COMMENT ON COLUMN message_participant.role IS '角色：0普通1管理员2群主';

-- 消息详情表（按月分区）
DROP TABLE IF EXISTS message_detail CASCADE;
CREATE TABLE message_detail (
    id           BIGINT      PRIMARY KEY,
    thread_id    BIGINT      NOT NULL,
    sender_id    BIGINT      NOT NULL,
    content_type SMALLINT    NOT NULL,
    content      JSONB       NOT NULL,
    ext          JSONB       NULL,
    quote_id     BIGINT      NULL,
    send_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status       SMALLINT    NOT NULL DEFAULT 1,
    trace_id     VARCHAR(64) NULL
) PARTITION BY RANGE (send_time);

-- 创建分区
CREATE TABLE message_detail_2025_01 PARTITION OF message_detail 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE message_detail_2025_02 PARTITION OF message_detail 
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

CREATE INDEX idx_message_detail_thread_time ON message_detail (thread_id, send_time DESC);
CREATE INDEX idx_message_detail_sender ON message_detail (sender_id, send_time DESC);

COMMENT ON TABLE message_detail IS '消息详情表';
COMMENT ON COLUMN message_detail.content_type IS '内容类型：1文本2图片3视频4语音5分享6系统';

-- 未读消息表
DROP TABLE IF EXISTS message_unread CASCADE;
CREATE TABLE message_unread (
    thread_id           BIGINT      NOT NULL,
    user_id             BIGINT      NOT NULL,
    unread_count        INTEGER     NOT NULL DEFAULT 0,
    last_read_message_id BIGINT     NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (thread_id, user_id)
);

CREATE INDEX idx_message_unread_user ON message_unread (user_id, unread_count DESC, updated_at DESC);

COMMENT ON TABLE message_unread IS '未读消息表';

-- =============================================
-- 5. 营销库 (xiaolvshu_marketing_db)
-- =============================================

-- 营销团队表
DROP TABLE IF EXISTS marketing_team CASCADE;
CREATE TABLE marketing_team (
    id             BIGINT         PRIMARY KEY,
    name           VARCHAR(64)    NOT NULL,
    contact_name   VARCHAR(32)    NOT NULL,
    contact_phone  VARCHAR(20)    NOT NULL,
    contact_email  VARCHAR(64)    NULL,
    qualification  JSONB          NOT NULL,
    deposit_amount DECIMAL(10,2)  NOT NULL DEFAULT 0,
    frozen_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0,
    status         SMALLINT       NOT NULL DEFAULT 0,
    apply_time     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    approve_time   TIMESTAMPTZ    NULL,
    approver_id    BIGINT         NULL,
    reject_reason  TEXT           NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_marketing_team_name UNIQUE (name)
);

CREATE INDEX idx_marketing_team_status ON marketing_team (status, apply_time DESC);

COMMENT ON TABLE marketing_team IS '营销团队表';
COMMENT ON COLUMN marketing_team.status IS '状态：0申请中1已通过2已拒绝3已冻结';

-- 广告计划表
DROP TABLE IF EXISTS marketing_ad_campaign CASCADE;
CREATE TABLE marketing_ad_campaign (
    id         BIGINT         PRIMARY KEY,
    team_id    BIGINT         NOT NULL,
    slot_id    BIGINT         NOT NULL,
    name       VARCHAR(64)    NOT NULL,
    budget     DECIMAL(10,2)  NOT NULL,
    spent      DECIMAL(10,2)  NOT NULL DEFAULT 0,
    start_time TIMESTAMPTZ    NOT NULL,
    end_time   TIMESTAMPTZ    NOT NULL,
    targeting  JSONB          NULL,
    status     SMALLINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_campaign_team_status ON marketing_ad_campaign (team_id, status, created_at DESC);
CREATE INDEX idx_campaign_slot ON marketing_ad_campaign (slot_id, status, start_time);

COMMENT ON TABLE marketing_ad_campaign IS '广告计划表';
COMMENT ON COLUMN marketing_ad_campaign.status IS '状态：0草稿1投放中2已暂停3已结束';

-- 广告订单表
DROP TABLE IF EXISTS marketing_ad_order CASCADE;
CREATE TABLE marketing_ad_order (
    id          BIGINT         PRIMARY KEY,
    order_no    VARCHAR(32)    NOT NULL,
    campaign_id BIGINT         NOT NULL,
    post_id     BIGINT         NOT NULL,
    cost_type   VARCHAR(16)    NOT NULL,
    amount      DECIMAL(10,2)  NOT NULL DEFAULT 0,
    coin_amount INTEGER        NOT NULL DEFAULT 0,
    view_count  INTEGER        NOT NULL DEFAULT 0,
    click_count INTEGER        NOT NULL DEFAULT 0,
    state       SMALLINT       NOT NULL DEFAULT 0,
    start_time  TIMESTAMPTZ    NOT NULL,
    end_time    TIMESTAMPTZ    NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_marketing_ad_order_no UNIQUE (order_no)
);

CREATE INDEX idx_ad_order_campaign ON marketing_ad_order (campaign_id, state, created_at DESC);
CREATE INDEX idx_ad_order_post ON marketing_ad_order (post_id, state);

COMMENT ON TABLE marketing_ad_order IS '广告订单表';
COMMENT ON COLUMN marketing_ad_order.cost_type IS '计费类型：coin/cny';
COMMENT ON COLUMN marketing_ad_order.state IS '状态：0待投放1投放中2已完成3已取消';

-- 押金流水表（按月分区）
DROP TABLE IF EXISTS marketing_deposit_log CASCADE;
CREATE TABLE marketing_deposit_log (
    id            BIGINT         PRIMARY KEY,
    team_id       BIGINT         NOT NULL,
    change_type   VARCHAR(16)    NOT NULL,
    amount        DECIMAL(10,2)  NOT NULL,
    balance_after DECIMAL(10,2)  NOT NULL,
    ref_no        VARCHAR(32)    NULL,
    remark        VARCHAR(128)   NULL,
    operator_id   BIGINT         NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- 创建分区
CREATE TABLE marketing_deposit_log_2025_01 PARTITION OF marketing_deposit_log 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE INDEX idx_deposit_log_team_time ON marketing_deposit_log (team_id, created_at DESC);

COMMENT ON TABLE marketing_deposit_log IS '押金流水表';
COMMENT ON COLUMN marketing_deposit_log.change_type IS '变动类型：deposit/refund/freeze/unfreeze';

-- 热推记录表
DROP TABLE IF EXISTS marketing_boost_record CASCADE;
CREATE TABLE marketing_boost_record (
    id         BIGINT      PRIMARY KEY,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    boost_level SMALLINT   NOT NULL,
    paid_coin  INTEGER     NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ NOT NULL,
    status     SMALLINT    NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_boost_record_post ON marketing_boost_record (post_id, status, start_time DESC);
CREATE INDEX idx_boost_record_user ON marketing_boost_record (user_id, created_at DESC);

COMMENT ON TABLE marketing_boost_record IS '热推记录表';

-- =============================================
-- 6. 商城库 (xiaolvshu_shop_db)
-- =============================================

-- 商品表
DROP TABLE IF EXISTS shop_product CASCADE;
CREATE TABLE shop_product (
    id           BIGINT         PRIMARY KEY,
    name         VARCHAR(128)   NOT NULL,
    sku          VARCHAR(64)    NOT NULL,
    category_id  BIGINT         NULL,
    price        DECIMAL(10,2)  NOT NULL,
    coin_price   INTEGER        NOT NULL DEFAULT 0,
    stock        INTEGER        NOT NULL DEFAULT 0,
    sales        INTEGER        NOT NULL DEFAULT 0,
    status       SMALLINT       NOT NULL DEFAULT 1,
    cover        VARCHAR(255)   NULL,
    images       JSONB          NULL,
    detail       TEXT           NULL,
    ext_json     JSONB          NULL,
    sort_order   INTEGER        NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shop_product_sku UNIQUE (sku)
);

CREATE INDEX idx_shop_product_status ON shop_product (status, sort_order DESC);
CREATE INDEX idx_shop_product_category ON shop_product (category_id, status, sales DESC);

COMMENT ON TABLE shop_product IS '商品表';
COMMENT ON COLUMN shop_product.status IS '状态：1上架0下架';

-- 商品口令码表
DROP TABLE IF EXISTS shop_product_code CASCADE;
CREATE TABLE shop_product_code (
    id           BIGINT       PRIMARY KEY,
    product_id   BIGINT       NOT NULL,
    code         VARCHAR(32)  NOT NULL,
    status       SMALLINT     NOT NULL DEFAULT 1,
    use_count    INTEGER      NOT NULL DEFAULT 0,
    max_use      INTEGER      NOT NULL DEFAULT 0,
    bind_post_id BIGINT       NULL,
    expired_at   TIMESTAMPTZ  NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shop_product_code UNIQUE (code)
);

CREATE INDEX idx_product_code_product ON shop_product_code (product_id, status);
CREATE INDEX idx_product_code_post ON shop_product_code (bind_post_id) WHERE bind_post_id IS NOT NULL;

COMMENT ON TABLE shop_product_code IS '商品口令码表';

-- 订单表（按月分区）
DROP TABLE IF EXISTS shop_order CASCADE;
CREATE TABLE shop_order (
    id             BIGINT         PRIMARY KEY,
    order_no       VARCHAR(32)    NOT NULL,
    user_id        BIGINT         NOT NULL,
    product_id     BIGINT         NOT NULL,
    product_name   VARCHAR(128)   NOT NULL,
    product_sku    VARCHAR(64)    NOT NULL,
    quantity       INTEGER        NOT NULL DEFAULT 1,
    price          DECIMAL(10,2)  NOT NULL,
    coin_amount    INTEGER        NOT NULL DEFAULT 0,
    total_amount   DECIMAL(10,2)  NOT NULL,
    pay_channel    VARCHAR(16)    NULL,
    order_status   SMALLINT       NOT NULL DEFAULT 0,
    pay_status     SMALLINT       NOT NULL DEFAULT 0,
    pay_time       TIMESTAMPTZ    NULL,
    delivery_status SMALLINT      NOT NULL DEFAULT 0,
    delivery_time  TIMESTAMPTZ    NULL,
    receive_time   TIMESTAMPTZ    NULL,
    refund_status  SMALLINT       NOT NULL DEFAULT 0,
    refund_time    TIMESTAMPTZ    NULL,
    remark         VARCHAR(255)   NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_shop_order_no UNIQUE (order_no)
) PARTITION BY RANGE (created_at);

-- 创建分区
CREATE TABLE shop_order_2025_01 PARTITION OF shop_order 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE INDEX idx_shop_order_user ON shop_order (user_id, created_at DESC);
CREATE INDEX idx_shop_order_status ON shop_order (order_status, pay_status, created_at DESC);

COMMENT ON TABLE shop_order IS '商品订单表';
COMMENT ON COLUMN shop_order.order_status IS '订单状态：0待支付1已支付2已取消3已完成';
COMMENT ON COLUMN shop_order.pay_status IS '支付状态：0未支付1已支付2已退款';

-- 收货地址表
DROP TABLE IF EXISTS shop_address CASCADE;
CREATE TABLE shop_address (
    id         BIGINT       PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    receiver   VARCHAR(32)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    province   VARCHAR(32)  NOT NULL,
    city       VARCHAR(32)  NOT NULL,
    district   VARCHAR(32)  NOT NULL,
    detail     VARCHAR(128) NOT NULL,
    postal_code VARCHAR(10)  NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shop_address_user_default ON shop_address (user_id, is_default DESC, created_at DESC);

COMMENT ON TABLE shop_address IS '收货地址表';

-- =============================================
-- 7. 运维库 (xiaolvshu_ops_db)  
-- =============================================

-- 操作审计日志表（按月分区）
DROP TABLE IF EXISTS ops_audit_log CASCADE;
CREATE TABLE ops_audit_log (
    id           BIGINT       PRIMARY KEY,
    operator_id  BIGINT       NOT NULL,
    operator_name VARCHAR(32) NOT NULL,
    action       VARCHAR(32)  NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_id  BIGINT       NULL,
    resource_name VARCHAR(64) NULL,
    result       SMALLINT     NOT NULL,
    ip           INET         NOT NULL,
    user_agent   VARCHAR(255) NULL,
    request_data JSONB        NULL,
    response_data JSONB       NULL,
    error_msg    TEXT         NULL,
    duration     INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- 创建分区
CREATE TABLE ops_audit_log_2025_01 PARTITION OF ops_audit_log 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE INDEX idx_ops_audit_log_operator ON ops_audit_log (operator_id, created_at DESC);
CREATE INDEX idx_ops_audit_log_resource ON ops_audit_log (resource_type, resource_id, created_at DESC);
CREATE INDEX idx_ops_audit_log_action ON ops_audit_log (action, result, created_at DESC);

COMMENT ON TABLE ops_audit_log IS '操作审计日志表';

-- API调用统计表
DROP TABLE IF EXISTS ops_api_call_stat CASCADE;
CREATE TABLE ops_api_call_stat (
    id            BIGINT       PRIMARY KEY,
    api_path      VARCHAR(128) NOT NULL,
    method        VARCHAR(8)   NOT NULL,
    call_count    INTEGER      NOT NULL DEFAULT 0,
    error_count   INTEGER      NOT NULL DEFAULT 0,
    avg_duration  INTEGER      NOT NULL DEFAULT 0,
    max_duration  INTEGER      NOT NULL DEFAULT 0,
    min_duration  INTEGER      NOT NULL DEFAULT 0,
    stat_date     DATE         NOT NULL,
    stat_hour     SMALLINT     NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ops_api_call_stat UNIQUE (api_path, method, stat_date, stat_hour)
);

CREATE INDEX idx_ops_api_call_stat_date ON ops_api_call_stat (stat_date DESC, call_count DESC);

COMMENT ON TABLE ops_api_call_stat IS 'API调用统计表';

-- =============================================
-- 8. 物化视图和函数
-- =============================================

-- 热门内容排行榜物化视图
DROP MATERIALIZED VIEW IF EXISTS mv_content_hot_rank;
CREATE MATERIALIZED VIEW mv_content_hot_rank AS
SELECT 
    p.id as post_id,
    p.author_id,
    p.title,
    p.cover_image,
    p.type,
    -- 热度计算公式：点赞*2 + 评论*3 + 分享*5 + 收藏*4 - 时间衰减
    (p.like_count * 2 + p.comment_count * 3 + p.share_count * 5 + p.collect_count * 4) 
    * EXP(-EXTRACT(EPOCH FROM (NOW() - p.publish_time)) / 86400.0) as hot_score,
    p.publish_time,
    CURRENT_DATE as rank_date,
    ROW_NUMBER() OVER (ORDER BY 
        (p.like_count * 2 + p.comment_count * 3 + p.share_count * 5 + p.collect_count * 4) 
        * EXP(-EXTRACT(EPOCH FROM (NOW() - p.publish_time)) / 86400.0) DESC
    ) as rank_num
FROM content_post p
WHERE p.audit_status = 'approved' 
  AND p.publish_time >= CURRENT_DATE - INTERVAL '7 days'
  AND p.deleted = 0
ORDER BY hot_score DESC
LIMIT 1000;

-- 为物化视图创建索引
CREATE UNIQUE INDEX idx_mv_content_hot_rank_post ON mv_content_hot_rank (post_id);
CREATE INDEX idx_mv_content_hot_rank_score ON mv_content_hot_rank (hot_score DESC);
CREATE INDEX idx_mv_content_hot_rank_type ON mv_content_hot_rank (type, rank_num);

-- 自定义函数：生成不重复的custom_id
CREATE OR REPLACE FUNCTION generate_custom_id() RETURNS BIGINT AS $$
DECLARE
    new_id BIGINT;
    max_attempts INTEGER := 100;
    attempt INTEGER := 0;
BEGIN
    LOOP
        -- 生成7-9位随机数字，避免豹子号
        new_id := (RANDOM() * 899999999 + 1000000)::BIGINT;
        
        -- 检查是否为豹子号（连续3个或以上相同数字）
        IF new_id::TEXT !~ '(\d)\1{2,}' THEN
            -- 检查是否已存在
            IF NOT EXISTS (SELECT 1 FROM member_user WHERE custom_id = new_id) THEN
                RETURN new_id;
            END IF;
        END IF;
        
        attempt := attempt + 1;
        IF attempt >= max_attempts THEN
            RAISE EXCEPTION 'Failed to generate unique custom_id after % attempts', max_attempts;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 自定义函数：更新内容统计数据
CREATE OR REPLACE FUNCTION update_content_stats(p_post_id BIGINT, p_stat_type VARCHAR, p_increment INTEGER DEFAULT 1) 
RETURNS VOID AS $$
BEGIN
    UPDATE content_post 
    SET 
        like_count = CASE WHEN p_stat_type = 'like' THEN like_count + p_increment ELSE like_count END,
        comment_count = CASE WHEN p_stat_type = 'comment' THEN comment_count + p_increment ELSE comment_count END,
        share_count = CASE WHEN p_stat_type = 'share' THEN share_count + p_increment ELSE share_count END,
        collect_count = CASE WHEN p_stat_type = 'collect' THEN collect_count + p_increment ELSE collect_count END,
        view_count = CASE WHEN p_stat_type = 'view' THEN view_count + p_increment ELSE view_count END,
        updated_at = NOW()
    WHERE id = p_post_id;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- 9. 分区维护函数
-- =============================================

-- 自动创建分区的函数
CREATE OR REPLACE FUNCTION create_monthly_partitions(table_name TEXT, months_ahead INTEGER DEFAULT 6)
RETURNS VOID AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    partition_name TEXT;
    i INTEGER;
BEGIN
    -- 从下个月开始创建分区
    start_date := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');
    
    FOR i IN 1..months_ahead LOOP
        end_date := start_date + INTERVAL '1 month';
        partition_name := table_name || '_' || TO_CHAR(start_date, 'YYYY_MM');
        
        -- 检查分区是否已存在
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables WHERE tablename = partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                partition_name, table_name, start_date, end_date
            );
            RAISE NOTICE 'Created partition: %', partition_name;
        END IF;
        
        start_date := end_date;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- 10. 初始化数据
-- =============================================

-- 插入系统用户
INSERT INTO member_user (id, custom_id, username, password_hash, password_algo, status, register_time) 
VALUES (1, 1000001, 'system', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgO.sqnGdmrxhCdvdOgVc1EuG', 'bcrypt', 1, NOW())
ON CONFLICT DO NOTHING;

-- 插入默认任务规则
INSERT INTO task_daily_rule (id, code, name, description, trigger_event, target_count, reward_coin, reward_exp, status)
VALUES 
(1, 'DAILY_VIDEO_VIEW', '每日观看视频', '每日观看3个短视频获得奖励', 'video_view', 3, 10, 5, 1),
(2, 'DAILY_IMAGE_VIEW', '每日浏览图文', '每日浏览3篇图文获得奖励', 'image_view', 3, 10, 5, 1),
(3, 'DAILY_AD_VIEW', '每日观看广告', '每日观看1条广告获得奖励', 'ad_view', 1, 20, 10, 1),
(4, 'DAILY_LIKE', '每日点赞', '每日点赞5次获得奖励', 'like', 5, 5, 3, 1),
(5, 'DAILY_COMMENT', '每日评论', '每日评论2次获得奖励', 'comment', 2, 15, 8, 1)
ON CONFLICT DO NOTHING;

-- 插入默认话题
INSERT INTO content_topic (id, name, category, icon, description, status, is_hot)
VALUES 
(1, '生活分享', 'life', '🏠', '分享生活中的美好瞬间', 1, true),
(2, '美食推荐', 'food', '🍜', '发现和分享美食', 1, true),
(3, '旅行日记', 'travel', '✈️', '记录旅行的足迹', 1, true),
(4, '时尚穿搭', 'fashion', '👗', '时尚穿搭分享', 1, true),
(5, '数码科技', 'tech', '📱', '数码产品和科技资讯', 1, false),
(6, '健身运动', 'fitness', '💪', '健身运动相关内容', 1, false),
(7, '学习成长', 'study', '📚', '学习方法和个人成长', 1, false),
(8, '宠物日常', 'pet', '🐱', '宠物相关的可爱内容', 1, true)
ON CONFLICT DO NOTHING;

-- 插入默认广告位
INSERT INTO content_ad_slot (id, code, name, position, support_type, pricing_mode, base_price, status)
VALUES 
(1, 'HOME_BANNER', '首页横幅', 'home_top', '["image", "video"]', 2, 100.00, 1),
(2, 'FEED_INSERT', '信息流插入', 'feed_middle', '["image", "video", "native"]', 1, 0.50, 1),
(3, 'DETAIL_BOTTOM', '详情页底部', 'detail_bottom', '["image", "text"]', 1, 0.30, 1)
ON CONFLICT DO NOTHING;

-- 更新序列值
SELECT setval('member_custom_id_seq', 1000001, true);

-- =============================================
-- 结束
-- =============================================

-- 刷新物化视图
REFRESH MATERIALIZED VIEW mv_content_hot_rank;

COMMIT;