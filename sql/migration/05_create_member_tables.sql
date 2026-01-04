-- =============================================
-- Member Module Tables - xiaolvshu_member database
-- Execute this after connecting to xiaolvshu_member database
-- 用户中心模块数据库建表脚本
-- =============================================

-- Connect to xiaolvshu_member database
\c xiaolvshu_member

-- =============================================
-- 1. C端用户主表（核心表）
-- =============================================

DROP TABLE IF EXISTS member_users CASCADE;

CREATE TABLE member_users (
    id                 BIGINT       PRIMARY KEY,
    username           VARCHAR(30)  NOT NULL,
    password           VARCHAR(100) NOT NULL,
    nickname           VARCHAR(30)  NOT NULL,
    mobile             VARCHAR(11)  NULL,
    email              VARCHAR(50)  NULL,
    avatar             VARCHAR(255) NULL,
    status             SMALLINT     NOT NULL DEFAULT 0,
    register_ip        VARCHAR(50)  NULL,
    register_source    SMALLINT     NOT NULL DEFAULT 0,
    last_login_ip      VARCHAR(50)  NULL,
    last_login_time    TIMESTAMPTZ  NULL,

    -- 积分与等级
    points             INTEGER      NOT NULL DEFAULT 0,
    level_id           BIGINT       NULL,
    experience         INTEGER      NOT NULL DEFAULT 0,

    -- 隐私设置
    privacy_level      SMALLINT     NOT NULL DEFAULT 0,
    allow_message      SMALLINT     NOT NULL DEFAULT 1,
    allow_comment      SMALLINT     NOT NULL DEFAULT 1,

    -- 统计信息
    follow_count       INTEGER      NOT NULL DEFAULT 0,
    follower_count     INTEGER      NOT NULL DEFAULT 0,
    post_count         INTEGER      NOT NULL DEFAULT 0,
    like_count         INTEGER      NOT NULL DEFAULT 0,

    -- 扩展字段
    ext_json           JSON         NULL,

    -- 芋道标准字段
    creator            VARCHAR(64)  DEFAULT '',
    create_time        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater            VARCHAR(64)  DEFAULT '',
    update_time        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted            SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_member_username ON member_users (username);
CREATE UNIQUE INDEX uk_member_mobile ON member_users (mobile) WHERE mobile IS NOT NULL;
CREATE INDEX idx_member_create_time ON member_users (create_time DESC);
CREATE INDEX idx_member_status ON member_users (status, create_time DESC);
CREATE INDEX idx_member_level ON member_users (level_id);

-- 添加注释
COMMENT ON TABLE member_users IS 'C端用户主表';
COMMENT ON COLUMN member_users.status IS '状态：0=正常 1=冻结 2=注销';
COMMENT ON COLUMN member_users.register_source IS '注册来源：0=APP 1=小程序 2=H5';
COMMENT ON COLUMN member_users.privacy_level IS '隐私等级：0=公开 1=粉丝 2=私密';
COMMENT ON COLUMN member_users.allow_message IS '是否允许私信：0=拒绝 1=所有 2=关注的人';

-- =============================================
-- 2. 会员等级配置表
-- =============================================

DROP TABLE IF EXISTS member_level CASCADE;

CREATE TABLE member_level (
    id              BIGINT       PRIMARY KEY,
    name            VARCHAR(30)  NOT NULL,
    icon            VARCHAR(255) NULL,
    min_experience  INTEGER      NOT NULL,
    max_experience  INTEGER      NULL,
    discount_rate   DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    benefits        JSON         NULL,
    is_default      SMALLINT     NOT NULL DEFAULT 0,
    status          SMALLINT     NOT NULL DEFAULT 1,
    sort_order      INTEGER      NOT NULL DEFAULT 999,

    creator         VARCHAR(64)  DEFAULT '',
    create_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)  DEFAULT '',
    update_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted         SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_level_default ON member_level (is_default, sort_order);
CREATE INDEX idx_level_status ON member_level (status, sort_order);

-- 添加注释
COMMENT ON TABLE member_level IS '会员等级配置表';
COMMENT ON COLUMN member_level.discount_rate IS '折扣率(%)';
COMMENT ON COLUMN member_level.is_default IS '是否默认等级：0=否 1=是';
COMMENT ON COLUMN member_level.status IS '状态：0=禁用 1=启用';

-- 初始化等级数据
INSERT INTO member_level (id, name, min_experience, max_experience, discount_rate, is_default, sort_order)
VALUES
(1, '新手', 0, 99, 100.00, 1, 100),
(2, '达人', 100, 999, 98.00, 0, 200),
(3, '专家', 1000, 4999, 95.00, 0, 300),
(4, '大师', 5000, 19999, 90.00, 0, 400),
(5, '宗师', 20000, NULL, 85.00, 0, 500);

-- =============================================
-- 3. 积分流水表
-- =============================================

DROP TABLE IF EXISTS member_points_record CASCADE;

CREATE TABLE member_points_record (
    id              BIGINT       PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    type            SMALLINT     NOT NULL,
    amount          INTEGER      NOT NULL,
    balance         INTEGER      NOT NULL,
    biz_type        SMALLINT     NOT NULL,
    biz_id          VARCHAR(64)  NULL,
    description     VARCHAR(200) NULL,

    creator         VARCHAR(64)  DEFAULT '',
    create_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)  DEFAULT '',
    update_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted         SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_points_user_time ON member_points_record (user_id, create_time DESC);
CREATE INDEX idx_points_biz ON member_points_record (biz_type, biz_id);
CREATE INDEX idx_points_create_time ON member_points_record (create_time DESC);

-- 添加注释
COMMENT ON TABLE member_points_record IS '会员积分流水表';
COMMENT ON COLUMN member_points_record.type IS '类型：0=获得 1=消耗';
COMMENT ON COLUMN member_points_record.biz_type IS '业务类型：0=签到 1=发布内容 2=消费抵扣 3=管理员调整';

-- =============================================
-- 4. 第三方授权绑定表
-- =============================================

DROP TABLE IF EXISTS member_auth_bind CASCADE;

CREATE TABLE member_auth_bind (
    id              BIGINT       PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    auth_type       SMALLINT     NOT NULL,
    openid          VARCHAR(100) NOT NULL,
    unionid         VARCHAR(100) NULL,
    access_token    VARCHAR(255) NULL,
    refresh_token   VARCHAR(255) NULL,
    expires_time    TIMESTAMPTZ  NULL,
    nickname        VARCHAR(100) NULL,
    avatar          VARCHAR(255) NULL,

    creator         VARCHAR(64)  DEFAULT '',
    create_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)  DEFAULT '',
    update_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted         SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_auth_openid ON member_auth_bind (auth_type, openid);
CREATE INDEX idx_auth_user_id ON member_auth_bind (user_id);
CREATE INDEX idx_auth_unionid ON member_auth_bind (unionid);

-- 添加注释
COMMENT ON TABLE member_auth_bind IS '第三方授权绑定表';
COMMENT ON COLUMN member_auth_bind.auth_type IS '授权类型：0=微信 1=QQ 2=微博 3=Apple';

-- =============================================
-- 5. 用户认证日志表（分区表）
-- =============================================

DROP TABLE IF EXISTS member_auth_log CASCADE;

CREATE TABLE member_auth_log (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    auth_type   SMALLINT     NOT NULL,
    ip          VARCHAR(50)  NULL,
    location    VARCHAR(100) NULL,
    device      VARCHAR(100) NULL,
    user_agent  VARCHAR(255) NULL,
    status      SMALLINT     NOT NULL,
    fail_reason VARCHAR(100) NULL,

    creator     VARCHAR(64)  DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- 创建分区（每年一个分区）
CREATE TABLE member_auth_log_2025 PARTITION OF member_auth_log
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE member_auth_log_2026 PARTITION OF member_auth_log
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

-- 创建索引
CREATE INDEX idx_auth_user_time ON member_auth_log (user_id, create_time DESC);
CREATE INDEX idx_auth_ip ON member_auth_log (ip, create_time DESC);
CREATE INDEX idx_auth_create_time ON member_auth_log (create_time DESC);

-- 添加注释
COMMENT ON TABLE member_auth_log IS '用户认证日志表（按年分区）';
COMMENT ON COLUMN member_auth_log.auth_type IS '认证类型：0=密码登录 1=短信登录 2=三方授权';
COMMENT ON COLUMN member_auth_log.status IS '状态：0=失败 1=成功';

-- =============================================
-- 6. 用户关注关系表
-- =============================================

DROP TABLE IF EXISTS member_follow CASCADE;

CREATE TABLE member_follow (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    follow_id   BIGINT       NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,

    creator     VARCHAR(64)  DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64)  DEFAULT '',
    update_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_follow_user_follow ON member_follow (user_id, follow_id);
CREATE INDEX idx_follow_user_id ON member_follow (user_id, create_time DESC);
CREATE INDEX idx_follow_follow_id ON member_follow (follow_id, create_time DESC);

-- 添加注释
COMMENT ON TABLE member_follow IS '用户关注关系表';
COMMENT ON COLUMN member_follow.status IS '状态：0=取消关注 1=已关注';

-- =============================================
-- 7. 用户屏蔽列表表
-- =============================================

DROP TABLE IF EXISTS member_block_list CASCADE;

CREATE TABLE member_block_list (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    block_id    BIGINT       NOT NULL,
    block_type  SMALLINT     NOT NULL DEFAULT 0,
    reason      VARCHAR(200) NULL,

    creator     VARCHAR(64)  DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64)  DEFAULT '',
    update_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_block_user_target ON member_block_list (user_id, block_id, block_type);
CREATE INDEX idx_block_user_id ON member_block_list (user_id, create_time DESC);

-- 添加注释
COMMENT ON TABLE member_block_list IS '用户屏蔽列表';
COMMENT ON COLUMN member_block_list.block_type IS '屏蔽类型：0=用户 1=内容 2=话题';

-- =============================================
-- 序列号创建（用于ID自增）
-- =============================================

CREATE SEQUENCE IF NOT EXISTS member_users_seq INCREMENT BY 1 MINVALUE 10000000 START WITH 10000000;
CREATE SEQUENCE IF NOT EXISTS member_level_seq INCREMENT BY 1 START WITH 100;
CREATE SEQUENCE IF NOT EXISTS member_points_record_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_auth_bind_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_auth_log_seq INCREMENT BY 1 START WITH 10000;
CREATE SEQUENCE IF NOT EXISTS member_follow_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_block_list_seq INCREMENT BY 1 START WITH 1000;

-- =============================================
-- 完成
-- =============================================

COMMIT;

-- 显示所有表
\dt
