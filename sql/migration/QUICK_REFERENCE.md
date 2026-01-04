# 快速参考 - 分区函数与定时任务

## 🚀 一键启动定时任务

### Step 1: 启用 XXL-Job

```yaml
# yudao-module-content/application-local.yml
xxl:
  job:
    enabled: true
    admin:
      addresses: http://127.0.0.1:9090/xxl-job-admin

# yudao-module-message/application-local.yml
xxl:
  job:
    enabled: true
    admin:
      addresses: http://127.0.0.1:9090/xxl-job-admin
```

### Step 2: 登录 XXL-Job

访问: `http://127.0.0.1:9090/xxl-job-admin`
账号: `admin` / 密码: `123456`

### Step 3: 添加3个任务

| JobHandler | Cron | 说明 |
|------------|------|------|
| contentPartitionMaintenanceJob | `0 0 2 1 * ?` | 内容分区维护（每月） |
| contentHotRankRefreshJob | `0 5 * * * ?` | 热榜刷新（每小时） |
| messagePartitionMaintenanceJob | `0 0 2 1 * ?` | 消息分区维护（每月） |

详细配置: `XXL_JOB_SETUP_GUIDE.md`

---

## 📊 数据库函数速查

### 分区管理函数

```sql
-- 创建未来月分区（默认6个月）
SELECT create_monthly_partitions('content_post', 6);
SELECT create_monthly_partitions('message_detail', 6);

-- 创建未来日分区（默认30天）
SELECT create_daily_partitions('content_comment', 30);

-- 查看已创建的分区
SELECT tablename FROM pg_tables
WHERE tablename LIKE 'content_post_%'
ORDER BY tablename;
```

### 热榜刷新

```sql
-- 刷新热榜（非阻塞模式）
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;

-- 查看热榜
SELECT * FROM mv_content_hot_rank ORDER BY rank_num LIMIT 20;

-- 统计热榜内容数量
SELECT COUNT(*) FROM mv_content_hot_rank;
```

### 内容统计更新

```sql
-- 更新点赞数 +1
SELECT update_content_stats(1, 'like', 1);

-- 更新评论数 +1
SELECT update_content_stats(1, 'comment', 1);

-- 更新浏览数 +10
SELECT update_content_stats(1, 'view', 10);

-- 更新分享数 +1
SELECT update_content_stats(1, 'share', 1);

-- 更新收藏数 +1
SELECT update_content_stats(1, 'collect', 1);
```

---

## 🔍 常用检查命令

### 检查数据库

```bash
# 查看所有数据库
docker exec postgres-xiaolvshu psql -U postgres -c "\l" | grep xiaolvshu

# 连接到 content 数据库
docker exec -it postgres-xiaolvshu psql -U postgres -d xiaolvshu_content

# 连接到 message 数据库
docker exec -it postgres-xiaolvshu psql -U postgres -d xiaolvshu_message
```

### 检查表和分区

```sql
-- Content 模块表统计
SELECT
    COUNT(*) FILTER (WHERE tablename NOT LIKE '%_%_20%') as main_tables,
    COUNT(*) FILTER (WHERE tablename LIKE '%_%_20%') as partitions
FROM pg_tables
WHERE schemaname = 'public' AND tablename LIKE 'content_%';

-- 查看分区大小
SELECT tablename, pg_size_pretty(pg_total_relation_size('public.'||tablename)) as size
FROM pg_tables
WHERE tablename LIKE 'content_post_%'
ORDER BY tablename DESC LIMIT 10;
```

### 检查函数和视图

```sql
-- 查看所有函数
\df

-- 查看物化视图
\dm

-- 查看函数定义
\df+ create_monthly_partitions
```

---

## ⚡ 性能测试

### 热榜查询对比

```sql
-- 方式1：实时计算（慢）
EXPLAIN ANALYZE
SELECT id, title,
       (like_count*2 + comment_count*3 + share_count*5 + collect_count*4) as hot_score
FROM content_post
WHERE audit_status = 'approved'
  AND publish_time >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY hot_score DESC LIMIT 20;
-- 耗时: 500-1000ms

-- 方式2：物化视图（快）
EXPLAIN ANALYZE
SELECT * FROM mv_content_hot_rank ORDER BY rank_num LIMIT 20;
-- 耗时: 10-20ms (50倍提升!)
```

### 分区裁剪效果

```sql
-- 查询10月的数据（只扫描1个分区）
EXPLAIN ANALYZE
SELECT * FROM content_post
WHERE created_at >= '2025-10-01' AND created_at < '2025-11-01';

-- 查看执行计划中的 Partitions scanned
```

---

## 🛠️ 故障排查

### 问题1: 分区不存在

**错误**: `ERROR: no partition of relation "content_post" found for row`

**解决**:
```sql
-- 检查缺失哪个月
SELECT '2025-' || LPAD(month::TEXT, 2, '0') as missing_month
FROM generate_series(1, 12) month
WHERE NOT EXISTS (
    SELECT 1 FROM pg_tables
    WHERE tablename = 'content_post_2025_' || LPAD(month::TEXT, 2, '0')
);

-- 手动创建缺失分区
SELECT create_monthly_partitions('content_post', 12);
```

### 问题2: 热榜数据为空

**检查**:
```sql
-- 检查 content_post 是否有数据
SELECT COUNT(*) FROM content_post
WHERE audit_status = 'approved'
  AND publish_time >= CURRENT_DATE - INTERVAL '7 days';

-- 如果有数据但热榜为空，手动刷新
REFRESH MATERIALIZED VIEW mv_content_hot_rank;
```

### 问题3: XXL-Job 任务未执行

**检查清单**:
- [ ] XXL-Job Admin 是否运行: `http://127.0.0.1:9090/xxl-job-admin`
- [ ] `xxl.job.enabled` 是否为 `true`
- [ ] 执行器是否注册成功
- [ ] Cron 表达式是否正确
- [ ] 应用日志是否有错误

---

## 📅 维护日历

### 每小时
- ✅ 热榜自动刷新（XXL-Job）

### 每月1号
- ✅ Content 分区自动创建（XXL-Job）
- ✅ Message 分区自动创建（XXL-Job）

### 每季度（手动）
- 🔍 检查分区数量是否正常增长
- 🔍 清理超过2年的旧分区（可选）
- 🔍 分析慢查询日志

### 每年（手动）
- 🔍 评估是否需要调整分区策略
- 🔍 评估热榜刷新频率
- 🔍 数据库性能调优

---

## 📚 相关文档

| 文档 | 说明 |
|------|------|
| `MIGRATION_SUMMARY.md` | 数据库迁移总结 |
| `XXL_JOB_SETUP_GUIDE.md` | XXL-Job 详细配置指南 |
| `SCHEDULED_JOBS_SUMMARY.md` | 定时任务总结 |
| `README.md` | 迁移步骤详解 |

---

## 🎯 核心指标

| 指标 | 目标值 | 当前状态 |
|------|--------|----------|
| Content 主表数量 | 11 | ✅ |
| Content 分区数量 | 70+ | ✅ |
| Message 主表数量 | 4 | ✅ |
| Message 分区数量 | 16+ | ✅ |
| 热榜查询耗时 | < 50ms | ✅ 10-20ms |
| 分区自动创建 | 每月执行 | ✅ XXL-Job |

---

**快速求助**:
- 数据库相关问题 → 查看 `MIGRATION_SUMMARY.md`
- XXL-Job 配置 → 查看 `XXL_JOB_SETUP_GUIDE.md`
- 定时任务代码 → 查看 `SCHEDULED_JOBS_SUMMARY.md`
