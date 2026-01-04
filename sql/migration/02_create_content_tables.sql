-- =============================================
-- Content Module Tables - xiaolvshu_content database
-- Execute this after connecting to xiaolvshu_content database
-- =============================================

-- Connect to xiaolvshu_content
\c xiaolvshu_content

-- =============================================
-- 1. Content Post Table (Main table with monthly partition)
-- =============================================

DROP TABLE IF EXISTS content_post CASCADE;

CREATE TABLE content_post (
    id            BIGINT       NOT NULL,
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
    deleted       SMALLINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE content_post IS 'Content post main table (partitioned by month)';
COMMENT ON COLUMN content_post.type IS 'Type: 0=image 1=video 2=ad';
COMMENT ON COLUMN content_post.audit_status IS 'Audit status: pending/approved/rejected';
COMMENT ON COLUMN content_post.boost_level IS 'Boost level: 0-10';

-- Create partitions for 2025
CREATE TABLE content_post_2025_01 PARTITION OF content_post
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE content_post_2025_02 PARTITION OF content_post
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE content_post_2025_03 PARTITION OF content_post
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE content_post_2025_04 PARTITION OF content_post
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE content_post_2025_05 PARTITION OF content_post
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE content_post_2025_06 PARTITION OF content_post
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE content_post_2025_07 PARTITION OF content_post
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
CREATE TABLE content_post_2025_08 PARTITION OF content_post
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');
CREATE TABLE content_post_2025_09 PARTITION OF content_post
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');
CREATE TABLE content_post_2025_10 PARTITION OF content_post
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
CREATE TABLE content_post_2025_11 PARTITION OF content_post
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
CREATE TABLE content_post_2025_12 PARTITION OF content_post
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

-- Create indexes
CREATE INDEX idx_content_post_author_created ON content_post (author_id, created_at DESC);
CREATE INDEX idx_content_post_topic ON content_post (topic_id) WHERE topic_id IS NOT NULL;
CREATE INDEX idx_content_post_status ON content_post (audit_status, created_at DESC);
CREATE INDEX idx_content_post_type_publish ON content_post (type, publish_time DESC) WHERE publish_time IS NOT NULL;
CREATE INDEX idx_content_post_boost ON content_post (boost_level, boost_expire_at DESC) WHERE boost_level > 0;

-- =============================================
-- 2. Content Media Table
-- =============================================

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

COMMENT ON TABLE content_media IS 'Media files table';
COMMENT ON COLUMN content_media.media_type IS 'Media type: 1=image 2=video 3=audio';
COMMENT ON COLUMN content_media.transcode_status IS 'Transcode status: 0=pending 1=processing 2=success 3=failed';

-- =============================================
-- 3. Content Topic Table
-- =============================================

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

COMMENT ON TABLE content_topic IS 'Topic table';

-- Insert default topics
INSERT INTO content_topic (id, name, category, icon, description, status, is_hot)
VALUES
(1, 'Life Sharing', 'life', '🏠', 'Share beautiful moments in life', 1, true),
(2, 'Food Recommendations', 'food', '🍜', 'Discover and share food', 1, true),
(3, 'Travel Diary', 'travel', '✈️', 'Record travel footprints', 1, true),
(4, 'Fashion Outfits', 'fashion', '👗', 'Fashion outfit sharing', 1, true),
(5, 'Digital Technology', 'tech', '📱', 'Digital products and tech news', 1, false),
(6, 'Fitness Sports', 'fitness', '💪', 'Fitness and sports related content', 1, false),
(7, 'Learning Growth', 'study', '📚', 'Learning methods and personal growth', 1, false),
(8, 'Pet Daily', 'pet', '🐱', 'Cute pet related content', 1, true)
ON CONFLICT DO NOTHING;

-- =============================================
-- 4. Content Tag Table
-- =============================================

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

COMMENT ON TABLE content_tag IS 'Tag table';

-- =============================================
-- 5. Content Tag Relation Table
-- =============================================

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

COMMENT ON TABLE content_post_tag_relation IS 'Post tag relation table';

-- =============================================
-- 6. Content Comment Table (Daily partition)
-- =============================================

DROP TABLE IF EXISTS content_comment CASCADE;

CREATE TABLE content_comment (
    id           BIGINT      NOT NULL,
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
    deleted      SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE content_comment IS 'Comment table (partitioned by day)';

-- Create partitions for current month (2025-10)
CREATE TABLE content_comment_2025_10_01 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-01') TO ('2025-10-02');
CREATE TABLE content_comment_2025_10_02 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-02') TO ('2025-10-03');
CREATE TABLE content_comment_2025_10_03 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-03') TO ('2025-10-04');
CREATE TABLE content_comment_2025_10_04 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-04') TO ('2025-10-05');
CREATE TABLE content_comment_2025_10_05 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-05') TO ('2025-10-06');
CREATE TABLE content_comment_2025_10_06 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-06') TO ('2025-10-07');
CREATE TABLE content_comment_2025_10_07 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-07') TO ('2025-10-08');
CREATE TABLE content_comment_2025_10_08 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-08') TO ('2025-10-09');
CREATE TABLE content_comment_2025_10_09 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-09') TO ('2025-10-10');
CREATE TABLE content_comment_2025_10_10 PARTITION OF content_comment
    FOR VALUES FROM ('2025-10-10') TO ('2025-10-11');
-- ... (More partitions can be auto-generated by function)

CREATE INDEX idx_content_comment_post_created ON content_comment (post_id, created_at DESC);
CREATE INDEX idx_content_comment_root ON content_comment (root_id, created_at ASC) WHERE root_id IS NOT NULL;
CREATE INDEX idx_content_comment_user ON content_comment (user_id, created_at DESC);

-- =============================================
-- 7. Content Like Table (Hash partition by user_id)
-- =============================================

DROP TABLE IF EXISTS content_like CASCADE;

CREATE TABLE content_like (
    id          BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    target_type SMALLINT    NOT NULL,
    target_id   BIGINT      NOT NULL,
    state       SMALLINT    NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, user_id),
    CONSTRAINT uk_content_like UNIQUE (user_id, target_type, target_id)
) PARTITION BY HASH (user_id);

COMMENT ON TABLE content_like IS 'Like table (partitioned by user_id hash)';
COMMENT ON COLUMN content_like.target_type IS 'Target type: 0=post 1=comment';
COMMENT ON COLUMN content_like.state IS 'State: 1=liked 0=unliked';

-- Create 16 hash partitions
CREATE TABLE content_like_p0 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE content_like_p1 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE content_like_p2 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE content_like_p3 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE content_like_p4 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE content_like_p5 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE content_like_p6 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE content_like_p7 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE content_like_p8 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE content_like_p9 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE content_like_p10 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE content_like_p11 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE content_like_p12 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE content_like_p13 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE content_like_p14 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE content_like_p15 PARTITION OF content_like FOR VALUES WITH (MODULUS 16, REMAINDER 15);

CREATE INDEX idx_content_like_target ON content_like (target_type, target_id) WHERE state = 1;

-- =============================================
-- 8. Content Favorite Group Table
-- =============================================

DROP TABLE IF EXISTS content_favorite_group CASCADE;

CREATE TABLE content_favorite_group (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    group_name  VARCHAR(64)  NOT NULL,
    description VARCHAR(255) NULL,
    color       VARCHAR(32)  NULL,
    cover_image VARCHAR(255) NULL,
    is_default  SMALLINT     NOT NULL DEFAULT 0,
    tag_list    JSONB        NULL,
    extra       JSONB        NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

CREATE INDEX idx_favorite_group_user ON content_favorite_group (user_id, is_default DESC, created_at DESC);
CREATE UNIQUE INDEX uk_favorite_group_default ON content_favorite_group (user_id) WHERE is_default = 1;

COMMENT ON TABLE content_favorite_group IS 'Favorite group table';

-- =============================================
-- 9. Content Favorite Record Table
-- =============================================

DROP TABLE IF EXISTS content_favorite_record CASCADE;

CREATE TABLE content_favorite_record (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    content_id  BIGINT       NOT NULL,
    group_id    BIGINT       NOT NULL,
    tags        JSONB        NULL,
    note        VARCHAR(255) NULL,
    source      SMALLINT     NULL,
    extra       JSONB        NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_favorite_record UNIQUE (user_id, content_id)
);

CREATE INDEX idx_favorite_record_group ON content_favorite_record (group_id, created_at DESC)
    WHERE deleted = 0;
CREATE INDEX idx_favorite_record_user ON content_favorite_record (user_id, created_at DESC)
    WHERE deleted = 0;

COMMENT ON TABLE content_favorite_record IS 'Favorite record table';

-- =============================================
-- 10. Content Interaction Table
-- =============================================

DROP TABLE IF EXISTS content_interaction CASCADE;

CREATE TABLE content_interaction (
    id              BIGINT      PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    content_id      BIGINT      NOT NULL,
    interaction_type SMALLINT   NOT NULL,
    device_info     JSONB       NULL,
    ip_address      INET        NULL,
    user_agent      VARCHAR(255) NULL,
    source          VARCHAR(32)  NULL,
    extra_data      JSONB       NULL,
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_content_interaction UNIQUE (content_id, user_id, interaction_type)
);

CREATE INDEX idx_content_interaction_user ON content_interaction (user_id, create_time DESC);
CREATE INDEX idx_content_interaction_content ON content_interaction (content_id, interaction_type, create_time DESC);

COMMENT ON TABLE content_interaction IS 'Content interaction record table';
COMMENT ON COLUMN content_interaction.interaction_type IS 'Interaction type: 1=view 2=like 3=favorite 4=share';

-- =============================================
-- 11. Content Feed Ad Table
-- =============================================

DROP TABLE IF EXISTS content_ad CASCADE;

CREATE TABLE content_ad (
    id             BIGINT       PRIMARY KEY,
    title          VARCHAR(128) NOT NULL,
    sub_title      VARCHAR(255) NULL,
    card_type      VARCHAR(32)  NULL,
    media_type     VARCHAR(32)  NULL,
    cover_image    VARCHAR(255) NULL,
    video_url      VARCHAR(255) NULL,
    jump_url       VARCHAR(255) NULL,
    call_to_action VARCHAR(64)  NULL,
    advertiser_name VARCHAR(128) NULL,
    style_meta     JSONB        NULL,
    status         SMALLINT     NOT NULL DEFAULT 1,
    priority       SMALLINT     NOT NULL DEFAULT 0,
    display_scene  SMALLINT     NOT NULL DEFAULT 1,
    frequency_cap  SMALLINT     NULL,
    start_time     TIMESTAMPTZ  NULL,
    end_time       TIMESTAMPTZ  NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted        SMALLINT     NOT NULL DEFAULT 0
);

CREATE INDEX idx_content_ad_scene ON content_ad (display_scene, status) WHERE deleted = 0;
CREATE INDEX idx_content_ad_time ON content_ad (start_time, end_time);

COMMENT ON TABLE content_ad IS 'Feed advertisement configuration table';

-- =============================================
-- 12. Ad Slot Configuration Table
-- =============================================

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

COMMENT ON TABLE content_ad_slot IS 'Ad slot configuration table';
COMMENT ON COLUMN content_ad_slot.pricing_mode IS 'Pricing mode: 1=CPC 2=CPM 3=CPA';

-- Insert default ad slots
INSERT INTO content_ad_slot (id, code, name, position, support_type, pricing_mode, base_price, status)
VALUES
(1, 'HOME_BANNER', 'Home Banner', 'home_top', '["image", "video"]', 2, 100.00, 1),
(2, 'FEED_INSERT', 'Feed Insert', 'feed_middle', '["image", "video", "native"]', 1, 0.50, 1),
(3, 'DETAIL_BOTTOM', 'Detail Bottom', 'detail_bottom', '["image", "text"]', 1, 0.30, 1)
ON CONFLICT DO NOTHING;

COMMIT;
