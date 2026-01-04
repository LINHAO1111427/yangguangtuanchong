# Database Migration Guide - xiaolvshu Project

## Overview

This migration guide implements the 3-database architecture:
1. **ruoyi-vue-pro** - Yudao core modules (system, member, infra, pay, mall, etc.)
2. **xiaolvshu_content** - Content module (posts, comments, likes, favorites)
3. **xiaolvshu_message** - Message module (threads, messages, participants)

## Prerequisites

- PostgreSQL 12+ installed
- psql command-line tool available
- Backup your existing `xiaolvshu` database before starting

## Migration Steps

### Step 1: Backup Existing Data

```bash
# Backup current xiaolvshu database
pg_dump -h 127.0.0.1 -p 55432 -U postgres xiaolvshu > backup_xiaolvshu_$(date +%Y%m%d).sql
```

### Step 2: Rename Database and Create Message DB

```bash
# Connect to PostgreSQL as postgres user
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres

# Then execute:
\i C:/WorkSpace/xiaolvshu/yudao-cloud-jdk17/sql/migration/01_rename_database.sql
```

**What this does:**
- Renames `xiaolvshu` → `xiaolvshu_content`
- Creates new `xiaolvshu_message` database

### Step 3: Create Content Module Tables

```bash
# Connect to xiaolvshu_content database
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content

# Execute:
\i C:/WorkSpace/xiaolvshu/yudao-cloud-jdk17/sql/migration/02_create_content_tables.sql
```

**What this does:**
- Creates 11 content tables with proper structure
- Enables monthly partitioning for `content_post` (2025-01 to 2025-12)
- Enables daily partitioning for `content_comment` (current month)
- Enables hash partitioning for `content_like` (16 partitions)
- Creates all necessary indexes
- Inserts default topics and ad slots

**Tables created:**
1. `content_post` - Main content table (monthly partitioned)
2. `content_media` - Media files
3. `content_topic` - Topics
4. `content_tag` - Tags
5. `content_post_tag_relation` - Post-tag relations
6. `content_comment` - Comments (daily partitioned)
7. `content_like` - Likes (hash partitioned)
8. `content_favorite` - Favorites
9. `content_favorite_folder` - Favorite folders
10. `content_interaction` - Interaction records
11. `content_ad_slot` - Ad slot configuration

### Step 4: Create Materialized View and Functions

```bash
# Still in xiaolvshu_content database
\i C:/WorkSpace/xiaolvshu/yudao-cloud-jdk17/sql/migration/03_create_materialized_view_and_functions.sql
```

**What this does:**
- Creates materialized view `mv_content_hot_rank` for hot content ranking
- Creates function `update_content_stats()` for updating content statistics
- Creates function `create_monthly_partitions()` for auto-creating monthly partitions
- Creates function `create_daily_partitions()` for auto-creating daily partitions
- Auto-creates future partitions (6 months for content_post, 30 days for content_comment)

### Step 5: Create Message Module Tables

```bash
# Connect to xiaolvshu_message database
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_message

# Execute:
\i C:/WorkSpace/xiaolvshu/yudao-cloud-jdk17/sql/migration/04_create_message_tables.sql
```

**What this does:**
- Creates 4 message tables
- Enables monthly partitioning for `message_detail` (2025-01 to 2025-12)
- Creates auto-partition function
- Auto-creates future partitions (6 months ahead)

**Tables created:**
1. `message_thread` - Message threads (conversations)
2. `message_participant` - Thread participants
3. `message_detail` - Message details (monthly partitioned)
4. `message_unread` - Unread message counters

### Step 6: Verify Migration

```bash
# Check databases
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres -c "\l" | grep xiaolvshu

# Expected output:
# xiaolvshu_content
# xiaolvshu_message

# Check content tables
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content -c "\dt"

# Check content partitions
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content -c "
SELECT tablename, pg_size_pretty(pg_total_relation_size('public.'||tablename))
FROM pg_tables
WHERE tablename LIKE 'content_post_%' OR tablename LIKE 'content_comment_%'
ORDER BY tablename;
"

# Check message tables
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_message -c "\dt"

# Check materialized view
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content -c "SELECT COUNT(*) FROM mv_content_hot_rank;"
```

### Step 7: Application Configuration Updated

✅ **Already completed:**

1. **Content module** (`yudao-module-content/application-local.yml`):
   - Changed database URL to `xiaolvshu_content`
   - Configured master/slave datasources

2. **Message module** (`yudao-module-message/application-local.yml`):
   - Changed database URL to `xiaolvshu_message`
   - Configured master/slave datasources

## Features Enabled

### ✅ PostgreSQL Native Partitioning

**content_post** - Monthly range partitioning
- Automatic partition pruning for date range queries
- Easy historical data archival
- Better query performance on large datasets

**content_comment** - Daily range partitioning
- Optimized for recent comment queries
- Automatic partition management via function

**content_like** - Hash partitioning (16 partitions)
- Evenly distributed by user_id
- Better concurrent write performance

### ✅ Materialized View

**mv_content_hot_rank**
- Pre-calculated hot content ranking (last 7 days, top 1000)
- Hot score formula: `like*2 + comment*3 + share*5 + collect*4 - time_decay`
- Indexed for fast queries
- **Refresh strategy:**
  ```sql
  -- Manual refresh
  REFRESH MATERIALIZED VIEW mv_content_hot_rank;

  -- Or non-blocking refresh
  REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;
  ```

**Recommended:** Set up a cron job to refresh every hour

### ✅ Auto-Partition Functions

**create_monthly_partitions(table_name, months_ahead)**
- Automatically creates future monthly partitions
- Usage: `SELECT create_monthly_partitions('content_post', 6);`

**create_daily_partitions(table_name, days_ahead)**
- Automatically creates future daily partitions
- Usage: `SELECT create_daily_partitions('content_comment', 30);`

**Recommended:** Set up a monthly cron job to maintain partitions

### ✅ Statistics Update Function

**update_content_stats(post_id, stat_type, increment)**
- Atomic counter updates
- Usage:
  ```sql
  SELECT update_content_stats(1, 'like', 1);      -- +1 like
  SELECT update_content_stats(1, 'comment', 1);   -- +1 comment
  SELECT update_content_stats(1, 'view', 10);     -- +10 views
  ```

## Maintenance Tasks

### Daily

1. **Monitor partition usage:**
   ```sql
   SELECT tablename, pg_size_pretty(pg_total_relation_size('public.'||tablename))
   FROM pg_tables
   WHERE tablename LIKE 'content_post_%' OR tablename LIKE 'content_comment_%'
   ORDER BY tablename DESC LIMIT 10;
   ```

### Hourly (Recommended)

2. **Refresh materialized view:**
   ```bash
   psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content -c "REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;"
   ```

### Monthly

3. **Create future partitions:**
   ```sql
   SELECT create_monthly_partitions('content_post', 6);
   SELECT create_monthly_partitions('message_detail', 6);
   SELECT create_daily_partitions('content_comment', 30);
   ```

4. **Archive old data:**
   ```sql
   -- Example: Drop partitions older than 2 years
   DROP TABLE IF EXISTS content_post_2023_01;
   DROP TABLE IF EXISTS content_post_2023_02;
   ```

### Backup Strategy

```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d)
pg_dump -h 127.0.0.1 -p 55432 -U postgres xiaolvshu_content > backup_content_$DATE.sql
pg_dump -h 127.0.0.1 -p 55432 -U postgres xiaolvshu_message > backup_message_$DATE.sql
pg_dump -h 127.0.0.1 -p 55432 -U postgres ruoyi-vue-pro > backup_core_$DATE.sql

# Compress
gzip backup_*.sql

# Keep last 7 days
find . -name "backup_*.sql.gz" -mtime +7 -delete
```

## Troubleshooting

### Issue: "database is being accessed by other users"

**Solution:**
```sql
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'xiaolvshu' AND pid <> pg_backend_pid();

-- Then retry rename
ALTER DATABASE xiaolvshu RENAME TO xiaolvshu_content;
```

### Issue: Partition does not exist for new data

**Solution:**
```sql
-- Create missing partition manually
-- For content_post
CREATE TABLE content_post_2026_01 PARTITION OF content_post
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- Or use auto-function
SELECT create_monthly_partitions('content_post', 12);
```

### Issue: Materialized view is stale

**Solution:**
```sql
-- Force refresh
REFRESH MATERIALIZED VIEW mv_content_hot_rank;

-- Or setup hourly cron:
# crontab -e
0 * * * * psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu_content -c "REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;" > /dev/null 2>&1
```

## Performance Tips

1. **Use partition pruning:**
   ```sql
   -- Good: Partition pruning enabled
   SELECT * FROM content_post WHERE created_at >= '2025-10-01' AND created_at < '2025-11-01';

   -- Bad: Full table scan
   SELECT * FROM content_post WHERE author_id = 123;
   ```

2. **Query materialized view instead of main table for hot content:**
   ```sql
   -- Use this for hot content ranking
   SELECT * FROM mv_content_hot_rank ORDER BY rank_num LIMIT 20;
   ```

3. **Monitor slow queries:**
   ```sql
   SELECT query, mean_exec_time, calls
   FROM pg_stat_statements
   ORDER BY mean_exec_time DESC
   LIMIT 10;
   ```

## Rollback Plan

If you need to rollback:

```bash
# 1. Stop application
# 2. Rename database back
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres -c "ALTER DATABASE xiaolvshu_content RENAME TO xiaolvshu;"

# 3. Restore from backup
psql -h 127.0.0.1 -p 55432 -U postgres -d xiaolvshu < backup_xiaolvshu_YYYYMMDD.sql

# 4. Revert application config changes
```

## Summary

✅ **Completed:**
- Database renamed to `xiaolvshu_content`
- Created `xiaolvshu_message` database
- Enabled monthly partitioning for `content_post`
- Enabled daily partitioning for `content_comment`
- Created materialized view `mv_content_hot_rank`
- Deployed auto-partition functions
- Updated application configurations

✅ **Performance benefits:**
- Faster queries with partition pruning
- Pre-calculated hot content ranking
- Automatic partition management
- Scalable architecture for future growth

✅ **Next steps:**
- Test application connectivity
- Set up cron jobs for materialized view refresh
- Monitor partition growth
- Consider read-write splitting when traffic grows
