-- =============================================
-- Materialized View and Auto Partition Functions
-- Execute this after content tables are created
-- =============================================

-- Connect to xiaolvshu_content
\c xiaolvshu_content

-- =============================================
-- 1. Materialized View: Hot Content Ranking
-- =============================================

DROP MATERIALIZED VIEW IF EXISTS mv_content_hot_rank CASCADE;

CREATE MATERIALIZED VIEW mv_content_hot_rank AS
SELECT
    p.id as post_id,
    p.author_id,
    p.title,
    p.cover_image,
    p.type,
    -- Hot score formula: like*2 + comment*3 + share*5 + collect*4 - time decay
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

-- Create indexes for materialized view
CREATE UNIQUE INDEX idx_mv_content_hot_rank_post ON mv_content_hot_rank (post_id);
CREATE INDEX idx_mv_content_hot_rank_score ON mv_content_hot_rank (hot_score DESC);
CREATE INDEX idx_mv_content_hot_rank_type ON mv_content_hot_rank (type, rank_num);

COMMENT ON MATERIALIZED VIEW mv_content_hot_rank IS 'Hot content ranking (last 7 days, top 1000)';

-- =============================================
-- 2. Function: Update Content Statistics
-- =============================================

CREATE OR REPLACE FUNCTION update_content_stats(
    p_post_id BIGINT,
    p_stat_type VARCHAR,
    p_increment INTEGER DEFAULT 1
)
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

COMMENT ON FUNCTION update_content_stats IS 'Update content statistics counters';

-- Usage example:
-- SELECT update_content_stats(1, 'like', 1);      -- Increment like count by 1
-- SELECT update_content_stats(1, 'comment', 1);   -- Increment comment count by 1
-- SELECT update_content_stats(1, 'view', 10);     -- Increment view count by 10

-- =============================================
-- 3. Function: Auto Create Monthly Partitions
-- =============================================

CREATE OR REPLACE FUNCTION create_monthly_partitions(
    table_name TEXT,
    months_ahead INTEGER DEFAULT 6
)
RETURNS VOID AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    partition_name TEXT;
    i INTEGER;
BEGIN
    -- Start from next month
    start_date := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');

    FOR i IN 1..months_ahead LOOP
        end_date := start_date + INTERVAL '1 month';
        partition_name := table_name || '_' || TO_CHAR(start_date, 'YYYY_MM');

        -- Check if partition already exists
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables WHERE tablename = partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                partition_name, table_name, start_date, end_date
            );
            RAISE NOTICE 'Created partition: %', partition_name;
        ELSE
            RAISE NOTICE 'Partition already exists: %', partition_name;
        END IF;

        start_date := end_date;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_monthly_partitions IS 'Auto create monthly partitions for specified table';

-- Usage example:
-- SELECT create_monthly_partitions('content_post', 6);  -- Create 6 months ahead

-- =============================================
-- 4. Function: Auto Create Daily Partitions
-- =============================================

CREATE OR REPLACE FUNCTION create_daily_partitions(
    table_name TEXT,
    days_ahead INTEGER DEFAULT 30
)
RETURNS VOID AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    partition_name TEXT;
    i INTEGER;
BEGIN
    -- Start from tomorrow
    start_date := CURRENT_DATE + INTERVAL '1 day';

    FOR i IN 1..days_ahead LOOP
        end_date := start_date + INTERVAL '1 day';
        partition_name := table_name || '_' || TO_CHAR(start_date, 'YYYY_MM_DD');

        -- Check if partition already exists
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables WHERE tablename = partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                partition_name, table_name, start_date, end_date
            );
            RAISE NOTICE 'Created partition: %', partition_name;
        ELSE
            RAISE NOTICE 'Partition already exists: %', partition_name;
        END IF;

        start_date := end_date;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_daily_partitions IS 'Auto create daily partitions for specified table';

-- Usage example:
-- SELECT create_daily_partitions('content_comment', 30);  -- Create 30 days ahead

-- =============================================
-- 5. Execute: Create Future Partitions
-- =============================================

-- Create partitions for content_post (next 6 months)
SELECT create_monthly_partitions('content_post', 6);

-- Create partitions for content_comment (next 30 days)
SELECT create_daily_partitions('content_comment', 30);

-- =============================================
-- 6. Schedule: Refresh Materialized View
-- =============================================

-- NOTE: You need to set up a cron job or scheduled task to refresh the materialized view
-- Recommended: Refresh every hour

-- Manual refresh:
-- REFRESH MATERIALIZED VIEW mv_content_hot_rank;

-- Or refresh concurrently (non-blocking):
-- REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;

-- =============================================
-- 7. Verify: Check Partitions
-- =============================================

-- Check content_post partitions
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE tablename LIKE 'content_post_%'
ORDER BY tablename;

-- Check content_comment partitions
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE tablename LIKE 'content_comment_%'
ORDER BY tablename;

-- Check materialized view
SELECT COUNT(*) as total_hot_posts FROM mv_content_hot_rank;

COMMIT;
