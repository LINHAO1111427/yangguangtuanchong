-- =============================================
-- Message Module Tables - xiaolvshu_message database
-- Execute this after connecting to xiaolvshu_message database
-- =============================================

-- Connect to xiaolvshu_message
\c xiaolvshu_message

-- =============================================
-- 1. Message Thread Table (Conversation)
-- =============================================

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

COMMENT ON TABLE message_thread IS 'Message thread (conversation) table';
COMMENT ON COLUMN message_thread.type IS 'Type: 0=system 1=private 2=group';

-- =============================================
-- 2. Message Participant Table
-- =============================================

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

COMMENT ON TABLE message_participant IS 'Message thread participant table';
COMMENT ON COLUMN message_participant.role IS 'Role: 0=member 1=admin 2=owner';

-- =============================================
-- 3. Message Detail Table (Monthly partition)
-- =============================================

DROP TABLE IF EXISTS message_detail CASCADE;

CREATE TABLE message_detail (
    id           BIGINT      NOT NULL,
    thread_id    BIGINT      NOT NULL,
    sender_id    BIGINT      NOT NULL,
    content_type SMALLINT    NOT NULL,
    content      JSONB       NOT NULL,
    ext          JSONB       NULL,
    quote_id     BIGINT      NULL,
    send_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status       SMALLINT    NOT NULL DEFAULT 1,
    trace_id     VARCHAR(64) NULL,
    PRIMARY KEY (id, send_time)
) PARTITION BY RANGE (send_time);

COMMENT ON TABLE message_detail IS 'Message detail table (partitioned by month)';
COMMENT ON COLUMN message_detail.content_type IS 'Content type: 1=text 2=image 3=video 4=audio 5=share 6=system';

-- Create partitions for 2025
CREATE TABLE message_detail_2025_01 PARTITION OF message_detail
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE message_detail_2025_02 PARTITION OF message_detail
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE message_detail_2025_03 PARTITION OF message_detail
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE message_detail_2025_04 PARTITION OF message_detail
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE message_detail_2025_05 PARTITION OF message_detail
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE message_detail_2025_06 PARTITION OF message_detail
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE message_detail_2025_07 PARTITION OF message_detail
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
CREATE TABLE message_detail_2025_08 PARTITION OF message_detail
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');
CREATE TABLE message_detail_2025_09 PARTITION OF message_detail
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');
CREATE TABLE message_detail_2025_10 PARTITION OF message_detail
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
CREATE TABLE message_detail_2025_11 PARTITION OF message_detail
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
CREATE TABLE message_detail_2025_12 PARTITION OF message_detail
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

CREATE INDEX idx_message_detail_thread_time ON message_detail (thread_id, send_time DESC);
CREATE INDEX idx_message_detail_sender ON message_detail (sender_id, send_time DESC);

-- =============================================
-- 4. Message Unread Table
-- =============================================

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

COMMENT ON TABLE message_unread IS 'Message unread counter table';

-- =============================================
-- 5. Function: Auto Create Monthly Partitions for Message
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

COMMENT ON FUNCTION create_monthly_partitions IS 'Auto create monthly partitions for message tables';

-- Create future partitions
SELECT create_monthly_partitions('message_detail', 6);

-- =============================================
-- 6. Verify: Check Partitions
-- =============================================

-- Check message_detail partitions
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE tablename LIKE 'message_detail_%'
ORDER BY tablename;

COMMIT;
