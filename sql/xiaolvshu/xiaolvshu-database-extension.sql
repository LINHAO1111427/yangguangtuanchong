-- =============================================
-- 阳光团宠平台数据库扩展设计 - 基于芋道框架
-- 只包含芋道框架没有的特有功能表
-- 创建时间: 2025-01-08
-- =============================================

-- =============================================
-- 1. 内容相关表（基于芋道基础）
-- =============================================

-- 作品表（扩展内容功能）
DROP TABLE IF EXISTS content_post CASCADE;
CREATE TABLE content_post (
    id               BIGINT       PRIMARY KEY,
    author_id        BIGINT       NOT NULL,
    type             SMALLINT     NOT NULL DEFAULT 0,  -- 0图文1视频2广告
    title            VARCHAR(120) NULL,
    content          TEXT         NULL,
    media_urls       JSONB        NULL,  -- 媒体文件URLs
    cover_image      VARCHAR(255) NULL,
    topic_id         BIGINT       NULL,
    tags             JSONB        NULL,
    location         JSONB        NULL,   -- 位置信息
    audit_status     VARCHAR(16)  NOT NULL DEFAULT 'pending',
    audit_result     JSONB        NULL,
    audit_time       TIMESTAMPTZ  NULL,
    auditor_id       BIGINT       NULL,
    is_ad            BOOLEAN      NOT NULL DEFAULT FALSE,
    boost_level      SMALLINT     NOT NULL DEFAULT 0,  -- 热推等级
    boost_expire_at  TIMESTAMPTZ  NULL,
    shop_code        VARCHAR(32)  NULL,   -- 商品口令码
    publish_time     TIMESTAMPTZ  NULL,
    expire_time      TIMESTAMPTZ  NULL,
    view_count       INTEGER      NOT NULL DEFAULT 0,
    like_count       INTEGER      NOT NULL DEFAULT 0,
    comment_count    INTEGER      NOT NULL DEFAULT 0,
    share_count      INTEGER      NOT NULL DEFAULT 0,
    collect_count    INTEGER      NOT NULL DEFAULT 0,
    creator          VARCHAR(64)  NULL DEFAULT '',
    create_time      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater          VARCHAR(64)  NULL DEFAULT '',
    update_time      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted          SMALLINT     NOT NULL DEFAULT 0,
    tenant_id        BIGINT       NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- 创建分区
CREATE TABLE content_post_2025_01 PARTITION OF content_post 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE content_post_2025_02 PARTITION OF content_post 
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

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
    creator     VARCHAR(64)  NULL DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64)  NULL DEFAULT '',
    update_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    tenant_id   BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_content_topic_name UNIQUE (name)
);

-- =============================================
-- 2. 互动系统表（基于芋道基础）
-- =============================================

-- 点赞表
DROP TABLE IF EXISTS interaction_like CASCADE;
CREATE TABLE interaction_like (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    target_type SMALLINT    NOT NULL,  -- 0作品1评论
    target_id   BIGINT      NOT NULL,
    state       SMALLINT    NOT NULL DEFAULT 1,  -- 1点赞0取消
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_interaction_like UNIQUE (user_id, target_type, target_id)
) PARTITION BY HASH (user_id);

-- 创建分区
CREATE TABLE interaction_like_p0 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE interaction_like_p1 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE interaction_like_p2 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE interaction_like_p3 PARTITION OF interaction_like FOR VALUES WITH (MODULUS 16, REMAINDER 3);

-- 评论表
DROP TABLE IF EXISTS interaction_comment CASCADE;
CREATE TABLE interaction_comment (
    id           BIGINT      PRIMARY KEY,
    post_id      BIGINT      NOT NULL,
    parent_id    BIGINT      NULL,
    root_id      BIGINT      NULL,
    user_id      BIGINT      NOT NULL,
    content      TEXT        NOT NULL,
    media        JSONB       NULL,  -- 评论图片/视频/GIF
    ip_location  VARCHAR(64) NULL,
    status       SMALLINT    NOT NULL DEFAULT 1,
    like_count   INTEGER     NOT NULL DEFAULT 0,
    reply_count  INTEGER     NOT NULL DEFAULT 0,
    creator      VARCHAR(64) NULL DEFAULT '',
    create_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater      VARCHAR(64) NULL DEFAULT '',
    update_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted      SMALLINT    NOT NULL DEFAULT 0,
    tenant_id    BIGINT      NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- 收藏表
DROP TABLE IF EXISTS interaction_favorite CASCADE;
CREATE TABLE interaction_favorite (
    id         BIGINT      PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL,
    folder_id  BIGINT      NULL,
    tags       JSONB       NULL,  -- 收藏标签分组
    state      SMALLINT    NOT NULL DEFAULT 1,
    creator    VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater    VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted    SMALLINT    NOT NULL DEFAULT 0,
    tenant_id  BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_interaction_favorite UNIQUE (user_id, post_id)
);

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
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- =============================================
-- 3. 商城扩展功能表
-- =============================================

-- 商品回收表
DROP TABLE IF EXISTS shop_recycle CASCADE;
CREATE TABLE shop_recycle (
    id               BIGINT         PRIMARY KEY,
    recycle_no       VARCHAR(32)    NOT NULL,  -- 回收单号
    user_id          BIGINT         NOT NULL,  -- 回收用户
    product_name     VARCHAR(128)   NOT NULL,  -- 商品名称
    product_images   JSONB          NOT NULL,  -- 商品图片
    product_desc     TEXT           NULL,      -- 商品描述
    original_price   DECIMAL(10,2)  NULL,      -- 原价
    estimate_price   DECIMAL(10,2)  NOT NULL,  -- 估价
    final_price      DECIMAL(10,2)  NULL,      -- 最终回收价
    status           SMALLINT       NOT NULL DEFAULT 0,  -- 0待审核1已接收2已完成3已拒绝
    audit_time       TIMESTAMPTZ    NULL,
    auditor_id       BIGINT         NULL,
    audit_remark     VARCHAR(255)   NULL,
    logistics_info   JSONB          NULL,      -- 物流信息
    creator          VARCHAR(64)    NULL DEFAULT '',
    create_time      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updater          VARCHAR(64)    NULL DEFAULT '',
    update_time      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted          SMALLINT       NOT NULL DEFAULT 0,
    tenant_id        BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_shop_recycle_no UNIQUE (recycle_no)
);

-- 用户信誉值表
DROP TABLE IF EXISTS member_credit CASCADE;
CREATE TABLE member_credit (
    user_id         BIGINT      PRIMARY KEY,
    credit_score    INTEGER     NOT NULL DEFAULT 100,  -- 信誉分（100满分）
    credit_level    VARCHAR(16) NOT NULL DEFAULT 'good',  -- 信誉等级
    total_orders    INTEGER     NOT NULL DEFAULT 0,      -- 总订单数
    completed_orders INTEGER    NOT NULL DEFAULT 0,      -- 完成订单数
    violation_count INTEGER     NOT NULL DEFAULT 0,      -- 违规次数
    last_violation_time TIMESTAMPTZ NULL,               -- 最后违规时间
    creator         VARCHAR(64) NULL DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) NULL DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT    NOT NULL DEFAULT 0,
    tenant_id       BIGINT      NOT NULL DEFAULT 0
);

-- 商品口令码表
DROP TABLE IF EXISTS shop_product_code CASCADE;
CREATE TABLE shop_product_code (
    id           BIGINT       PRIMARY KEY,
    code         VARCHAR(32)  NOT NULL,  -- 口令码
    product_id   BIGINT       NOT NULL,  -- 关联商品
    product_name VARCHAR(128) NOT NULL,  -- 商品名称
    status       SMALLINT     NOT NULL DEFAULT 1,  -- 1有效0失效
    use_count    INTEGER      NOT NULL DEFAULT 0,  -- 使用次数
    max_use      INTEGER      NOT NULL DEFAULT 0,  -- 最大使用次数(0不限制)
    expired_at   TIMESTAMPTZ  NULL,      -- 过期时间
    creator      VARCHAR(64)  NULL DEFAULT '',
    create_time  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater      VARCHAR(64)  NULL DEFAULT '',
    update_time  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    tenant_id    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_shop_product_code UNIQUE (code)
);

-- VIP会员表（扩展芋道member）
DROP TABLE IF EXISTS member_vip CASCADE;
CREATE TABLE member_vip (
    id              BIGINT      PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    vip_level       SMALLINT    NOT NULL DEFAULT 0,  -- VIP等级
    vip_expire_time TIMESTAMPTZ NULL,     -- VIP过期时间
    ad_watch_count  INTEGER     NOT NULL DEFAULT 0,  -- 今日看广告次数
    last_ad_time    TIMESTAMPTZ NULL,     -- 最后看广告时间
    total_ad_count  INTEGER     NOT NULL DEFAULT 0,  -- 总看广告次数
    privileges      JSONB       NULL,     -- VIP特权配置
    creator         VARCHAR(64) NULL DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) NULL DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT    NOT NULL DEFAULT 0,
    tenant_id       BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_member_vip_user UNIQUE (user_id)
);

-- 代币券表（扩展芋道promotion）
DROP TABLE IF EXISTS promotion_token_coupon CASCADE;
CREATE TABLE promotion_token_coupon (
    id               BIGINT         PRIMARY KEY,
    name             VARCHAR(64)    NOT NULL,  -- 券名称
    token_amount     INTEGER        NOT NULL,  -- 代币数量
    discount_amount  DECIMAL(10,2)  NULL,      -- 折扣金额
    discount_percent DECIMAL(5,2)   NULL,      -- 折扣百分比
    min_amount       DECIMAL(10,2)  NOT NULL DEFAULT 0,  -- 最小使用金额
    total_count      INTEGER        NOT NULL,  -- 总发放数量
    used_count       INTEGER        NOT NULL DEFAULT 0,  -- 已使用数量
    start_time       TIMESTAMPTZ    NOT NULL,  -- 开始时间
    end_time         TIMESTAMPTZ    NOT NULL,  -- 结束时间
    status           SMALLINT       NOT NULL DEFAULT 1,  -- 1启用0禁用
    creator          VARCHAR(64)    NULL DEFAULT '',
    create_time      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updater          VARCHAR(64)    NULL DEFAULT '',
    update_time      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted          SMALLINT       NOT NULL DEFAULT 0,
    tenant_id        BIGINT         NOT NULL DEFAULT 0
);

-- 用户代币券记录
DROP TABLE IF EXISTS promotion_user_token_coupon CASCADE;
CREATE TABLE promotion_user_token_coupon (
    id           BIGINT      PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    coupon_id    BIGINT      NOT NULL,
    order_id     BIGINT      NULL,      -- 使用的订单ID
    status       SMALLINT    NOT NULL DEFAULT 1,  -- 1未使用2已使用3已过期
    use_time     TIMESTAMPTZ NULL,      -- 使用时间
    expire_time  TIMESTAMPTZ NOT NULL,  -- 过期时间
    creator      VARCHAR(64) NULL DEFAULT '',
    create_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater      VARCHAR(64) NULL DEFAULT '',
    update_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted      SMALLINT    NOT NULL DEFAULT 0,
    tenant_id    BIGINT      NOT NULL DEFAULT 0
);

-- 抽奖活动表
DROP TABLE IF EXISTS promotion_lottery CASCADE;
CREATE TABLE promotion_lottery (
    id          BIGINT      PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,    -- 抽奖名称
    type        SMALLINT    NOT NULL,    -- 1特惠区抽奖2普通抽奖
    cost_tokens INTEGER     NOT NULL,    -- 消耗代币
    prizes      JSONB       NOT NULL,    -- 奖品配置
    start_time  TIMESTAMPTZ NOT NULL,    -- 开始时间
    end_time    TIMESTAMPTZ NOT NULL,    -- 结束时间
    status      SMALLINT    NOT NULL DEFAULT 1,  -- 1启用0禁用
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- 抽奖记录表
DROP TABLE IF EXISTS promotion_lottery_record CASCADE;
CREATE TABLE promotion_lottery_record (
    id          BIGINT      PRIMARY KEY,
    lottery_id  BIGINT      NOT NULL,    -- 抽奖活动ID
    user_id     BIGINT      NOT NULL,    -- 用户ID
    cost_tokens INTEGER     NOT NULL,    -- 消耗代币
    prize_type  SMALLINT    NOT NULL,    -- 奖品类型
    prize_name  VARCHAR(64) NOT NULL,    -- 奖品名称
    prize_value JSONB       NULL,        -- 奖品详情
    is_winner   BOOLEAN     NOT NULL DEFAULT FALSE,  -- 是否中奖
    claim_status SMALLINT   NOT NULL DEFAULT 0,  -- 0未领取1已领取
    claim_time  TIMESTAMPTZ NULL,        -- 领取时间
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- 免单活动表
DROP TABLE IF EXISTS promotion_free_order CASCADE;
CREATE TABLE promotion_free_order (
    id          BIGINT      PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,    -- 活动名称
    description TEXT        NULL,        -- 活动描述
    product_ids JSONB       NOT NULL,    -- 参与商品IDs
    total_quota INTEGER     NOT NULL,    -- 总免单名额
    used_quota  INTEGER     NOT NULL DEFAULT 0,  -- 已使用名额
    condition_type SMALLINT NOT NULL,    -- 条件类型：1分享2评论3关注
    start_time  TIMESTAMPTZ NOT NULL,    -- 开始时间
    end_time    TIMESTAMPTZ NOT NULL,    -- 结束时间
    status      SMALLINT    NOT NULL DEFAULT 1,  -- 1启用0禁用
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- 团购活动表
DROP TABLE IF EXISTS promotion_group_buy CASCADE;
CREATE TABLE promotion_group_buy (
    id              BIGINT         PRIMARY KEY,
    name            VARCHAR(64)    NOT NULL,    -- 团购名称
    product_id      BIGINT         NOT NULL,    -- 商品ID
    min_people      INTEGER        NOT NULL,    -- 最少成团人数
    max_people      INTEGER        NOT NULL,    -- 最多成团人数
    original_price  DECIMAL(10,2)  NOT NULL,    -- 原价
    group_price     DECIMAL(10,2)  NOT NULL,    -- 团购价
    start_time      TIMESTAMPTZ    NOT NULL,    -- 开始时间
    end_time        TIMESTAMPTZ    NOT NULL,    -- 结束时间
    status          SMALLINT       NOT NULL DEFAULT 1,  -- 1启用0禁用
    creator         VARCHAR(64)    NULL DEFAULT '',
    create_time     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)    NULL DEFAULT '',
    update_time     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    tenant_id       BIGINT         NOT NULL DEFAULT 0
);

-- 团购参与记录表
DROP TABLE IF EXISTS promotion_group_buy_record CASCADE;
CREATE TABLE promotion_group_buy_record (
    id            BIGINT      PRIMARY KEY,
    group_buy_id  BIGINT      NOT NULL,    -- 团购活动ID
    group_no      VARCHAR(32) NOT NULL,    -- 团购编号
    leader_id     BIGINT      NOT NULL,    -- 团长ID
    user_id       BIGINT      NOT NULL,    -- 参与用户ID
    order_id      BIGINT      NULL,        -- 订单ID
    status        SMALLINT    NOT NULL DEFAULT 0,  -- 0进行中1成功2失败3已取消
    join_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),  -- 参团时间
    expire_time   TIMESTAMPTZ NOT NULL,    -- 过期时间
    creator       VARCHAR(64) NULL DEFAULT '',
    create_time   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater       VARCHAR(64) NULL DEFAULT '',
    update_time   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted       SMALLINT    NOT NULL DEFAULT 0,
    tenant_id     BIGINT      NOT NULL DEFAULT 0
);

-- 先用再付订单表
DROP TABLE IF EXISTS trade_deferred_payment CASCADE;
CREATE TABLE trade_deferred_payment (
    id              BIGINT         PRIMARY KEY,
    order_id        BIGINT         NOT NULL,    -- 关联订单
    user_id         BIGINT         NOT NULL,    -- 用户ID
    payment_password VARCHAR(128)  NOT NULL,    -- 支付密码(加密)
    auto_pay_time   TIMESTAMPTZ    NOT NULL,    -- 自动扣款时间
    status          SMALLINT       NOT NULL DEFAULT 0,  -- 0待发货1已发货2已扣款3扣款失败
    pay_time        TIMESTAMPTZ    NULL,        -- 实际扣款时间
    fail_reason     VARCHAR(255)   NULL,        -- 扣款失败原因
    creator         VARCHAR(64)    NULL DEFAULT '',
    create_time     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)    NULL DEFAULT '',
    update_time     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    tenant_id       BIGINT         NOT NULL DEFAULT 0
);

-- 门店信息表
DROP TABLE IF EXISTS shop_store CASCADE;
CREATE TABLE shop_store (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,    -- 门店名称
    address     VARCHAR(255) NOT NULL,    -- 门店地址
    longitude   DECIMAL(10,6) NOT NULL,   -- 经度
    latitude    DECIMAL(10,6) NOT NULL,   -- 纬度
    phone       VARCHAR(20)  NULL,        -- 联系电话
    business_hours VARCHAR(128) NULL,     -- 营业时间
    images      JSONB        NULL,        -- 门店图片
    status      SMALLINT     NOT NULL DEFAULT 1,  -- 1营业0关闭
    creator     VARCHAR(64)  NULL DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64)  NULL DEFAULT '',
    update_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    tenant_id   BIGINT       NOT NULL DEFAULT 0
);

-- 交易快照表（用于数据导出认证）
DROP TABLE IF EXISTS trade_snapshot CASCADE;
CREATE TABLE trade_snapshot (
    id           BIGINT      PRIMARY KEY,
    order_id     BIGINT      NOT NULL,    -- 订单ID
    user_id      BIGINT      NOT NULL,    -- 用户ID
    snapshot_data JSONB      NOT NULL,    -- 快照数据
    snapshot_hash VARCHAR(64) NOT NULL,   -- 数据哈希（防篡改）
    export_format VARCHAR(16) NOT NULL,   -- 导出格式：json/image/pdf
    export_url   VARCHAR(255) NULL,       -- 导出文件URL
    creator      VARCHAR(64) NULL DEFAULT '',
    create_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater      VARCHAR(64) NULL DEFAULT '',
    update_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted      SMALLINT    NOT NULL DEFAULT 0,
    tenant_id    BIGINT      NOT NULL DEFAULT 0
);

-- =============================================
-- 4. 消息系统表（与聊天系统共用）
-- =============================================

-- 消息会话表
DROP TABLE IF EXISTS message_thread CASCADE;
CREATE TABLE message_thread (
    id                BIGINT       PRIMARY KEY,
    type              SMALLINT     NOT NULL,  -- 0系统1私信2群组3商城客服
    biz_type          VARCHAR(16)  NULL,      -- 业务类型：shop/content/system
    biz_id            BIGINT       NULL,      -- 业务ID
    title             VARCHAR(64)  NULL,      -- 会话标题
    avatar            VARCHAR(255) NULL,      -- 会话头像
    last_message_id   BIGINT       NULL,      -- 最后消息ID
    last_message_time TIMESTAMPTZ  NULL,      -- 最后消息时间
    participant_count INTEGER      NOT NULL DEFAULT 0,  -- 参与人数
    status            SMALLINT     NOT NULL DEFAULT 1,   -- 1正常0关闭
    creator           VARCHAR(64)  NULL DEFAULT '',
    create_time       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater           VARCHAR(64)  NULL DEFAULT '',
    update_time       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted           SMALLINT     NOT NULL DEFAULT 0,
    tenant_id         BIGINT       NOT NULL DEFAULT 0
);

-- 会话参与者表
DROP TABLE IF EXISTS message_participant CASCADE;
CREATE TABLE message_participant (
    id         BIGINT      PRIMARY KEY,
    thread_id  BIGINT      NOT NULL,    -- 会话ID
    user_id    BIGINT      NOT NULL,    -- 用户ID
    role       SMALLINT    NOT NULL DEFAULT 0,  -- 0普通1管理员2群主3客服
    join_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    leave_time TIMESTAMPTZ NULL,        -- 离开时间
    mute       BOOLEAN     NOT NULL DEFAULT FALSE,  -- 是否静音
    top        BOOLEAN     NOT NULL DEFAULT FALSE,  -- 是否置顶
    status     SMALLINT    NOT NULL DEFAULT 1,      -- 1正常0已离开
    creator    VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater    VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted    SMALLINT    NOT NULL DEFAULT 0,
    tenant_id  BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_message_participant UNIQUE (thread_id, user_id)
);

-- 消息详情表
DROP TABLE IF EXISTS message_detail CASCADE;
CREATE TABLE message_detail (
    id           BIGINT      PRIMARY KEY,
    thread_id    BIGINT      NOT NULL,    -- 会话ID
    sender_id    BIGINT      NOT NULL,    -- 发送者ID
    content_type SMALLINT    NOT NULL,    -- 1文本2图片3视频4语音5分享6系统7商品卡片
    content      JSONB       NOT NULL,    -- 消息内容
    ext          JSONB       NULL,        -- 扩展信息
    quote_id     BIGINT      NULL,        -- 引用消息ID
    send_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status       SMALLINT    NOT NULL DEFAULT 1,  -- 1正常0撤回
    trace_id     VARCHAR(64) NULL,        -- 链路追踪ID
    creator      VARCHAR(64) NULL DEFAULT '',
    create_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater      VARCHAR(64) NULL DEFAULT '',
    update_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted      SMALLINT    NOT NULL DEFAULT 0,
    tenant_id    BIGINT      NOT NULL DEFAULT 0
) PARTITION BY RANGE (send_time);

-- 创建分区
CREATE TABLE message_detail_2025_01 PARTITION OF message_detail 
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

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

-- 私信会话表
DROP TABLE IF EXISTS message_conversation CASCADE;
CREATE TABLE message_conversation (
    id                  BIGINT       PRIMARY KEY,
    user_id             BIGINT       NOT NULL,    -- 当前用户ID
    target_id           BIGINT       NOT NULL,    -- 对方用户ID
    type                SMALLINT     NOT NULL DEFAULT 1,  -- 会话类型：1私信
    last_message_id     BIGINT       NULL,        -- 最后一条消息ID
    last_message_content VARCHAR(255) NULL,       -- 最后消息内容摘要
    last_message_time   TIMESTAMPTZ  NULL,        -- 最后消息时间
    unread_count        INTEGER      NOT NULL DEFAULT 0,  -- 未读数
    is_top              SMALLINT     NOT NULL DEFAULT 0,  -- 是否置顶
    is_mute             SMALLINT     NOT NULL DEFAULT 0,  -- 是否免打扰
    deleted             SMALLINT     NOT NULL DEFAULT 0,  -- 是否删除
    creator             VARCHAR(64)  NULL DEFAULT '',
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater             VARCHAR(64)  NULL DEFAULT '',
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    tenant_id           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_message_conversation UNIQUE (user_id, target_id)
);

-- 私信消息表
DROP TABLE IF EXISTS message_private CASCADE;
CREATE TABLE message_private (
    id                  BIGINT       PRIMARY KEY,
    from_user_id        BIGINT       NOT NULL,    -- 发送者ID
    to_user_id          BIGINT       NOT NULL,    -- 接收者ID
    type                SMALLINT     NOT NULL DEFAULT 1,  -- 消息类型：1文本2图片3视频4语音5文件
    content             TEXT         NULL,        -- 消息内容
    extra_data          JSONB        NULL,        -- 扩展数据（如文件URL）
    status              SMALLINT     NOT NULL DEFAULT 0,  -- 0未读1已读2已撤回
    deleted             SMALLINT     NOT NULL DEFAULT 0,  -- 0正常1发送者删除2接收者删除3双方都删除
    read_time           TIMESTAMPTZ  NULL,        -- 读取时间
    creator             VARCHAR(64)  NULL DEFAULT '',
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater             VARCHAR(64)  NULL DEFAULT '',
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    tenant_id           BIGINT       NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- 创建消息分区
CREATE TABLE message_private_2025_01 PARTITION OF message_private
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE message_private_2025_02 PARTITION OF message_private
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- 群组表
DROP TABLE IF EXISTS group_info CASCADE;
CREATE TABLE group_info (
    id                  BIGINT       PRIMARY KEY,
    group_name          VARCHAR(64)  NOT NULL,    -- 群名称
    avatar              VARCHAR(255) NULL,        -- 群头像
    owner_user_id       BIGINT       NOT NULL,    -- 群主用户ID
    announcement        TEXT         NULL,        -- 群公告
    description         VARCHAR(255) NULL,        -- 群描述
    member_count        INTEGER      NOT NULL DEFAULT 1,  -- 成员数
    max_member_count    INTEGER      NOT NULL DEFAULT 500,  -- 最大成员数
    join_type           SMALLINT     NOT NULL DEFAULT 0,  -- 加入方式：0自由1需验证2禁止
    status              SMALLINT     NOT NULL DEFAULT 0,  -- 0正常1已解散
    mute_all            SMALLINT     NOT NULL DEFAULT 0,  -- 是否全员禁言
    creator             VARCHAR(64)  NULL DEFAULT '',
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater             VARCHAR(64)  NULL DEFAULT '',
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    dissolve_time       TIMESTAMPTZ  NULL,        -- 解散时间
    deleted             SMALLINT     NOT NULL DEFAULT 0,
    tenant_id           BIGINT       NOT NULL DEFAULT 0
);

-- 群成员表
DROP TABLE IF EXISTS group_member CASCADE;
CREATE TABLE group_member (
    id                  BIGINT       PRIMARY KEY,
    group_id            BIGINT       NOT NULL,    -- 群组ID
    user_id             BIGINT       NOT NULL,    -- 用户ID
    role                SMALLINT     NOT NULL DEFAULT 3,  -- 角色：1群主2管理员3普通成员
    status              SMALLINT     NOT NULL DEFAULT 0,  -- 0正常1已退出2被踢出
    muted               SMALLINT     NOT NULL DEFAULT 0,  -- 是否禁言
    mute_end_time       TIMESTAMPTZ  NULL,        -- 禁言到期时间
    nickname            VARCHAR(64)  NULL,        -- 群内昵称
    join_time           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),  -- 加入时间
    quit_time           TIMESTAMPTZ  NULL,        -- 退出/被踢时间
    creator             VARCHAR(64)  NULL DEFAULT '',
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater             VARCHAR(64)  NULL DEFAULT '',
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted             SMALLINT     NOT NULL DEFAULT 0,
    tenant_id           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_group_member UNIQUE (group_id, user_id)
);

-- 群聊消息表
DROP TABLE IF EXISTS group_message CASCADE;
CREATE TABLE group_message (
    id                  BIGINT       PRIMARY KEY,
    group_id            BIGINT       NOT NULL,    -- 群组ID
    from_user_id        BIGINT       NOT NULL,    -- 发送者ID
    type                SMALLINT     NOT NULL DEFAULT 1,  -- 消息类型：1文本2图片3视频4语音5文件10系统消息
    content             TEXT         NULL,        -- 消息内容
    extra_data          JSONB        NULL,        -- 扩展数据
    status              SMALLINT     NOT NULL DEFAULT 0,  -- 0正常1已撤回
    deleted             SMALLINT     NOT NULL DEFAULT 0,  -- 是否删除
    creator             VARCHAR(64)  NULL DEFAULT '',
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater             VARCHAR(64)  NULL DEFAULT '',
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    tenant_id           BIGINT       NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- 创建群消息分区
CREATE TABLE group_message_2025_01 PARTITION OF group_message
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE group_message_2025_02 PARTITION OF group_message
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- =============================================
-- 5. 任务奖励系统表
-- =============================================

-- 每日任务规则表
DROP TABLE IF EXISTS task_daily_rule CASCADE;
CREATE TABLE task_daily_rule (
    id            BIGINT      PRIMARY KEY,
    code          VARCHAR(32) NOT NULL,    -- 任务编码
    name          VARCHAR(64) NOT NULL,    -- 任务名称
    description   TEXT        NULL,        -- 任务描述
    trigger_event VARCHAR(32) NOT NULL,    -- 触发事件
    target_count  INTEGER     NOT NULL DEFAULT 1,  -- 目标次数
    reward_coin   INTEGER     NOT NULL DEFAULT 0,  -- 奖励代币
    reward_exp    INTEGER     NOT NULL DEFAULT 0,  -- 奖励经验
    status        SMALLINT    NOT NULL DEFAULT 1,  -- 1启用0禁用
    sort_order    INTEGER     NOT NULL DEFAULT 0,  -- 排序
    creator       VARCHAR(64) NULL DEFAULT '',
    create_time   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater       VARCHAR(64) NULL DEFAULT '',
    update_time   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted       SMALLINT    NOT NULL DEFAULT 0,
    tenant_id     BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_task_daily_rule_code UNIQUE (code)
);

-- 用户任务记录表
DROP TABLE IF EXISTS task_user_record CASCADE;
CREATE TABLE task_user_record (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,    -- 用户ID
    rule_code   VARCHAR(32) NOT NULL,    -- 任务编码
    biz_date    DATE        NOT NULL,    -- 业务日期
    progress    INTEGER     NOT NULL DEFAULT 0,  -- 进度
    status      SMALLINT    NOT NULL DEFAULT 0,  -- 0进行中1已完成2已领取
    reward_time TIMESTAMPTZ NULL,        -- 奖励时间
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_task_user_record UNIQUE (user_id, rule_code, biz_date)
);

-- 用户代币钱包表（扩展芋道pay_wallet）
DROP TABLE IF EXISTS pay_token_wallet CASCADE;
CREATE TABLE pay_token_wallet (
    id             BIGINT      PRIMARY KEY,
    user_id        BIGINT      NOT NULL,    -- 用户ID
    balance        INTEGER     NOT NULL DEFAULT 0,  -- 代币余额
    frozen_balance INTEGER     NOT NULL DEFAULT 0,  -- 冻结余额
    total_income   INTEGER     NOT NULL DEFAULT 0,  -- 总收入
    total_expense  INTEGER     NOT NULL DEFAULT 0,  -- 总支出
    creator        VARCHAR(64) NULL DEFAULT '',
    create_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater        VARCHAR(64) NULL DEFAULT '',
    update_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted        SMALLINT    NOT NULL DEFAULT 0,
    tenant_id      BIGINT      NOT NULL DEFAULT 0,
    version        INTEGER     NOT NULL DEFAULT 0,  -- 乐观锁版本号
    CONSTRAINT uk_pay_token_wallet_user UNIQUE (user_id)
);

-- 代币流水表
DROP TABLE IF EXISTS pay_token_wallet_transaction CASCADE;
CREATE TABLE pay_token_wallet_transaction (
    id            BIGINT       PRIMARY KEY,
    wallet_id     BIGINT       NOT NULL,    -- 钱包ID
    biz_type      VARCHAR(16)  NOT NULL,    -- 业务类型
    biz_id        VARCHAR(64)  NOT NULL,    -- 业务编号
    no            VARCHAR(64)  NOT NULL,    -- 流水号
    title         VARCHAR(128) NOT NULL,    -- 标题
    price         INTEGER      NOT NULL,    -- 变动金额（分）
    balance       INTEGER      NOT NULL,    -- 变动后余额
    creator       VARCHAR(64)  NULL DEFAULT '',
    create_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater       VARCHAR(64)  NULL DEFAULT '',
    update_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted       SMALLINT     NOT NULL DEFAULT 0,
    tenant_id     BIGINT       NOT NULL DEFAULT 0
) PARTITION BY RANGE (create_time);

-- =============================================
-- 6. 举报处罚系统表
-- =============================================

-- 举报表
DROP TABLE IF EXISTS report_complaint CASCADE;
CREATE TABLE report_complaint (
    id          BIGINT      PRIMARY KEY,
    complainant_id BIGINT   NOT NULL,    -- 举报人ID
    target_type SMALLINT    NOT NULL,    -- 举报类型：1内容2用户3商品4订单
    target_id   BIGINT      NOT NULL,    -- 举报目标ID
    reason_type SMALLINT    NOT NULL,    -- 举报原因类型
    reason_desc TEXT        NULL,        -- 举报原因描述
    evidence    JSONB       NULL,        -- 举报证据（图片、截图等）
    status      SMALLINT    NOT NULL DEFAULT 0,  -- 0待处理1已受理2已处理3已驳回
    handler_id  BIGINT      NULL,        -- 处理人ID
    handle_time TIMESTAMPTZ NULL,        -- 处理时间
    handle_result TEXT      NULL,        -- 处理结果
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- 处罚记录表
DROP TABLE IF EXISTS punishment_record CASCADE;
CREATE TABLE punishment_record (
    id          BIGINT      PRIMARY KEY,
    user_id     BIGINT      NOT NULL,    -- 被处罚用户ID
    report_id   BIGINT      NULL,        -- 关联举报ID
    type        SMALLINT    NOT NULL,    -- 处罚类型：1警告2禁言3封号4降信誉
    reason      TEXT        NOT NULL,    -- 处罚原因
    duration    INTEGER     NULL,        -- 处罚时长（小时）
    start_time  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_time    TIMESTAMPTZ NULL,        -- 处罚结束时间
    status      SMALLINT    NOT NULL DEFAULT 1,  -- 1生效0已撤销
    operator_id BIGINT      NOT NULL,    -- 操作人ID
    creator     VARCHAR(64) NULL DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater     VARCHAR(64) NULL DEFAULT '',
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT    NOT NULL DEFAULT 0,
    tenant_id   BIGINT      NOT NULL DEFAULT 0
);

-- =============================================
-- 7. 创建索引
-- =============================================

-- 内容相关索引
CREATE INDEX idx_content_post_author_time ON content_post (author_id, create_time DESC);
CREATE INDEX idx_content_post_status ON content_post (audit_status, publish_time DESC);
CREATE INDEX idx_content_post_topic ON content_post (topic_id) WHERE topic_id IS NOT NULL;
CREATE INDEX idx_content_post_shop_code ON content_post (shop_code) WHERE shop_code IS NOT NULL;

-- 互动相关索引
CREATE INDEX idx_interaction_like_target ON interaction_like (target_type, target_id) WHERE state = 1;
CREATE INDEX idx_interaction_comment_post ON interaction_comment (post_id, create_time DESC);
CREATE INDEX idx_interaction_favorite_user ON interaction_favorite (user_id, create_time DESC);

-- 商城相关索引
CREATE INDEX idx_shop_recycle_user ON shop_recycle (user_id, create_time DESC);
CREATE INDEX idx_shop_recycle_status ON shop_recycle (status, create_time DESC);
CREATE INDEX idx_shop_product_code_product ON shop_product_code (product_id, status);
CREATE INDEX idx_member_vip_expire ON member_vip (vip_expire_time) WHERE vip_expire_time IS NOT NULL;

-- 消息相关索引
CREATE INDEX idx_message_thread_type ON message_thread (type, last_message_time DESC);
CREATE INDEX idx_message_detail_thread_time ON message_detail (thread_id, send_time DESC);
CREATE INDEX idx_message_participant_user ON message_participant (user_id, status, top DESC);
CREATE INDEX idx_message_conversation_user ON message_conversation (user_id, update_time DESC);
CREATE INDEX idx_message_private_users ON message_private (from_user_id, to_user_id, create_time DESC);
CREATE INDEX idx_group_info_owner ON group_info (owner_user_id, create_time DESC);
CREATE INDEX idx_group_member_group ON group_member (group_id, status);
CREATE INDEX idx_group_member_user ON group_member (user_id, status);
CREATE INDEX idx_group_message_group_time ON group_message (group_id, create_time DESC);

-- 任务相关索引
CREATE INDEX idx_task_user_record_user_date ON task_user_record (user_id, biz_date DESC);
CREATE INDEX idx_task_user_record_status ON task_user_record (status, biz_date DESC);

-- 举报相关索引
CREATE INDEX idx_report_complaint_target ON report_complaint (target_type, target_id);
CREATE INDEX idx_report_complaint_status ON report_complaint (status, create_time DESC);
CREATE INDEX idx_punishment_record_user ON punishment_record (user_id, status, end_time);

-- =============================================
-- 8. 插入初始数据
-- =============================================

-- 插入默认任务规则
INSERT INTO task_daily_rule (id, code, name, description, trigger_event, target_count, reward_coin, reward_exp, status)
VALUES 
(1, 'DAILY_VIDEO_VIEW', '每日观看视频', '每日观看3个短视频获得奖励', 'video_view', 3, 10, 5, 1),
(2, 'DAILY_IMAGE_VIEW', '每日浏览图文', '每日浏览3篇图文获得奖励', 'image_view', 3, 10, 5, 1),
(3, 'DAILY_AD_VIEW', '每日观看广告', '每日观看1条广告获得奖励', 'ad_view', 1, 20, 10, 1),
(4, 'DAILY_LIKE', '每日点赞', '每日点赞5次获得奖励', 'like', 5, 5, 3, 1),
(5, 'DAILY_COMMENT', '每日评论', '每日评论2次获得奖励', 'comment', 2, 15, 8, 1),
(6, 'DAILY_SHARE', '每日分享', '每日分享1次内容获得奖励', 'share', 1, 12, 6, 1)
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
(8, '宠物日常', 'pet', '🐱', '宠物相关的可爱内容', 1, true),
(9, '手办收藏', 'collectible', '🎭', '手办模型收藏分享', 1, true),
(10, '二手闲置', 'secondhand', '♻️', '二手商品交易分享', 1, false)
ON CONFLICT DO NOTHING;

COMMIT;