-- =============================================
-- Step 1: Rename database and create message db
-- Execute these SQL commands manually in PostgreSQL
-- =============================================

-- 1. Rename xiaolvshu to xiaolvshu_content
-- NOTE: Cannot rename while connected. You need to:
-- 1) Connect to 'postgres' database first
-- 2) Terminate all connections to xiaolvshu
-- 3) Then rename

-- Terminate connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'xiaolvshu' AND pid <> pg_backend_pid();

-- Rename database
ALTER DATABASE xiaolvshu RENAME TO xiaolvshu_content;

-- 2. Create xiaolvshu_message database
CREATE DATABASE xiaolvshu_message
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE xiaolvshu_message IS 'Message module database for xiaolvshu project';

-- Verify
\l xiaolvshu_content
\l xiaolvshu_message
