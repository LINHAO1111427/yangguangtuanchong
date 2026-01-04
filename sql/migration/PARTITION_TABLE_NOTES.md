# PostgreSQL 分区表主键规则说明

## 🚨 遇到的错误

```
ERROR: unique constraint on partitioned table must include all partitioning columns
Detail: PRIMARY KEY constraint on table "xxx" lacks column "create_time" which is part of the partition key.
```

---

## 📖 PostgreSQL 分区表规则

### 核心规则

**分区表的主键/唯一约束必须包含所有分区键列。**

### 为什么？

PostgreSQL 的唯一性约束是在**分区级别**检查的，而不是跨分区全局检查。如果主键不包含分区键：

1. **无法保证全局唯一性** - 不同分区可能有相同的ID
2. **查询性能下降** - 无法利用分区裁剪优化
3. **索引维护困难** - 无法在分区边界正确维护唯一索引

---

## ✅ 解决方案

### 错误写法 ❌

```sql
CREATE TABLE content_file_access_log (
    id          BIGINT PRIMARY KEY,  -- ❌ 错误！主键不包含分区键
    file_id     BIGINT NOT NULL,
    create_time TIMESTAMPTZ NOT NULL
) PARTITION BY RANGE (create_time);  -- 按 create_time 分区
```

**问题：**
- 主键只有 `id`
- 分区键是 `create_time`
- 主键未包含分区键 → 报错！

---

### 正确写法 ✅

```sql
CREATE TABLE content_file_access_log (
    id          BIGINT NOT NULL,
    file_id     BIGINT NOT NULL,
    create_time TIMESTAMPTZ NOT NULL,

    -- ✅ 正确：主键包含 id 和 create_time（分区键）
    PRIMARY KEY (id, create_time)
) PARTITION BY RANGE (create_time);
```

**修复要点：**
1. `id` 改为 `NOT NULL`（不再是单独的主键）
2. 添加复合主键 `PRIMARY KEY (id, create_time)`
3. 复合主键包含了分区键 `create_time`

---

## 📊 本项目中的分区表

### 1. content_file_access_log (文件访问日志)

```sql
-- 位置：09_optimized_content_file_architecture.sql

CREATE TABLE content_file_access_log (
    id          BIGINT NOT NULL,
    file_id     BIGINT NOT NULL,
    create_time TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (id, create_time)  -- ✅ 复合主键
) PARTITION BY RANGE (create_time);

-- 按月分区
CREATE TABLE content_file_access_log_2025_11 PARTITION OF content_file_access_log
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
```

**业务影响：**
- ✅ 查询时需要同时提供 `id` 和 `create_time`
- ✅ 插入时自动根据 `create_time` 路由到对应分区
- ✅ 利用分区裁剪，查询性能提升

---

### 2. member_visitor (访客记录)

```sql
-- 位置：08_optimized_member_architecture.sql

CREATE TABLE member_visitor (
    id              BIGINT NOT NULL,
    viewed_user_id  BIGINT NOT NULL,
    viewer_id       BIGINT NOT NULL,
    create_time     TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (id, create_time)  -- ✅ 复合主键
) PARTITION BY RANGE (create_time);

-- 按月分区（保留12个月）
CREATE TABLE member_visitor_2025_11 PARTITION OF member_visitor
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
```

**业务影响：**
- ✅ 访客记录按月分区存储
- ✅ 自动清理12个月前的旧分区
- ✅ 查询最近访客时性能最优

---

## 🔍 对业务代码的影响

### 插入数据（无影响）

```java
// ✅ 插入数据时无需特殊处理，PostgreSQL会自动路由到正确的分区
ContentFileAccessLogDO log = new ContentFileAccessLogDO();
log.setId(snowflake.nextId());  // 生成ID
log.setFileId(fileId);
log.setCreateTime(LocalDateTime.now());  // 自动分区路由
// ...
contentFileAccessLogMapper.insert(log);
```

---

### 按ID查询（需要注意）

#### ❌ 不推荐（跨分区扫描）

```java
// 只提供ID，没有create_time
// PostgreSQL需要扫描所有分区！
ContentFileAccessLogDO log = contentFileAccessLogMapper.selectById(123L);
```

**性能：** 慢（扫描所有分区）

---

#### ✅ 推荐（分区裁剪）

```java
// 同时提供ID和create_time范围
// PostgreSQL只扫描相关分区！
LambdaQueryWrapper<ContentFileAccessLogDO> query = new LambdaQueryWrapper<>();
query.eq(ContentFileAccessLogDO::getId, 123L)
     .ge(ContentFileAccessLogDO::getCreateTime, LocalDateTime.now().minusDays(7));  // 最近7天

ContentFileAccessLogDO log = contentFileAccessLogMapper.selectOne(query);
```

**性能：** 快（仅扫描相关分区）

---

### 按时间范围查询（最优场景）

```java
// ✅ 按时间范围查询，充分利用分区裁剪
LambdaQueryWrapper<ContentFileAccessLogDO> query = new LambdaQueryWrapper<>();
query.eq(ContentFileAccessLogDO::getFileId, fileId)
     .between(ContentFileAccessLogDO::getCreateTime,
              LocalDateTime.of(2025, 11, 1, 0, 0),
              LocalDateTime.of(2025, 11, 30, 23, 59));  // 查询11月数据

List<ContentFileAccessLogDO> logs = contentFileAccessLogMapper.selectList(query);
```

**性能：** 极快（仅扫描2025_11分区）

---

## 📈 性能对比

### 场景：查询最近7天的访问日志

#### 无分区表

```sql
-- 扫描全表（假设1000万条数据）
SELECT * FROM access_log
WHERE file_id = 123
  AND create_time >= NOW() - INTERVAL '7 days';
```

- 扫描行数：1000万
- 查询时间：2-5秒

---

#### 分区表（按月分区）

```sql
-- 仅扫描最近1-2个分区（假设每月100万条）
SELECT * FROM content_file_access_log
WHERE file_id = 123
  AND create_time >= NOW() - INTERVAL '7 days';
```

- 扫描行数：100-200万
- 查询时间：0.2-0.5秒
- **性能提升：10倍！**

---

## 🛠️ 最佳实践

### 1. 日志表必须分区

**适合分区的表：**
- ✅ 访问日志（content_file_access_log）
- ✅ 访客记录（member_visitor）
- ✅ 操作日志（audit_log）
- ✅ 行为记录（user_behavior）

**特点：**
- 数据量巨大（千万-亿级）
- 按时间增长
- 查询主要是时间范围查询
- 旧数据需要定期归档/删除

---

### 2. 业务主表不推荐分区

**不适合分区的表：**
- ❌ 用户表（member_profile）
- ❌ 内容表（content_post）
- ❌ 订单表（trade_order）

**原因：**
- 需要频繁按ID查询（不带时间条件）
- 分区反而增加查询复杂度
- 可以用其他方案优化（分表、读写分离）

---

### 3. 复合主键的查询技巧

```java
// ❌ 避免：只查ID（跨分区扫描）
selectById(id)

// ✅ 推荐：加上时间范围（分区裁剪）
selectOne(
    query.eq(id)
         .ge(createTime, recentTime)
)

// ✅ 最佳：时间范围查询（充分利用分区）
selectList(
    query.between(createTime, start, end)
)
```

---

## 🔧 分区维护

### 自动创建未来分区

```sql
-- 定时任务每月1号执行
CREATE OR REPLACE FUNCTION create_future_partitions()
RETURNS INTEGER AS $$
DECLARE
    partition_count INTEGER := 0;
    future_month DATE;
    partition_name TEXT;
BEGIN
    -- 创建未来6个月的分区
    FOR i IN 1..6 LOOP
        future_month := DATE_TRUNC('month', NOW() + (i || ' month')::INTERVAL);
        partition_name := 'content_file_access_log_' || TO_CHAR(future_month, 'YYYY_MM');

        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = partition_name) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF content_file_access_log
                 FOR VALUES FROM (%L) TO (%L)',
                partition_name,
                TO_CHAR(future_month, 'YYYY-MM-DD'),
                TO_CHAR(future_month + INTERVAL '1 month', 'YYYY-MM-DD')
            );
            partition_count := partition_count + 1;
        END IF;
    END LOOP;

    RETURN partition_count;
END;
$$ LANGUAGE plpgsql;
```

---

### 删除过期分区

```sql
-- 定时任务每月1号执行（删除12个月前的分区）
DROP TABLE IF EXISTS content_file_access_log_2024_11;
DROP TABLE IF EXISTS member_visitor_2024_11;
```

---

## 📝 总结

| 项目 | 说明 |
|-----|------|
| **核心规则** | 分区表的主键必须包含分区键 |
| **解决方案** | 使用复合主键 `(id, create_time)` |
| **性能影响** | 查询时提供时间范围，性能提升10倍+ |
| **业务影响** | 插入无影响，查询需注意加时间条件 |
| **适用场景** | 日志表、行为记录等海量时序数据 |
| **维护成本** | 定时任务自动创建/删除分区 |

---

## ✅ 已修复的表

1. ✅ `content_file_access_log` - 文件访问日志表
   - 位置：`09_optimized_content_file_architecture.sql`
   - 修复：`PRIMARY KEY (id, create_time)`

2. ✅ `member_visitor` - 访客记录表
   - 位置：`08_optimized_member_architecture.sql`
   - 修复：`PRIMARY KEY (id, create_time)`

---

**现在可以正常执行SQL脚本了！** ✅
