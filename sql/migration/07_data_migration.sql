-- =============================================
-- Data Migration Scripts
-- 历史数据迁移脚本
-- 从infra_file迁移到content_file
-- 从system_users迁移到member_users
-- =============================================

-- =============================================
-- 1. 从infra_file迁移到content_file
-- 执行前请确保两个数据库都已创建
-- =============================================

-- 1.1 创建临时表记录迁移状态
DROP TABLE IF EXISTS migration_file_map CASCADE;

CREATE TABLE migration_file_map (
    id              BIGINT       PRIMARY KEY,
    old_file_id     BIGINT       NOT NULL,
    new_file_id     BIGINT       NOT NULL,
    file_path       VARCHAR(512) NOT NULL,
    migrated_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    status          SMALLINT     NOT NULL DEFAULT 0,
    error_msg       TEXT         NULL
);

COMMENT ON TABLE migration_file_map IS '文件迁移映射表';
COMMENT ON COLUMN migration_file_map.status IS '迁移状态：0=成功 1=失败';

-- 1.2 从infra_file迁移到content_file
-- 注意：请先确认infra库的连接串，使用dblink或手动导出导入

-- PostgreSQL跨库查询示例（需要dblink扩展）
CREATE EXTENSION IF NOT EXISTS dblink;

-- 迁移content相关文件
INSERT INTO content_file (
    id, config_id, name, path, url, type, size, hash,
    biz_type, author_id, status, storage_type, create_time, update_time, deleted
)
SELECT
    -- ID偏移避免冲突
    f.id + 10000000 AS id,
    f.config_id,
    f.name,
    f.path,
    f.url,
    f.type,
    f.size,
    NULL AS hash,  -- 历史数据需要后续补充hash

    -- 根据文件类型判断业务类型
    CASE
        WHEN f.type LIKE 'video/%' THEN 0
        WHEN f.type LIKE 'image/%' THEN 1
        ELSE 4
    END AS biz_type,

    -- 通过路径获取作者ID（需要后续补充）
    0 AS author_id,

    0 AS status,  -- 正常
    0 AS storage_type,  -- MinIO

    f.create_time,
    f.update_time,
    f.deleted
FROM dblink(
    'host=127.0.0.1 port=55432 dbname=ruoyi-vue-pro user=postgres password=postgres',
    'SELECT id, config_id, name, path, url, type, size, create_time, update_time, deleted
     FROM infra_file
     WHERE path LIKE ''content/%''
     ORDER BY id'
) AS f(
    id BIGINT,
    config_id BIGINT,
    name VARCHAR(256),
    path VARCHAR(512),
    url VARCHAR(1024),
    type VARCHAR(128),
    size BIGINT,
    create_time TIMESTAMPTZ,
    update_time TIMESTAMPTZ,
    deleted SMALLINT
)
WHERE NOT EXISTS (
    SELECT 1 FROM content_file cf WHERE cf.path = f.path
);

-- 记录迁移日志
INSERT INTO migration_file_map (id, old_file_id, new_file_id, file_path, status)
SELECT
    nextval('migration_file_map_seq'),
    f.id,
    f.id + 10000000,
    f.path,
    0
FROM dblink(
    'host=127.0.0.1 port=55432 dbname=ruoyi-vue-pro user=postgres password=postgres',
    'SELECT id, path FROM infra_file WHERE path LIKE ''content/%'''
) AS f(id BIGINT, path VARCHAR(512));

-- 1.3 根据content_post补充author_id
UPDATE content_file cf
SET author_id = cp.author_id
FROM content_post cp
WHERE cf.post_id IS NOT NULL
  AND cf.post_id = cp.id
  AND cf.author_id = 0;

-- 1.4 根据文件路径中的user目录补充author_id
-- 格式：content/video/2025/11/12/{userId}/xxx.mp4
UPDATE content_file
SET author_id = CAST(substring(path FROM 'content/video/\\d{4}/\\d{2}/\\d{2}/(\\d+)/') AS BIGINT)
WHERE author_id = 0
  AND path ~ 'content/video/\\d{4}/\\d{2}/\\d{2}/\\d+/';

-- 1.5 验证迁移结果
SELECT
    'Total migrated files' AS metric,
    COUNT(*) AS count
FROM content_file
WHERE id >= 10000000

UNION ALL

SELECT
    'Files with author_id' AS metric,
    COUNT(*) AS count
FROM content_file
WHERE id >= 10000000
  AND author_id > 0

UNION ALL

SELECT
    'Files without author_id' AS metric,
    COUNT(*) AS count
FROM content_file
WHERE id >= 10000000
  AND author_id = 0;

-- =============================================
-- 2. 从system_users迁移到member_users
-- =============================================

-- 2.1 创建迁移状态表
DROP TABLE IF EXISTS migration_user_map CASCADE;

CREATE TABLE migration_user_map (
    id              BIGINT       PRIMARY KEY,
    old_user_id     BIGINT       NOT NULL,
    new_user_id     BIGINT       NOT NULL,
    username        VARCHAR(30)  NOT NULL,
    migrated_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    status          SMALLINT     NOT NULL DEFAULT 0,
    error_msg       TEXT         NULL
);

COMMENT ON TABLE migration_user_map IS '用户迁移映射表';
COMMENT ON COLUMN migration_user_map.status IS '迁移状态：0=成功 1=失败';

-- 2.2 迁移C端用户（user_type = 1）
-- 注意：B端管理员（user_type = 0）不迁移，保留在system库

INSERT INTO member_users (
    id, username, password, nickname, mobile, email, avatar, status,
    register_ip, register_source, last_login_ip, last_login_time,
    points, level_id, experience,
    privacy_level, allow_message, allow_comment,
    follow_count, follower_count, post_count, like_count,
    ext_json,
    creator, create_time, updater, update_time, deleted
)
SELECT
    -- ID偏移避免冲突
    u.id + 10000000 AS id,
    u.username,
    u.password,  -- 密码hash保持不变，用户无需重新登录
    u.nickname,
    u.mobile,
    u.email,
    u.avatar,
    u.status,

    NULL AS register_ip,  -- system_users无此字段，需要后续补充
    0 AS register_source,  -- 默认APP注册
    NULL AS last_login_ip,
    NULL AS last_login_time,

    0 AS points,  -- 初始积分
    1 AS level_id,  -- 默认新手等级
    0 AS experience,

    0 AS privacy_level,  -- 默认公开
    1 AS allow_message,  -- 允许所有私信
    1 AS allow_comment,  -- 允许所有评论

    0 AS follow_count,  -- 后续从业务表统计
    0 AS follower_count,
    0 AS post_count,
    0 AS like_count,

    '{}' AS ext_json,

    u.creator,
    u.create_time,
    u.updater,
    u.update_time,
    u.deleted
FROM dblink(
    'host=127.0.0.1 port=55432 dbname=ruoyi-vue-pro user=postgres password=postgres',
    'SELECT id, username, password, nickname, mobile, email, avatar, status, creator, create_time, updater, update_time, deleted
     FROM system_users
     WHERE user_type = 1  -- 只迁移C端用户
     ORDER BY id'
) AS u(
    id BIGINT,
    username VARCHAR(30),
    password VARCHAR(100),
    nickname VARCHAR(30),
    mobile VARCHAR(11),
    email VARCHAR(50),
    avatar VARCHAR(255),
    status SMALLINT,
    creator VARCHAR(64),
    create_time TIMESTAMPTZ,
    updater VARCHAR(64),
    update_time TIMESTAMPTZ,
    deleted SMALLINT
)
WHERE NOT EXISTS (
    SELECT 1 FROM member_users mu WHERE mu.username = u.username
);

-- 记录迁移日志
INSERT INTO migration_user_map (id, old_user_id, new_user_id, username, status)
SELECT
    nextval('migration_user_map_seq'),
    u.id,
    u.id + 10000000,
    u.username,
    0
FROM dblink(
    'host=127.0.0.1 port=55432 dbname=ruoyi-vue-pro user=postgres password=postgres',
    'SELECT id, username FROM system_users WHERE user_type = 1'
) AS u(id BIGINT, username VARCHAR(30));

-- 2.3 补充注册IP（从登录日志中获取）
UPDATE member_users mu
SET register_ip = (
    SELECT ip
    FROM dblink(
        'host=127.0.0.1 port=55432 dbname=ruoyi-vue-pro user=postgres password=postgres',
        'SELECT user_id, ip FROM system_login_log WHERE status = 1'
    ) AS log(user_id BIGINT, ip VARCHAR(50))
    WHERE log.user_id = mu.id - 10000000
    ORDER BY log.user_id DESC
    LIMIT 1
)
WHERE mu.register_ip IS NULL;

-- 2.4 验证迁移结果
SELECT
    'Total migrated users' AS metric,
    COUNT(*) AS count
FROM member_users
WHERE id >= 10000000

UNION ALL

SELECT
    'Active users' AS metric,
    COUNT(*) AS count
FROM member_users
WHERE id >= 10000000
  AND status = 0

UNION ALL

SELECT
    'Average points' AS metric,
    AVG(points)::BIGINT AS count
FROM member_users
WHERE id >= 10000000;

-- =============================================
-- 3. 验证数据一致性
-- =============================================

-- 3.1 验证user_id在content_post中的关联
SELECT
    'Orphaned content posts' AS issue,
    COUNT(*) AS count
FROM content_post cp
LEFT JOIN member_users mu ON cp.author_id = mu.id
WHERE mu.id IS NULL
  AND cp.author_id != 0;

-- 3.2 验证author_id在content_file中的关联
SELECT
    'Orphaned content files' AS issue,
    COUNT(*) AS count
FROM content_file cf
LEFT JOIN member_users mu ON cf.author_id = mu.id
WHERE mu.id IS NULL
  AND cf.author_id != 0;

-- =============================================
-- 4. 回滚脚本（如需回滚，请执行）
-- =============================================

-- 4.1 删除member_users中迁移的数据
-- DELETE FROM member_users WHERE id >= 10000000;

-- 4.2 删除content_file中迁移的数据
-- DELETE FROM content_file WHERE id >= 10000000;

-- 4.3 删除迁移日志
-- DELETE FROM migration_user_map;
-- DELETE FROM migration_file_map;

-- =============================================
-- 5. 后续补充任务
-- =============================================

-- 5.1 补充文件hash（需要通过文件内容重新计算）
-- 需要开发Java程序读取文件重新计算hash并更新

-- 5.2 补充用户注册IP（从更多日志中分析）
-- 需要分析Nginx访问日志或其他业务日志

-- 5.3 统计用户post_count/follow_count等数据
-- 需要关联content_post、member_follow等表进行统计

-- =============================================
-- 完成
-- =============================================

COMMIT;

-- 显示迁移结果
SELECT
    'Migration completed' AS status,
    COUNT(*) AS total_records
FROM migration_user_map
UNION ALL
SELECT
    'File migration completed' AS status,
    COUNT(*)
FROM migration_file_map;

-- 检查失败记录
SELECT * FROM migration_user_map WHERE status = 1;
SELECT * FROM migration_file_map WHERE status = 1;
