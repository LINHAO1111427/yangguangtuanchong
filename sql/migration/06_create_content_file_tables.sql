-- =============================================
-- Content File Tables - xiaolvshu_content database
-- Execute this after connecting to xiaolvshu_content database
-- 内容文件存储独立化建表脚本
-- =============================================

-- Connect to xiaolvshu_content database
\c xiaolvshu_content

-- =============================================
-- 1. 文件元数据主表（核心表）
-- =============================================

DROP TABLE IF EXISTS content_file CASCADE;

CREATE TABLE content_file (
    id               BIGINT       PRIMARY KEY,
    config_id        BIGINT       NOT NULL,
    name             VARCHAR(256) NOT NULL,
    path             VARCHAR(512) NOT NULL,
    url              VARCHAR(1024) NOT NULL,
    type             VARCHAR(128) NULL,
    size             BIGINT       NOT NULL,
    hash             VARCHAR(64)  NULL,

    -- 业务字段
    biz_type         SMALLINT     NOT NULL DEFAULT 0,
    author_id        BIGINT       NOT NULL,
    post_id          BIGINT       NULL,
    status           SMALLINT     NOT NULL DEFAULT 0,
    storage_type     SMALLINT     NOT NULL DEFAULT 0,
    access_count     INTEGER      NOT NULL DEFAULT 0,
    last_access_time TIMESTAMPTZ  NULL,

    -- CDN相关
    cdn_url          VARCHAR(1024) NULL,
    cdn_status       SMALLINT     NOT NULL DEFAULT 0,

    -- 生命周期
    archive_time     TIMESTAMPTZ  NULL,
    delete_time      TIMESTAMPTZ  NULL,

    -- 芋道标准字段
    creator          VARCHAR(64)  DEFAULT '',
    create_time      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater          VARCHAR(64)  DEFAULT '',
    update_time      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted          SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引（关键！影响性能）
CREATE UNIQUE INDEX uk_content_file_hash ON content_file (hash) WHERE hash IS NOT NULL;
CREATE INDEX idx_content_file_author_time ON content_file (author_id, create_time DESC);
CREATE INDEX idx_content_file_post ON content_file (post_id) WHERE post_id IS NOT NULL;
CREATE INDEX idx_content_file_biz_type ON content_file (biz_type, create_time DESC);
CREATE INDEX idx_content_file_status ON content_file (status, create_time DESC);
CREATE INDEX idx_content_file_last_access ON content_file (last_access_time DESC) WHERE last_access_time IS NOT NULL;
CREATE INDEX idx_content_file_create_time ON content_file (create_time DESC);

-- 添加注释
COMMENT ON TABLE content_file IS '内容文件元数据表（独立存储）';
COMMENT ON COLUMN content_file.config_id IS '存储配置ID（关联infra_file_config）';
COMMENT ON COLUMN content_file.hash IS '文件哈希（用于秒传）';
COMMENT ON COLUMN content_file.biz_type IS '业务类型：0=视频 1=图片 2=封面 3=头像 4=其他';
COMMENT ON COLUMN content_file.status IS '状态：0=正常 1=删除中 2=已归档 3=已删除';
COMMENT ON COLUMN content_file.storage_type IS '存储类型：0=MinIO 1=阿里云OSS 2=腾讯云COS 3=AWS S3';
COMMENT ON COLUMN content_file.cdn_status IS 'CDN状态：0=未预热 1=已预热 2=预热失败';

-- =============================================
-- 2. 文件存储配置表
-- =============================================

DROP TABLE IF EXISTS content_file_config CASCADE;

CREATE TABLE content_file_config (
    id            BIGINT       PRIMARY KEY,
    name          VARCHAR(63)  NOT NULL,
    storage       SMALLINT     NOT NULL,
    base_path     VARCHAR(255) NOT NULL,
    domain        VARCHAR(255) NOT NULL,
    config        JSON         NOT NULL,
    remark        VARCHAR(255) NULL,
    master        SMALLINT     NOT NULL DEFAULT 0,

    creator       VARCHAR(64)  DEFAULT '',
    create_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater       VARCHAR(64)  DEFAULT '',
    update_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted       SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_file_config_name ON content_file_config (name);
CREATE INDEX idx_file_config_master ON content_file_config (master);

-- 添加注释
COMMENT ON TABLE content_file_config IS '文件存储配置表';
COMMENT ON COLUMN content_file_config.storage IS '存储类型：0=MinIO 1=阿里云OSS 2=腾讯云COS';
COMMENT ON COLUMN content_file_config.config IS '配置JSON（accessKey/secretKey/bucket等）';
COMMENT ON COLUMN content_file_config.master IS '是否主配置：0=否 1=是';

-- 初始化MinIO配置
INSERT INTO content_file_config (id, name, storage, base_path, domain, config, master, remark)
VALUES (
    1,
    'MinIO-Primary',
    0,
    'content',
    'https://minio.xiaolvshu.com',
    '{"endpoint": "http://127.0.0.1:9000", "accessKey": "minioadmin", "secretKey": "minioadmin", "bucket": "xiaolvshu"}',
    1,
    '主存储配置（MinIO）'
);

-- =============================================
-- 3. 文件访问日志表（分区表）
-- =============================================

DROP TABLE IF EXISTS content_file_access_log CASCADE;

CREATE TABLE content_file_access_log (
    id          BIGINT       PRIMARY KEY,
    file_id     BIGINT       NOT NULL,
    user_id     BIGINT       NULL,
    ip          VARCHAR(50)  NULL,
    user_agent  VARCHAR(255) NULL,
    referer     VARCHAR(255) NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    error_msg   VARCHAR(200) NULL,
    cost_time   INTEGER      NULL,

    creator     VARCHAR(64)  DEFAULT '',
    create_time TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

-- 创建分区（每月一个分区）
-- 注意：PostgreSQL 10+ 支持自动分区，这里创建初始分区
CREATE TABLE content_file_access_log_2025_11 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

CREATE TABLE content_file_access_log_2025_12 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

-- 创建索引
CREATE INDEX idx_access_log_file_id ON content_file_access_log (file_id, create_time DESC);
CREATE INDEX idx_access_log_user_id ON content_file_access_log (user_id, create_time DESC);
CREATE INDEX idx_access_log_ip ON content_file_access_log (ip, create_time DESC);
CREATE INDEX idx_access_log_create_time ON content_file_access_log (create_time DESC);

-- 添加注释
COMMENT ON TABLE content_file_access_log IS '文件访问日志表（按月分区）';
COMMENT ON COLUMN content_file_access_log.status IS '状态：0=失败 1=成功';
COMMENT ON COLUMN content_file_access_log.cost_time IS '耗时(ms)';

-- =============================================
-- 4. 文件临时表（用于分片上传）
-- =============================================

DROP TABLE IF EXISTS content_file_temp CASCADE;

CREATE TABLE content_file_temp (
    id              BIGINT       PRIMARY KEY,
    upload_id       VARCHAR(64)  NOT NULL,
    file_name       VARCHAR(256) NOT NULL,
    total_size      BIGINT       NOT NULL,
    chunk_size      INTEGER      NOT NULL,
    total_chunks    INTEGER      NOT NULL,
    uploaded_chunks JSON         NOT NULL DEFAULT '[]',
    file_path       VARCHAR(512) NULL,
    status          SMALLINT     NOT NULL DEFAULT 0,
    expired_time    TIMESTAMPTZ  NOT NULL,

    creator         VARCHAR(64)  DEFAULT '',
    create_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updater         VARCHAR(64)  DEFAULT '',
    update_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted         SMALLINT     NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_temp_upload_id ON content_file_temp (upload_id);
CREATE INDEX idx_temp_status ON content_file_temp (status, expired_time);
CREATE INDEX idx_temp_expired ON content_file_temp (expired_time) WHERE expired_time < NOW();

-- 添加注释
COMMENT ON TABLE content_file_temp IS '文件分片上传临时表';
COMMENT ON COLUMN content_file_temp.upload_id IS '上传任务ID';
COMMENT ON COLUMN content_file_temp.status IS '状态：0=上传中 1=已完成 2=已过期';
COMMENT ON COLUMN content_file_temp.uploaded_chunks IS '已上传的分片索引数组';

-- =============================================
-- 序列号创建
-- =============================================

CREATE SEQUENCE IF NOT EXISTS content_file_seq INCREMENT BY 1 MINVALUE 10000000 START WITH 10000000;
CREATE SEQUENCE IF NOT EXISTS content_file_config_seq INCREMENT BY 1 START WITH 100;
CREATE SEQUENCE IF NOT EXISTS content_file_access_log_seq INCREMENT BY 1 START WITH 10000;
CREATE SEQUENCE IF NOT EXISTS content_file_temp_seq INCREMENT BY 1 START WITH 1000;

-- =============================================
-- 视图创建：热门文件统计
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

COMMENT ON VIEW content_hot_files IS '最近7天热门文件视图（访问≥100次）';

-- =============================================
-- 视图创建：用户文件统计
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

COMMENT ON VIEW content_user_file_stats IS '用户文件统计视图';

-- =============================================
-- 函数创建：清理过期临时文件
-- =============================================

CREATE OR REPLACE FUNCTION cleanup_expired_temp_files()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    -- 删除已过期的临时文件记录
    DELETE FROM content_file_temp
    WHERE status != 1 AND expired_time < NOW();

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_expired_temp_files() IS '清理已过期的分片上传临时文件';

-- =============================================
-- 函数创建：更新文件访问统计
-- =============================================

CREATE OR REPLACE FUNCTION update_file_access_stats(
    p_file_id BIGINT
) RETURNS VOID AS $$
BEGIN
    UPDATE content_file
    SET
        access_count = access_count + 1,
        last_access_time = NOW()
    WHERE id = p_file_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_file_access_stats(BIGINT) IS '更新文件访问次数和最后访问时间';

-- =============================================
-- 触发器：记录访问日志时更新文件统计
-- =============================================

CREATE OR REPLACE FUNCTION trg_update_file_access()
RETURNS TRIGGER AS $$
BEGIN
    -- 插入日志后更新文件的访问统计
    PERFORM update_file_access_stats(NEW.file_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_access_log_insert ON content_file_access_log;

CREATE TRIGGER trg_access_log_insert
    AFTER INSERT ON content_file_access_log
    FOR EACH ROW
    EXECUTE FUNCTION trg_update_file_access();

COMMENT ON TRIGGER trg_access_log_insert ON content_file_access_log IS '插入访问日志后更新文件统计信息';

-- =============================================
-- 完成
-- =============================================

COMMIT;

-- 显示所有表
\dt

-- 显示所有视图
\dv

-- 显示所有序列
SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema='public';
