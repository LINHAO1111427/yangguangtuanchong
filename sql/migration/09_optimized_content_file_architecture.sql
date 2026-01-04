-- =============================================
-- 优化后的内容文件存储架构
-- 替代KIMI的06_create_content_file_tables.sql
-- 核心理念:复用infra_file_config + 同库关联
-- =============================================

-- 连接到内容数据库
\c xiaolvshu

-- =============================================
-- 1. 内容文件元数据主表(优化版⭐)
-- =============================================

DROP TABLE IF EXISTS content_file CASCADE;

CREATE TABLE content_file (
    id               BIGINT PRIMARY KEY,

    -- 存储配置(复用infra_file_config!)
    config_id        BIGINT NOT NULL,  -- 关联infra_file_config.id(跨库,但查询少)

    -- 文件基础信息
    name             VARCHAR(256) NOT NULL,
    path             VARCHAR(512) NOT NULL,
    url              VARCHAR(1024) NOT NULL,
    type             VARCHAR(128) NULL,    -- MIME类型(video/mp4, image/jpeg等)
    size             BIGINT NOT NULL,
    hash             VARCHAR(64) NULL,     -- 文件哈希(用于秒传)

    -- 业务关联(关键修改⭐)
    biz_type         SMALLINT NOT NULL DEFAULT 0,  -- 0=视频 1=图片 2=封面 3=头像 4=其他
    author_id        BIGINT NOT NULL,               -- 关联system_users.id(同库!)
    post_id          BIGINT NULL,                   -- 关联content_post.id(同库!)

    -- 文件状态
    status           SMALLINT NOT NULL DEFAULT 0,   -- 0=正常 1=删除中 2=已归档 3=已删除
    storage_type     SMALLINT NOT NULL DEFAULT 0,   -- 0=MinIO 1=阿里云OSS 2=腾讯云COS

    -- 访问统计(冗余字段,提升性能)
    access_count     INTEGER NOT NULL DEFAULT 0,
    last_access_time TIMESTAMPTZ NULL,

    -- CDN相关
    cdn_url          VARCHAR(1024) NULL,
    cdn_status       SMALLINT NOT NULL DEFAULT 0,   -- 0=未预热 1=已预热 2=预热失败

    -- 生命周期管理
    archive_time     TIMESTAMPTZ NULL,              -- 归档时间
    delete_time      TIMESTAMPTZ NULL,              -- 删除时间

    -- 芋道标准字段
    creator          VARCHAR(64) DEFAULT '',
    create_time      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater          VARCHAR(64) DEFAULT '',
    update_time      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted          SMALLINT NOT NULL DEFAULT 0
);

-- 创建索引(关键性能优化!)
CREATE UNIQUE INDEX uk_content_file_hash ON content_file (hash) WHERE hash IS NOT NULL AND deleted = 0;
CREATE INDEX idx_content_file_author_time ON content_file (author_id, create_time DESC);
CREATE INDEX idx_content_file_post ON content_file (post_id) WHERE post_id IS NOT NULL;
CREATE INDEX idx_content_file_biz_type ON content_file (biz_type, create_time DESC);
CREATE INDEX idx_content_file_status ON content_file (status, create_time DESC);
CREATE INDEX idx_content_file_last_access ON content_file (last_access_time DESC) WHERE last_access_time IS NOT NULL;
CREATE INDEX idx_content_file_create_time ON content_file (create_time DESC);

-- 添加注释
COMMENT ON TABLE content_file IS '内容文件元数据表(独立于infra_file)';
COMMENT ON COLUMN content_file.config_id IS '存储配置ID(关联infra_file_config.id,跨库但查询少)';
COMMENT ON COLUMN content_file.hash IS '文件SHA256哈希(用于秒传和去重)';
COMMENT ON COLUMN content_file.biz_type IS '业务类型:0=视频 1=图片 2=封面 3=头像 4=其他';
COMMENT ON COLUMN content_file.author_id IS '作者ID(关联system_users.id,同库!)';
COMMENT ON COLUMN content_file.post_id IS '内容ID(关联content_post.id,同库!)';
COMMENT ON COLUMN content_file.status IS '状态:0=正常 1=删除中 2=已归档 3=已删除';
COMMENT ON COLUMN content_file.storage_type IS '存储类型:0=MinIO 1=阿里云OSS 2=腾讯云COS 3=AWS S3';
COMMENT ON COLUMN content_file.cdn_status IS 'CDN状态:0=未预热 1=已预热 2=预热失败';

-- =============================================
-- 2. 文件访问日志表(按月分区)
-- =============================================

DROP TABLE IF EXISTS content_file_access_log CASCADE;

CREATE TABLE content_file_access_log (
    id          BIGINT NOT NULL,
    file_id     BIGINT NOT NULL,
    user_id     BIGINT NULL,         -- 访问用户ID(可为空,匿名访问)
    ip          VARCHAR(50) NULL,
    user_agent  VARCHAR(255) NULL,
    referer     VARCHAR(255) NULL,
    status      SMALLINT NOT NULL DEFAULT 1,  -- 0=失败 1=成功
    error_msg   VARCHAR(200) NULL,
    cost_time   INTEGER NULL,        -- 耗时(ms)

    creator     VARCHAR(64) DEFAULT '',
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted     SMALLINT NOT NULL DEFAULT 0,

    -- ⚠️ 重要：分区表的主键必须包含分区键(create_time)
    PRIMARY KEY (id, create_time)
) PARTITION BY RANGE (create_time);

-- 创建分区(每月一个分区)
CREATE TABLE content_file_access_log_2025_11 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

CREATE TABLE content_file_access_log_2025_12 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

CREATE TABLE content_file_access_log_2026_01 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- 创建索引
CREATE INDEX idx_access_log_file_id ON content_file_access_log (file_id, create_time DESC);
CREATE INDEX idx_access_log_user_id ON content_file_access_log (user_id, create_time DESC) WHERE user_id IS NOT NULL;
CREATE INDEX idx_access_log_ip ON content_file_access_log (ip, create_time DESC);
CREATE INDEX idx_access_log_create_time ON content_file_access_log (create_time DESC);

COMMENT ON TABLE content_file_access_log IS '文件访问日志表(按月分区,保留6个月)';
COMMENT ON COLUMN content_file_access_log.status IS '状态:0=失败 1=成功';
COMMENT ON COLUMN content_file_access_log.cost_time IS '耗时(ms)';

-- =============================================
-- 3. 文件临时表(用于分片上传)
-- =============================================

DROP TABLE IF EXISTS content_file_temp CASCADE;

CREATE TABLE content_file_temp (
    id              BIGINT PRIMARY KEY,
    upload_id       VARCHAR(64) NOT NULL UNIQUE,  -- 上传任务ID(UUID)
    file_name       VARCHAR(256) NOT NULL,
    total_size      BIGINT NOT NULL,
    chunk_size      INTEGER NOT NULL,             -- 分片大小(字节)
    total_chunks    INTEGER NOT NULL,             -- 总分片数
    uploaded_chunks JSON NOT NULL DEFAULT '[]',   -- 已上传的分片索引数组
    file_path       VARCHAR(512) NULL,            -- 合并后的文件路径
    file_hash       VARCHAR(64) NULL,             -- 文件哈希(完成后计算)
    status          SMALLINT NOT NULL DEFAULT 0,  -- 0=上传中 1=已完成 2=已过期
    expired_time    TIMESTAMPTZ NOT NULL,         -- 过期时间(24小时后)

    creator         VARCHAR(64) DEFAULT '',
    create_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64) DEFAULT '',
    update_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         SMALLINT NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_temp_upload_id ON content_file_temp (upload_id);
CREATE INDEX idx_temp_status ON content_file_temp (status, expired_time);
CREATE INDEX idx_temp_expired ON content_file_temp (expired_time) WHERE status = 0 AND expired_time < NOW();

COMMENT ON TABLE content_file_temp IS '文件分片上传临时表';
COMMENT ON COLUMN content_file_temp.upload_id IS '上传任务ID(UUID)';
COMMENT ON COLUMN content_file_temp.status IS '状态:0=上传中 1=已完成 2=已过期';
COMMENT ON COLUMN content_file_temp.uploaded_chunks IS '已上传的分片索引数组[1,2,3,5,7]';

-- =============================================
-- 序列号创建
-- =============================================

CREATE SEQUENCE IF NOT EXISTS content_file_seq INCREMENT BY 1 MINVALUE 10000000 START WITH 10000000;
CREATE SEQUENCE IF NOT EXISTS content_file_access_log_seq INCREMENT BY 1 START WITH 10000;
CREATE SEQUENCE IF NOT EXISTS content_file_temp_seq INCREMENT BY 1 START WITH 1000;

-- =============================================
-- 视图: 热门文件统计(最近7天)
-- =============================================

DROP VIEW IF EXISTS content_hot_files;

CREATE VIEW content_hot_files AS
SELECT
    f.id,
    f.name,
    f.url,
    f.size,
    f.biz_type,
    f.author_id,
    f.post_id,
    COUNT(l.id) AS access_count,
    MAX(l.create_time) AS last_access_time
FROM content_file f
LEFT JOIN content_file_access_log l ON f.id = l.file_id
WHERE f.status = 0
  AND f.create_time >= NOW() - INTERVAL '7 days'
GROUP BY f.id
HAVING COUNT(l.id) >= 100
ORDER BY access_count DESC;

COMMENT ON VIEW content_hot_files IS '最近7天热门文件视图(访问≥100次)';

-- =============================================
-- 视图: 用户文件统计
-- =============================================

DROP VIEW IF EXISTS content_user_file_stats;

CREATE VIEW content_user_file_stats AS
SELECT
    author_id,
    biz_type,
    COUNT(*) AS file_count,
    SUM(size) AS total_size,
    AVG(size) AS avg_size,
    MAX(create_time) AS last_upload_time
FROM content_file
WHERE status = 0
GROUP BY author_id, biz_type;

COMMENT ON VIEW content_user_file_stats IS '用户文件统计视图(按业务类型分组)';

-- =============================================
-- 函数: 清理过期临时文件
-- =============================================

CREATE OR REPLACE FUNCTION cleanup_expired_temp_files()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- 删除已过期的临时文件记录
    UPDATE content_file_temp
    SET
        status = 2,
        updater = 'system',
        update_time = NOW()
    WHERE status = 0
      AND expired_time < NOW();

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_expired_temp_files() IS '清理已过期的分片上传临时文件(定时任务每小时执行)';

-- =============================================
-- 函数: 更新文件访问统计
-- =============================================

CREATE OR REPLACE FUNCTION update_file_access_stats(p_file_id BIGINT)
RETURNS VOID AS $$
BEGIN
    UPDATE content_file
    SET
        access_count = access_count + 1,
        last_access_time = NOW(),
        updater = 'system',
        update_time = NOW()
    WHERE id = p_file_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_file_access_stats(BIGINT) IS '更新文件访问次数和最后访问时间';

-- =============================================
-- 触发器: 记录访问日志时更新文件统计
-- =============================================

CREATE OR REPLACE FUNCTION trg_update_file_access()
RETURNS TRIGGER AS $$
BEGIN
    -- 仅在访问成功时更新统计
    IF NEW.status = 1 THEN
        PERFORM update_file_access_stats(NEW.file_id);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_access_log_insert ON content_file_access_log;

CREATE TRIGGER trg_access_log_insert
    AFTER INSERT ON content_file_access_log
    FOR EACH ROW
    EXECUTE FUNCTION trg_update_file_access();

COMMENT ON TRIGGER trg_access_log_insert ON content_file_access_log IS '插入访问日志后自动更新文件统计';

-- =============================================
-- 函数: 根据hash查找文件(秒传功能)
-- =============================================

CREATE OR REPLACE FUNCTION find_file_by_hash(p_hash VARCHAR(64))
RETURNS TABLE(
    file_id BIGINT,
    file_url VARCHAR(1024),
    file_size BIGINT,
    file_name VARCHAR(256)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        f.id,
        f.url,
        f.size,
        f.name
    FROM content_file f
    WHERE f.hash = p_hash
      AND f.status = 0
      AND f.deleted = 0
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION find_file_by_hash(VARCHAR) IS '根据文件hash查找已存在的文件(实现秒传)';

-- 使用示例:
-- SELECT * FROM find_file_by_hash('abc123def456...');

-- =============================================
-- 函数: 创建未来分区(定时任务调用)
-- =============================================

CREATE OR REPLACE FUNCTION create_future_partitions_content_file_log()
RETURNS INTEGER AS $$
DECLARE
    partition_count INTEGER := 0;
    future_month DATE;
    partition_name TEXT;
    start_date TEXT;
    end_date TEXT;
BEGIN
    -- 创建未来6个月的分区
    FOR i IN 1..6 LOOP
        future_month := DATE_TRUNC('month', NOW() + (i || ' month')::INTERVAL);
        partition_name := 'content_file_access_log_' || TO_CHAR(future_month, 'YYYY_MM');
        start_date := TO_CHAR(future_month, 'YYYY-MM-DD');
        end_date := TO_CHAR(future_month + INTERVAL '1 month', 'YYYY-MM-DD');

        -- 检查分区是否已存在
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables WHERE tablename = partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF content_file_access_log FOR VALUES FROM (%L) TO (%L)',
                partition_name, start_date, end_date
            );
            partition_count := partition_count + 1;
        END IF;
    END LOOP;

    RETURN partition_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_future_partitions_content_file_log() IS '自动创建未来6个月的分区(定时任务每月1号执行)';

-- =============================================
-- 完成
-- =============================================

COMMIT;

-- 显示所有表
\dt content_file*

-- 显示所有视图
\dv content_*

-- 显示所有序列
SELECT sequence_name FROM information_schema.sequences WHERE sequence_name LIKE 'content_file%';

-- 显示所有函数
\df cleanup_expired_temp_files
\df update_file_access_stats
\df find_file_by_hash
\df create_future_partitions_content_file_log

-- 验证content_file表结构
\d content_file

-- =============================================
-- 重要说明
-- =============================================

-- 1. config_id 关联到 infra_file_config.id
--    - 虽然跨库,但查询频率低(仅上传时)
--    - 可通过Feign API查询配置
--    - 避免重复定义存储配置

-- 2. author_id 关联到 system_users.id
--    - ⚠️ 重要修改: system_users和content_file在同一个数据库!
--    - 如果member模块使用member_profile方案,则user_id就是system_users.id
--    - 无需跨库查询,性能最优

-- 3. post_id 关联到 content_post.id
--    - 同库关联,性能最优
--    - 可使用外键约束保证数据一致性

-- 4. 文件秒传实现
--    - 上传前先计算文件hash
--    - 调用 find_file_by_hash() 检查是否已存在
--    - 存在则直接返回URL,无需重复上传

-- 5. 分片上传实现流程
--    - 1) 创建上传任务 -> content_file_temp
--    - 2) 客户端上传各分片 -> MinIO
--    - 3) 更新 uploaded_chunks 数组
--    - 4) 所有分片完成 -> 合并文件 -> 创建content_file记录
--    - 5) 定时任务清理过期的临时记录

-- 6. 定时任务配置(XXL-Job)
--    - cleanup_expired_temp_files: 每小时执行
--    - create_future_partitions_content_file_log: 每月1号凌晨执行
