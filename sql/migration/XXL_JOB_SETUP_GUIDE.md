# XXL-Job Setup Guide - Database Partition Maintenance

## 前提条件

### 1. 启用 XXL-Job

修改各模块的 `application-local.yml`：

**Content 模块**：`yudao-module-content-server/src/main/resources/application-local.yml`
```yaml
xxl:
  job:
    enabled: true  # 改为 true
    admin:
      addresses: http://127.0.0.1:9090/xxl-job-admin
```

**Message 模块**：`yudao-module-message-server/src/main/resources/application-local.yml`
```yaml
xxl:
  job:
    enabled: true  # 改为 true
    admin:
      addresses: http://127.0.0.1:9090/xxl-job-admin
```

### 2. 启动 XXL-Job Admin

芋道框架已经内置了 XXL-Job，访问：
```
http://127.0.0.1:9090/xxl-job-admin
默认账号：admin
默认密码：123456
```

如果未启动，检查：
- `yudao-module-infra` 或 `yudao-server` 是否包含 XXL-Job Admin
- Docker Compose 中是否有 xxl-job-admin 服务

---

## 定时任务配置

### 任务1：内容模块分区维护（每月执行）

**任务名称**：`contentPartitionMaintenanceJob`
**JobHandler**：`contentPartitionMaintenanceJob`
**Cron表达式**：`0 0 2 1 * ?`（每月1号凌晨2点执行）
**负责人**：admin
**执行器**：`content-executor`（或默认执行器）

**功能说明**：
- 为 `content_post` 表创建未来6个月的月分区
- 为 `content_comment` 表创建未来30天的日分区
- 防止新数据插入时找不到分区

---

### 任务2：热榜刷新（每小时执行）

**任务名称**：`contentHotRankRefreshJob`
**JobHandler**：`contentHotRankRefreshJob`
**Cron表达式**：`0 5 * * * ?`（每小时的第5分钟执行）
**负责人**：admin
**执行器**：`content-executor`

**功能说明**：
- 刷新物化视图 `mv_content_hot_rank`
- 更新热榜内容排名
- 使用 CONCURRENTLY 方式，不阻塞查询

---

### 任务3：消息模块分区维护（每月执行）

**任务名称**：`messagePartitionMaintenanceJob`
**JobHandler**：`messagePartitionMaintenanceJob`
**Cron表达式**：`0 0 2 1 * ?`（每月1号凌晨2点执行）
**负责人**：admin
**执行器**：`message-executor`

**功能说明**：
- 为 `message_detail` 表创建未来6个月的月分区

---

## 在 XXL-Job Admin 中配置

### 步骤1：登录控制台

访问：`http://127.0.0.1:9090/xxl-job-admin`

### 步骤2：添加执行器（如果不存在）

导航：`执行器管理` → `新增执行器`

**Content 执行器**：
- AppName：`content-executor`
- 名称：内容模块执行器
- 注册方式：自动注册

**Message 执行器**：
- AppName：`message-executor`
- 名称：消息模块执行器
- 注册方式：自动注册

### 步骤3：添加任务

导航：`任务管理` → `新增任务`

#### 任务1：内容分区维护

```
基础配置：
- 执行器：content-executor
- 任务描述：内容模块分区维护（每月创建未来分区）
- 负责人：admin

调度配置：
- 调度类型：Cron
- Cron：0 0 2 1 * ?
- 运行模式：BEAN

任务配置：
- JobHandler：contentPartitionMaintenanceJob
- 执行参数：（留空）
- 路由策略：第一个
- 阻塞处理策略：单机串行
- 任务超时时间：300（秒）
- 失败重试次数：3
```

#### 任务2：热榜刷新

```
基础配置：
- 执行器：content-executor
- 任务描述：热榜内容刷新（每小时更新）
- 负责人：admin

调度配置：
- 调度类型：Cron
- Cron：0 5 * * * ?
- 运行模式：BEAN

任务配置：
- JobHandler：contentHotRankRefreshJob
- 执行参数：（留空）
- 路由策略：第一个
- 阻塞处理策略：丢弃后续调度
- 任务超时时间：60（秒）
- 失败重试次数：1
```

#### 任务3：消息分区维护

```
基础配置：
- 执行器：message-executor
- 任务描述：消息模块分区维护（每月创建未来分区）
- 负责人：admin

调度配置：
- 调度类型：Cron
- Cron：0 0 2 1 * ?
- 运行模式：BEAN

任务配置：
- JobHandler：messagePartitionMaintenanceJob
- 执行参数：（留空）
- 路由策略：第一个
- 阻塞处理策略：单机串行
- 任务超时时间：300（秒）
- 失败重试次数：3
```

---

## Cron 表达式说明

| Cron | 含义 | 说明 |
|------|------|------|
| `0 0 2 1 * ?` | 每月1号凌晨2点 | 分区维护任务 |
| `0 5 * * * ?` | 每小时第5分钟 | 热榜刷新（避开整点高峰） |
| `0 0 */6 * * ?` | 每6小时执行一次 | 可选：如果每小时刷新太频繁 |

### Cron 在线生成器
- https://cron.qqe2.com/
- https://www.bejson.com/othertools/cron/

---

## 手动执行测试

### 方法1：XXL-Job 控制台

1. 进入 `任务管理`
2. 找到对应任务
3. 点击 `执行` 按钮
4. 查看 `调度日志` 确认执行结果

### 方法2：直接调用数据库函数

```sql
-- 手动创建分区
SELECT create_monthly_partitions('content_post', 6);
SELECT create_daily_partitions('content_comment', 30);
SELECT create_monthly_partitions('message_detail', 6);

-- 手动刷新热榜
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank;
```

---

## 监控和告警

### 查看执行日志

在 XXL-Job Admin 中：
1. `调度日志` → 查看所有任务执行记录
2. 可按执行器、任务、状态筛选
3. 查看详细日志输出

### 失败告警

XXL-Job 支持：
- 邮件告警
- 钉钉告警
- 企业微信告警

配置方式：在 `任务管理` → `编辑任务` → `告警邮件`

---

## 故障排查

### 问题1：任务未执行

**检查项**：
1. XXL-Job Admin 是否正常运行
2. `application-local.yml` 中 `xxl.job.enabled` 是否为 `true`
3. 执行器是否注册成功（XXL-Job Admin → 执行器管理）
4. Cron 表达式是否正确

### 问题2：分区创建失败

**常见原因**：
- 数据库连接失败
- 函数不存在（检查是否执行了 `03_create_materialized_view_and_functions.sql`）
- 权限不足

**解决方法**：
```sql
-- 检查函数是否存在
\df create_monthly_partitions

-- 手动测试
SELECT create_monthly_partitions('content_post', 1);
```

### 问题3：热榜刷新慢

**优化建议**：
- 检查 `mv_content_hot_rank` 是否有索引
- 考虑减少刷新频率（改为每2小时或每6小时）
- 查看数据量，是否需要优化 WHERE 条件

---

## 最佳实践

### 1. 初次部署

手动执行一次，确保正常：
```bash
# 进入 XXL-Job Admin
# 点击每个任务的"执行"按钮
# 查看调度日志，确认成功
```

### 2. 生产环境建议

**分区维护任务**：
- 时间：每月1号凌晨2点（业务低峰期）
- 重试：3次
- 告警：失败后邮件通知

**热榜刷新任务**：
- 时间：每小时第5分钟（避开整点）
- 重试：1次（失败影响小，下次再刷新）
- 超时：60秒

### 3. 监控指标

定期检查：
- 分区数量是否正常增长
- 热榜刷新耗时趋势
- 任务失败率

---

## 代码位置

### Content 模块任务
- `yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/job/ContentPartitionMaintenanceJob.java`
- `yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/job/ContentHotRankRefreshJob.java`

### Message 模块任务
- `yudao-module-message-server/src/main/java/cn/iocoder/yudao/module/message/job/MessagePartitionMaintenanceJob.java`

### 数据库函数
- `sql/migration/03_create_materialized_view_and_functions.sql`
- `sql/migration/04_create_message_tables.sql`

---

## FAQ

**Q：XXL-Job Admin 在哪里？**
A：芋道框架已集成，通常在 `http://127.0.0.1:9090/xxl-job-admin`，账号 admin/123456

**Q：可以不用 XXL-Job 吗？**
A：可以。有3种替代方案：
1. Linux Cron + psql 命令
2. Spring `@Scheduled` 注解（单体应用）
3. PostgreSQL pg_cron 扩展（数据库内调度）

**Q：多久创建一次分区？**
A：每月1次即可。函数会自动检测已存在的分区，不会重复创建。

**Q：热榜一定要每小时刷新吗？**
A：不一定。根据业务需求调整：
- 高活跃度：每小时或更频繁
- 中等活跃度：每2-6小时
- 低活跃度：每天刷新

**Q：如何知道任务是否执行成功？**
A：查看 XXL-Job Admin 的调度日志，或查看应用日志中的 `[contentPartitionMaintenanceJob]` 等关键字。

---

## 总结

✅ **已创建的定时任务**：
1. `ContentPartitionMaintenanceJob` - 内容分区维护（每月）
2. `ContentHotRankRefreshJob` - 热榜刷新（每小时）
3. `MessagePartitionMaintenanceJob` - 消息分区维护（每月）

✅ **配置步骤**：
1. 启用 XXL-Job（`enabled: true`）
2. 访问 XXL-Job Admin 控制台
3. 配置3个定时任务
4. 手动执行测试

✅ **维护建议**：
- 监控任务执行日志
- 定期检查分区数量
- 优化热榜刷新频率

**芋道框架的 XXL-Job 完全可以满足需求，无需额外部署调度平台！**
