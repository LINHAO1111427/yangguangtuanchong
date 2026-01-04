-- =============================================
-- 优化后的会员架构设计
-- 替代KIMI的05_create_member_tables.sql
-- 核心理念:共享用户主表 + C端扩展表
-- =============================================

-- 连接到核心数据库(不创建独立的member库!)
\c "ruoyi-vue-pro"

-- =============================================
-- 1. C端用户扩展资料表(核心⭐)
-- =============================================

DROP TABLE IF EXISTS member_profile CASCADE;

CREATE TABLE member_profile (
    id                 BIGINT PRIMARY KEY,
    user_id            BIGINT NOT NULL UNIQUE,  -- 关联system_users.id

    -- 🎯 custom_id(强制要求)
    custom_id          VARCHAR(9) NOT NULL UNIQUE,  -- 7-9位纯数字

    -- 积分与等级
    points             INTEGER NOT NULL DEFAULT 0,
    level_id           BIGINT NULL,
    experience         INTEGER NOT NULL DEFAULT 0,

    -- VIP体系(新增⭐)
    vip_level          SMALLINT NOT NULL DEFAULT 0,  -- 0=普通 1=临时VIP 2=永久VIP
    vip_expire_time    TIMESTAMPTZ NULL,              -- VIP过期时间

    -- 守护者体系(新增⭐)
    is_guardian        SMALLINT NOT NULL DEFAULT 0,   -- 是否守护者
    guardian_level     SMALLINT NULL,                 -- 守护等级

    -- 层级关系(新增⭐)
    parent_id          BIGINT NULL,                   -- 上级用户ID
    inviter_id         BIGINT NULL,                   -- 邀请人ID
    team_level         SMALLINT DEFAULT 1,            -- 团队层级(1-10)

    -- 隐私设置
    privacy_level      SMALLINT NOT NULL DEFAULT 0,
    allow_message      SMALLINT NOT NULL DEFAULT 1,
    allow_comment      SMALLINT NOT NULL DEFAULT 1,

    -- 统计信息(冗余字段,提升性能)
    follow_count       INTEGER NOT NULL DEFAULT 0,
    follower_count     INTEGER NOT NULL DEFAULT 0,
    post_count         INTEGER NOT NULL DEFAULT 0,
    like_count         INTEGER NOT NULL DEFAULT 0,

    -- 扩展字段
    ext_json           JSON NULL,

    -- 芋道标准字段
    creator            VARCHAR(64) DEFAULT '',
    create_time        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater            VARCHAR(64) DEFAULT '',
    update_time        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted            SMALLINT NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_member_user_id ON member_profile (user_id) WHERE deleted = 0;
CREATE UNIQUE INDEX uk_member_custom_id ON member_profile (custom_id) WHERE deleted = 0;
CREATE INDEX idx_member_parent_id ON member_profile (parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX idx_member_inviter_id ON member_profile (inviter_id) WHERE inviter_id IS NOT NULL;
CREATE INDEX idx_member_vip_expire ON member_profile (vip_expire_time) WHERE vip_level > 0 AND vip_expire_time IS NOT NULL;
CREATE INDEX idx_member_level ON member_profile (level_id) WHERE level_id IS NOT NULL;
CREATE INDEX idx_member_create_time ON member_profile (create_time DESC);

-- 添加注释
COMMENT ON TABLE member_profile IS 'C端用户扩展资料表(关联system_users)';
COMMENT ON COLUMN member_profile.user_id IS '关联system_users.id(一对一)';
COMMENT ON COLUMN member_profile.custom_id IS '7-9位纯数字ID,避免豹子号(111)和靓号(6666)';
COMMENT ON COLUMN member_profile.vip_level IS 'VIP等级:0=普通 1=临时VIP(5小时) 2=永久VIP';
COMMENT ON COLUMN member_profile.vip_expire_time IS 'VIP过期时间(仅vip_level>0时有效)';
COMMENT ON COLUMN member_profile.is_guardian IS '是否守护者:0=否 1=是';
COMMENT ON COLUMN member_profile.guardian_level IS '守护等级:1-10';
COMMENT ON COLUMN member_profile.parent_id IS '上级用户ID(用于层级体系)';
COMMENT ON COLUMN member_profile.inviter_id IS '邀请人ID(用于推广统计)';
COMMENT ON COLUMN member_profile.team_level IS '团队层级:1-10';

-- =============================================
-- 2. 会员等级配置表(与KIMI设计基本一致)
-- =============================================

DROP TABLE IF EXISTS member_level CASCADE;

CREATE TABLE member_level (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(30) NOT NULL,
    icon            VARCHAR(255) NULL,
    min_experience  INTEGER NOT NULL,
    max_experience  INTEGER NULL,
    discount_rate   DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    benefits        JSON NULL,
    is_default      SMALLINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    sort_order      INTEGER NOT NULL DEFAULT 999,

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_level_default ON member_level (is_default, sort_order);
CREATE INDEX idx_level_status ON member_level (status, sort_order);

COMMENT ON TABLE member_level IS '会员等级配置表';
COMMENT ON COLUMN member_level.discount_rate IS '折扣率(%)';
COMMENT ON COLUMN member_level.is_default IS '是否默认等级:0=否 1=是';
COMMENT ON COLUMN member_level.status IS '状态:0=禁用 1=启用';

-- 初始化等级数据
INSERT INTO member_level (id, name, min_experience, max_experience, discount_rate, is_default, sort_order)
VALUES
(1, '新手', 0, 99, 100.00, 1, 100),
(2, '达人', 100, 999, 98.00, 0, 200),
(3, '专家', 1000, 4999, 95.00, 0, 300),
(4, '大师', 5000, 19999, 90.00, 0, 400),
(5, '宗师', 20000, NULL, 85.00, 0, 500);

-- =============================================
-- 3. 团队层级关系表(闭包表设计⭐)
-- =============================================

DROP TABLE IF EXISTS member_team_hierarchy CASCADE;

CREATE TABLE member_team_hierarchy (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,      -- 用户ID
    ancestor_id     BIGINT NOT NULL,      -- 所有上级ID(包括直属和间接)
    level_diff      SMALLINT NOT NULL,    -- 层级差距(1=直属下级,2=二级下级...)

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 创建索引(关键性能优化!)
CREATE INDEX idx_team_user_id ON member_team_hierarchy (user_id);
CREATE INDEX idx_team_ancestor_id ON member_team_hierarchy (ancestor_id, level_diff);
CREATE UNIQUE INDEX uk_team_user_ancestor ON member_team_hierarchy (user_id, ancestor_id);

COMMENT ON TABLE member_team_hierarchy IS '团队层级关系闭包表(支持快速查询所有上级/下级)';
COMMENT ON COLUMN member_team_hierarchy.level_diff IS '层级差距:1=直属,2=二级,3=三级...';

-- 示例数据说明:
-- 用户A -> 用户B -> 用户D
--      -> 用户C
--
-- 闭包表记录:
-- user_id=B, ancestor_id=A, level_diff=1  (B的上级是A,差1级)
-- user_id=C, ancestor_id=A, level_diff=1  (C的上级是A,差1级)
-- user_id=D, ancestor_id=B, level_diff=1  (D的上级是B,差1级)
-- user_id=D, ancestor_id=A, level_diff=2  (D的上级是A,差2级) ← 闭包设计精髓!

-- =============================================
-- 4. VIP权限记录表
-- =============================================

DROP TABLE IF EXISTS member_vip_privilege CASCADE;

CREATE TABLE member_vip_privilege (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    privilege_type  SMALLINT NOT NULL,  -- 0=临时VIP(5小时) 1=永久VIP
    source          SMALLINT NOT NULL,  -- 0=看广告 1=充值购买 2=系统赠送 3=活动奖励
    duration_hours  INTEGER NULL,       -- 时长(小时),仅临时VIP有效
    expire_time     TIMESTAMPTZ NULL,   -- 过期时间

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_vip_user_id ON member_vip_privilege (user_id, expire_time DESC);
CREATE INDEX idx_vip_expire ON member_vip_privilege (expire_time) WHERE privilege_type = 0 AND expire_time IS NOT NULL;

COMMENT ON TABLE member_vip_privilege IS 'VIP权限记录表';
COMMENT ON COLUMN member_vip_privilege.privilege_type IS '权限类型:0=临时VIP 1=永久VIP';
COMMENT ON COLUMN member_vip_privilege.source IS '来源:0=看广告 1=充值 2=赠送 3=活动';
COMMENT ON COLUMN member_vip_privilege.duration_hours IS '时长(小时),临时VIP有效';

-- =============================================
-- 5. 访客记录表(按月分区)
-- =============================================

DROP TABLE IF EXISTS member_visitor CASCADE;

CREATE TABLE member_visitor (
    id              BIGINT NOT NULL,
    viewed_user_id  BIGINT NOT NULL,     -- 被访问的用户
    viewer_id       BIGINT NOT NULL,     -- 访客ID
    device_info     VARCHAR(200) NULL,   -- 设备信息
    ip_address      VARCHAR(50) NULL,    -- IP地址

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0,

    -- ⚠️ 重要：分区表的主键必须包含分区键(create_time)
    PRIMARY KEY (id, create_time)
) PARTITION BY RANGE (create_time);

-- 创建分区(每月一个分区,保留12个月)
CREATE TABLE member_visitor_2025_11 PARTITION OF member_visitor
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

CREATE TABLE member_visitor_2025_12 PARTITION OF member_visitor
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

-- 创建索引
CREATE INDEX idx_visitor_viewed_user ON member_visitor (viewed_user_id, create_time DESC);
CREATE INDEX idx_visitor_viewer_id ON member_visitor (viewer_id, create_time DESC);

COMMENT ON TABLE member_visitor IS '访客记录表(按月分区,保留12个月)';
COMMENT ON COLUMN member_visitor.viewed_user_id IS '被访问的用户ID';
COMMENT ON COLUMN member_visitor.viewer_id IS '访客用户ID';

-- =============================================
-- 6. 守护者记录表
-- =============================================

DROP TABLE IF EXISTS member_guardian CASCADE;

CREATE TABLE member_guardian (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,        -- 被守护的用户
    supporter_id    BIGINT NOT NULL,        -- 守护者(充值的粉丝)
    amount          DECIMAL(10,2) NOT NULL, -- 充值金额(520.99)
    guardian_type   SMALLINT NOT NULL,      -- 守护类型:1=520档 2=1314档 3=其他
    status          SMALLINT NOT NULL DEFAULT 1,  -- 状态:0=取消 1=生效

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_guardian_user_id ON member_guardian (user_id, create_time DESC);
CREATE INDEX idx_guardian_supporter_id ON member_guardian (supporter_id, create_time DESC);
CREATE INDEX idx_guardian_status ON member_guardian (status, create_time DESC);

COMMENT ON TABLE member_guardian IS '守护者记录表';
COMMENT ON COLUMN member_guardian.user_id IS '被守护的用户ID';
COMMENT ON COLUMN member_guardian.supporter_id IS '守护者ID(充值的粉丝)';
COMMENT ON COLUMN member_guardian.amount IS '充值金额(520.99/1314等)';
COMMENT ON COLUMN member_guardian.guardian_type IS '守护类型:1=520档 2=1314档';

-- =============================================
-- 7. 周守护排行表
-- =============================================

DROP TABLE IF EXISTS member_guardian_weekly_rank CASCADE;

CREATE TABLE member_guardian_weekly_rank (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    week_start_date DATE NOT NULL,          -- 周起始日期(周一)
    supporter_list  JSON NOT NULL,          -- [{supporter_id, nickname, avatar, amount, rank}]
    total_amount    DECIMAL(10,2) NOT NULL, -- 本周总金额
    supporter_count INTEGER NOT NULL DEFAULT 0,  -- 守护者人数

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_weekly_user_week ON member_guardian_weekly_rank (user_id, week_start_date);
CREATE INDEX idx_weekly_start_date ON member_guardian_weekly_rank (week_start_date DESC);

COMMENT ON TABLE member_guardian_weekly_rank IS '周守护排行表(每周一凌晨自动生成)';
COMMENT ON COLUMN member_guardian_weekly_rank.week_start_date IS '周起始日期(周一)';
COMMENT ON COLUMN member_guardian_weekly_rank.supporter_list IS 'JSON数组,存储本周守护者列表';

-- =============================================
-- 8. 积分流水表(与KIMI设计一致)
-- =============================================

DROP TABLE IF EXISTS member_points_record CASCADE;

CREATE TABLE member_points_record (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            SMALLINT NOT NULL,  -- 0=获得 1=消耗
    amount          INTEGER NOT NULL,
    balance         INTEGER NOT NULL,   -- 操作后余额
    biz_type        SMALLINT NOT NULL,  -- 业务类型
    biz_id          VARCHAR(64) NULL,
    description     VARCHAR(200) NULL,

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_points_user_time ON member_points_record (user_id, create_time DESC);
CREATE INDEX idx_points_biz ON member_points_record (biz_type, biz_id);
CREATE INDEX idx_points_create_time ON member_points_record (create_time DESC);

COMMENT ON TABLE member_points_record IS '会员积分流水表';
COMMENT ON COLUMN member_points_record.type IS '类型:0=获得 1=消耗';
COMMENT ON COLUMN member_points_record.biz_type IS '业务类型:0=签到 1=发布内容 2=消费抵扣 3=管理员调整';

-- =============================================
-- 9. 第三方授权绑定表(与KIMI设计一致)
-- =============================================

DROP TABLE IF EXISTS member_auth_bind CASCADE;

CREATE TABLE member_auth_bind (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    auth_type       SMALLINT NOT NULL,   -- 0=微信 1=QQ 2=微博 3=Apple
    openid          VARCHAR(100) NOT NULL,
    unionid         VARCHAR(100) NULL,
    access_token    VARCHAR(255) NULL,
    refresh_token   VARCHAR(255) NULL,
    expires_time    TIMESTAMPTZ NULL,
    nickname        VARCHAR(100) NULL,
    avatar          VARCHAR(255) NULL,

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_auth_openid ON member_auth_bind (auth_type, openid) WHERE deleted = 0;
CREATE INDEX idx_auth_user_id ON member_auth_bind (user_id);
CREATE INDEX idx_auth_unionid ON member_auth_bind (unionid) WHERE unionid IS NOT NULL;

COMMENT ON TABLE member_auth_bind IS '第三方授权绑定表';
COMMENT ON COLUMN member_auth_bind.auth_type IS '授权类型:0=微信 1=QQ 2=微博 3=Apple';

-- =============================================
-- 10. 关注关系表(与KIMI设计一致)
-- =============================================

DROP TABLE IF EXISTS member_follow CASCADE;

CREATE TABLE member_follow (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,     -- 关注者ID
    follow_id   BIGINT NOT NULL,     -- 被关注者ID
    status      SMALLINT NOT NULL DEFAULT 1,  -- 0=取消关注 1=已关注

    creator     VARCHAR(64) DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_follow_user_follow ON member_follow (user_id, follow_id) WHERE deleted = 0;
CREATE INDEX idx_follow_user_id ON member_follow (user_id, create_time DESC);
CREATE INDEX idx_follow_follow_id ON member_follow (follow_id, create_time DESC);

COMMENT ON TABLE member_follow IS '用户关注关系表';
COMMENT ON COLUMN member_follow.status IS '状态:0=取消关注 1=已关注';

-- =============================================
-- 11. 屏蔽列表表(与KIMI设计一致)
-- =============================================

DROP TABLE IF EXISTS member_block_list CASCADE;

CREATE TABLE member_block_list (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,     -- 屏蔽发起人
    block_id    BIGINT NOT NULL,     -- 被屏蔽对象ID
    block_type  SMALLINT NOT NULL DEFAULT 0,  -- 0=用户 1=内容 2=话题
    reason      VARCHAR(200) NULL,

    creator     VARCHAR(64) DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_block_user_target ON member_block_list (user_id, block_id, block_type) WHERE deleted = 0;
CREATE INDEX idx_block_user_id ON member_block_list (user_id, create_time DESC);

COMMENT ON TABLE member_block_list IS '用户屏蔽列表';
COMMENT ON COLUMN member_block_list.block_type IS '屏蔽类型:0=用户 1=内容 2=话题';

-- =============================================
-- 序列号创建(用于ID自增)
-- =============================================

CREATE SEQUENCE IF NOT EXISTS member_profile_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_level_seq INCREMENT BY 1 START WITH 100;
CREATE SEQUENCE IF NOT EXISTS member_team_hierarchy_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_vip_privilege_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_visitor_seq INCREMENT BY 1 START WITH 10000;
CREATE SEQUENCE IF NOT EXISTS member_guardian_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_guardian_weekly_rank_seq INCREMENT BY 1 START WITH 100;
CREATE SEQUENCE IF NOT EXISTS member_points_record_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_auth_bind_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_follow_seq INCREMENT BY 1 START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS member_block_list_seq INCREMENT BY 1 START WITH 1000;

-- =============================================
-- 函数: 查询用户所有下级(利用闭包表)
-- =============================================

CREATE OR REPLACE FUNCTION get_user_subordinates(p_user_id BIGINT, p_max_level INT DEFAULT 10)
RETURNS TABLE(
    subordinate_id BIGINT,
    level_diff SMALLINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.user_id AS subordinate_id,
        h.level_diff
    FROM member_team_hierarchy h
    WHERE h.ancestor_id = p_user_id
      AND h.level_diff <= p_max_level
    ORDER BY h.level_diff, h.user_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_user_subordinates(BIGINT, INT) IS '查询用户的所有下级(最多N级)';

-- 使用示例:
-- SELECT * FROM get_user_subordinates(100, 3);  -- 查询用户100的所有下级(最多3级)

-- =============================================
-- 函数: 更新VIP状态(定时任务调用)
-- =============================================

CREATE OR REPLACE FUNCTION update_expired_vip()
RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    -- 将过期的VIP用户降级为普通用户
    UPDATE member_profile
    SET
        vip_level = 0,
        vip_expire_time = NULL,
        updater = 'system',
        update_time = NOW()
    WHERE vip_level = 1  -- 仅处理临时VIP
      AND vip_expire_time IS NOT NULL
      AND vip_expire_time < NOW();

    GET DIAGNOSTICS expired_count = ROW_COUNT;

    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_expired_vip() IS '更新过期的VIP用户(定时任务每小时执行)';

-- =============================================
-- 触发器: 自动维护统计字段
-- =============================================

-- 关注数统计
CREATE OR REPLACE FUNCTION trg_update_follow_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- 增加关注数
        UPDATE member_profile SET follow_count = follow_count + 1 WHERE user_id = NEW.user_id;
        -- 增加粉丝数
        UPDATE member_profile SET follower_count = follower_count + 1 WHERE user_id = NEW.follow_id;
    ELSIF TG_OP = 'DELETE' OR (TG_OP = 'UPDATE' AND NEW.status = 0) THEN
        -- 减少关注数
        UPDATE member_profile SET follow_count = follow_count - 1 WHERE user_id = OLD.user_id AND follow_count > 0;
        -- 减少粉丝数
        UPDATE member_profile SET follower_count = follower_count - 1 WHERE user_id = OLD.follow_id AND follower_count > 0;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_follow_insert ON member_follow;
DROP TRIGGER IF EXISTS trg_follow_update ON member_follow;

CREATE TRIGGER trg_follow_insert
    AFTER INSERT ON member_follow
    FOR EACH ROW
    EXECUTE FUNCTION trg_update_follow_count();

CREATE TRIGGER trg_follow_update
    AFTER UPDATE ON member_follow
    FOR EACH ROW
    WHEN (OLD.status != NEW.status)
    EXECUTE FUNCTION trg_update_follow_count();

COMMENT ON FUNCTION trg_update_follow_count() IS '自动维护member_profile的关注数和粉丝数';

-- =============================================
-- 完成
-- =============================================

COMMIT;

-- 显示所有表
\dt member_*

-- 显示所有序列
SELECT sequence_name FROM information_schema.sequences WHERE sequence_name LIKE 'member_%';

-- 显示所有函数
\df get_user_subordinates
\df update_expired_vip

-- 验证member_profile表结构
\d member_profile
