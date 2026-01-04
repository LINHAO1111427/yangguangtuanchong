# 文件存储架构设计

## 📊 架构全景图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        文件存储分层架构                                │
└─────────────────────────────────────────────────────────────────────┘

┌────────────────────────┐
│   物理存储层 (Storage)  │
├────────────────────────┤
│  MinIO (主存储)         │
│  阿里云OSS (备用)        │
│  腾讯云COS (备用)        │
└────────────────────────┘
           ▲
           │ 上传/下载
           │
┌──────────┴─────────────────────────────────────────────────────────┐
│                    存储配置层 (Config)                               │
├─────────────────────────────────────────────────────────────────────┤
│  infra_file_config (存储配置表) - ruoyi-vue-pro 数据库              │
│  - 存储类型、endpoint、accessKey、bucket等配置                       │
│  - 所有模块共享同一套配置                                            │
└─────────────────────────────────────────────────────────────────────┘
           ▲
           │ 配置引用 (config_id)
           │
┌──────────┴─────────────┬─────────────────────────────────────────────┐
│                        │                                             │
│   ┌─────────────────────────┐      ┌─────────────────────────────┐  │
│   │  通用文件元数据层        │      │  内容文件元数据层            │  │
│   ├─────────────────────────┤      ├─────────────────────────────┤  │
│   │  infra_file             │      │  content_file               │  │
│   │  (ruoyi-vue-pro 库)     │      │  (xiaolvshu 库)             │  │
│   ├─────────────────────────┤      ├─────────────────────────────┤  │
│   │  用途：系统级文件        │      │  用途：用户内容文件         │  │
│   │  - 导出Excel/PDF        │      │  - 视频文件 ⭐              │  │
│   │  - 后台上传配置文件      │      │  - 图片文件 ⭐              │  │
│   │  - 系统临时文件          │      │  - 封面图片                │  │
│   │                         │      │  - 用户头像                │  │
│   ├─────────────────────────┤      ├─────────────────────────────┤  │
│   │  数据量：小 (万级)       │      │  数据量：巨大 (千万级)      │  │
│   │  查询频率：低            │      │  查询频率：极高             │  │
│   │  业务关联：弱            │      │  业务关联：强               │  │
│   │  生命周期：长期保留      │      │  生命周期：按策略归档       │  │
│   └─────────────────────────┘      └─────────────────────────────┘  │
│                                                ▲                     │
│                                                │ 关联 (post_id)      │
│                                                │                     │
│                                     ┌─────────────────────────────┐ │
│                                     │  业务层                      │ │
│                                     ├─────────────────────────────┤ │
│                                     │  content_post               │ │
│                                     │  content_comment            │ │
│                                     │  content_like               │ │
│                                     └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 设计原则

### 1. 职责分离

**infra_file (通用文件)**
- **职责**：管理系统级、通用型文件
- **特点**：数据量小、查询频率低、与业务解耦
- **生命周期**：长期保留

**content_file (内容文件)**
- **职责**：管理用户上传的内容文件（视频、图片）
- **特点**：数据量巨大、查询频率极高、与业务强关联
- **生命周期**：按策略归档（如保留2年）

### 2. 性能优化

**同库关联：**
```sql
-- content_file 与 content_post 在同一个库 (xiaolvshu)
SELECT p.title, f.url, f.size
FROM content_post p
JOIN content_file f ON p.video_id = f.id  -- ✅ 同库JOIN，性能最优
WHERE p.author_id = 123;
```

**跨库查询最小化：**
```sql
-- config_id 跨库引用 infra_file_config
-- 但查询频率极低（仅上传时需要）
SELECT * FROM content_file WHERE id = 456;  -- ✅ 不需要JOIN config

-- 仅在上传时查询配置
SELECT * FROM infra_file_config WHERE id = 1;  -- ⚠️ 跨库，但频率低
```

### 3. 数据隔离

**数据量预估（10年）：**

| 表 | 数据量 | 存储空间 | 索引空间 |
|----|--------|---------|---------|
| infra_file | 10万 | 50MB | 20MB |
| content_file | 6000万 | 30GB | 12GB |

**隔离的好处：**
- ✅ infra_file保持轻量，系统文件查询不受影响
- ✅ content_file独立优化（分区、归档、索引）
- ✅ 职责清晰，易于维护

---

## 📁 表结构对比

### infra_file (通用文件表)

```sql
-- 位置：ruoyi-vue-pro 数据库
CREATE TABLE infra_file (
    id          BIGINT PRIMARY KEY,
    config_id   BIGINT NOT NULL,
    name        VARCHAR(256),
    path        VARCHAR(512),
    url         VARCHAR(1024),
    type        VARCHAR(128),
    size        BIGINT,

    -- 通用字段
    creator     VARCHAR(64),
    create_time TIMESTAMPTZ,
    deleted     SMALLINT
);

-- 索引
CREATE INDEX idx_infra_file_create_time ON infra_file (create_time DESC);
```

**典型数据：**
```
id | name              | path                    | size
---+-------------------+-------------------------+-------
1  | export_users.xlsx | system/export/xxx.xlsx  | 2MB
2  | config.json       | system/config/xxx.json  | 10KB
3  | temp_report.pdf   | system/temp/xxx.pdf     | 500KB
```

---

### content_file (内容文件表)

```sql
-- 位置：xiaolvshu 数据库
CREATE TABLE content_file (
    id               BIGINT PRIMARY KEY,
    config_id        BIGINT NOT NULL,  -- 关联 infra_file_config.id
    name             VARCHAR(256),
    path             VARCHAR(512),
    url              VARCHAR(1024),
    type             VARCHAR(128),
    size             BIGINT,
    hash             VARCHAR(64),      -- ⭐ 文件哈希（秒传）

    -- 业务字段 ⭐
    biz_type         SMALLINT,         -- 0=视频 1=图片 2=封面 3=头像
    author_id        BIGINT NOT NULL,  -- 作者ID
    post_id          BIGINT,           -- 关联的作品ID

    -- 状态管理
    status           SMALLINT,         -- 0=正常 1=删除中 2=已归档
    access_count     INTEGER,          -- 访问次数
    last_access_time TIMESTAMPTZ,      -- 最后访问时间

    -- CDN相关
    cdn_url          VARCHAR(1024),
    cdn_status       SMALLINT,

    -- 生命周期
    archive_time     TIMESTAMPTZ,      -- 归档时间
    delete_time      TIMESTAMPTZ,      -- 删除时间

    -- 标准字段
    creator          VARCHAR(64),
    create_time      TIMESTAMPTZ,
    deleted          SMALLINT
);

-- 索引（关键性能优化）
CREATE UNIQUE INDEX uk_content_file_hash ON content_file (hash);
CREATE INDEX idx_content_file_author_time ON content_file (author_id, create_time DESC);
CREATE INDEX idx_content_file_post ON content_file (post_id);
CREATE INDEX idx_content_file_biz_type ON content_file (biz_type, create_time DESC);
CREATE INDEX idx_content_file_last_access ON content_file (last_access_time DESC);
```

**典型数据：**
```
id | name              | path                          | size  | biz_type | author_id | post_id
---+-------------------+-------------------------------+-------+----------+-----------+--------
1M | video_123.mp4     | content/video/2025/11/xxx.mp4 | 50MB  | 0(视频)  | 1001      | 5001
2M | photo_456.jpg     | content/image/2025/11/xxx.jpg | 2MB   | 1(图片)  | 1002      | 5002
3M | cover_789.jpg     | content/cover/2025/11/xxx.jpg | 500KB | 2(封面)  | 1001      | 5001
```

---

## 🔄 文件上传流程

### 1. 普通上传流程

```
┌────────┐      ┌────────────┐      ┌──────────────┐      ┌─────────┐
│ 客户端  │ ───> │ content模块 │ ───> │ infra模块     │ ───> │ MinIO   │
│        │      │            │      │ (获取配置)    │      │         │
└────────┘      └────────────┘      └──────────────┘      └─────────┘
                      │                                          │
                      │ 2. 上传成功                               │
                      ▼                                          │
              ┌──────────────┐                                   │
              │ content_file  │ ◄──────────────────────────────┘
              │ (保存元数据)  │
              └──────────────┘
```

**代码示例：**
```java
// ContentFileService.java
public String uploadFile(MultipartFile file, Long authorId, Long postId) {
    // 1. 获取存储配置（跨库，但查询频率低）
    InfraFileConfigDO config = infraFileConfigApi.getMasterConfig();

    // 2. 上传到MinIO
    String url = minioClient.upload(file, config);

    // 3. 计算文件hash（用于秒传）
    String hash = FileUtils.calculateHash(file);

    // 4. 保存元数据到content_file（同库）
    ContentFileDO fileDO = new ContentFileDO();
    fileDO.setConfigId(config.getId());
    fileDO.setAuthorId(authorId);
    fileDO.setPostId(postId);
    fileDO.setHash(hash);
    fileDO.setUrl(url);
    // ... 其他字段

    contentFileMapper.insert(fileDO);

    return url;
}
```

---

### 2. 秒传流程（基于hash去重）

```
┌────────┐      ┌────────────┐
│ 客户端  │ ───> │ content模块 │
│        │      │            │
└────────┘      └────────────┘
                      │
                      │ 1. 计算文件hash
                      ▼
              ┌──────────────────────────┐
              │ SELECT * FROM content_file│
              │ WHERE hash = 'abc123...'  │
              └──────────────────────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼ 存在                   ▼ 不存在
   ┌─────────────┐         ┌─────────────┐
   │ 直接返回URL  │         │ 执行普通上传 │
   │ (秒传成功)   │         │             │
   └─────────────┘         └─────────────┘
```

---

## 📊 查询性能对比

### 场景1：获取作品详情（含文件信息）

**方案A：infra_file（跨库JOIN）❌**
```sql
-- 性能：10ms ~ 100ms（慢）
SELECT
    p.id, p.title, p.content,
    f.url, f.size, f.cdn_url
FROM xiaolvshu.content_post p
LEFT JOIN ruoyi-vue-pro.infra_file f ON p.video_id = f.id  -- ⚠️ 跨库JOIN
WHERE p.id = 123;
```

**方案B：content_file（同库JOIN）✅**
```sql
-- 性能：0.1ms ~ 1ms（快）
SELECT
    p.id, p.title, p.content,
    f.url, f.size, f.cdn_url
FROM content_post p
LEFT JOIN content_file f ON p.video_id = f.id  -- ✅ 同库JOIN
WHERE p.id = 123;
```

**性能差异：快10-100倍！**

---

### 场景2：获取用户所有视频

**方案A：infra_file（跨库查询）❌**
```sql
-- 需要两次查询
-- 第一步：查content_post获取video_id列表
SELECT video_id FROM xiaolvshu.content_post WHERE author_id = 123;
-- video_id: [1001, 1002, 1003, ...]

-- 第二步：跨库查询infra_file
SELECT * FROM ruoyi-vue-pro.infra_file WHERE id IN (1001, 1002, 1003, ...);
```

**方案B：content_file（同库JOIN）✅**
```sql
-- 一次查询搞定
SELECT
    p.id, p.title,
    f.url, f.size, f.duration
FROM content_post p
LEFT JOIN content_file f ON p.video_id = f.id
WHERE p.author_id = 123
ORDER BY p.create_time DESC;
```

---

## 🗂️ 数据生命周期管理

### infra_file

**策略：长期保留**
- 系统文件通常需要长期保存
- 无需归档或定期清理
- 数据量小，不影响性能

---

### content_file

**策略：分级管理**

| 数据状态 | 保留时间 | 存储位置 | 访问频率 |
|---------|---------|---------|---------|
| 热数据 | 1个月内 | MinIO + CDN | 极高 |
| 温数据 | 1-12个月 | MinIO | 中等 |
| 冷数据 | 1-2年 | 归档存储 | 低 |
| 过期数据 | >2年 | 删除 | - |

**实现方式：**

```sql
-- 1. 标记待归档文件（定时任务每天执行）
UPDATE content_file
SET status = 2, archive_time = NOW()
WHERE status = 0
  AND last_access_time < NOW() - INTERVAL '12 months'
  AND create_time < NOW() - INTERVAL '12 months';

-- 2. 标记待删除文件（定时任务每周执行）
UPDATE content_file
SET status = 3, delete_time = NOW()
WHERE status = 2
  AND archive_time < NOW() - INTERVAL '12 months';

-- 3. 物理删除文件（定时任务每月执行）
DELETE FROM content_file
WHERE status = 3
  AND delete_time < NOW() - INTERVAL '1 month';
```

---

## 🔧 配置共享机制

### infra_file_config（存储配置表）

**所有模块共享同一套配置：**

```sql
-- 位置：ruoyi-vue-pro 数据库
CREATE TABLE infra_file_config (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(63),
    storage  SMALLINT,  -- 0=MinIO 1=阿里云OSS 2=腾讯云COS
    domain   VARCHAR(255),
    config   JSON,      -- {"endpoint": "...", "accessKey": "...", "bucket": "..."}
    master   SMALLINT   -- 0=备用 1=主配置
);

-- 示例数据
INSERT INTO infra_file_config (id, name, storage, domain, config, master) VALUES
(1, 'MinIO-Primary', 0, 'https://minio.xiaolvshu.com', '{"endpoint":"...","bucket":"xiaolvshu"}', 1),
(2, 'AliOSS-Backup', 1, 'https://oss.aliyuncs.com', '{"endpoint":"...","bucket":"xiaolvshu"}', 0);
```

**查询方式：**

```java
// InfraFileConfigApi.java (Feign接口)
@FeignClient(name = "infra-service", path = "/api/infra/file-config")
public interface InfraFileConfigApi {
    @GetMapping("/master")
    InfraFileConfigDO getMasterConfig();
}

// ContentFileService.java 使用
@Autowired
private InfraFileConfigApi infraFileConfigApi;

public void uploadFile() {
    // 跨库调用，但通过Feign API，查询频率低
    InfraFileConfigDO config = infraFileConfigApi.getMasterConfig();

    // 使用配置上传文件...
}
```

---

## 📈 扩展性设计

### 支持多种存储

**通过 config_id 灵活切换：**

```java
// 根据文件类型选择不同存储
public String uploadFile(MultipartFile file, Integer bizType) {
    InfraFileConfigDO config;

    if (bizType == 0) {  // 视频
        config = infraFileConfigApi.getConfigByName("MinIO-Video");
    } else if (bizType == 1) {  // 图片
        config = infraFileConfigApi.getConfigByName("AliOSS-Image");
    }

    // 根据不同配置上传到不同存储
    String url = storageService.upload(file, config);

    // 保存元数据
    ContentFileDO fileDO = new ContentFileDO();
    fileDO.setConfigId(config.getId());  // 记录使用的配置
    // ...
}
```

---

## 🎯 总结

### 为什么 content_file 在 xiaolvshu（内容库）？

| 维度 | 理由 |
|-----|------|
| **职责分离** | 内容业务文件与系统文件职责不同 |
| **性能优化** | 与content_post同库JOIN，性能最优 |
| **数据隔离** | 千万级数据不影响系统文件查询 |
| **业务关联** | 强关联content_post，需要频繁JOIN |
| **生命周期** | 需要独立的归档和清理策略 |
| **扩展性** | 支持分区、分片、读写分离等优化 |

### 为什么复用 infra_file_config？

| 维度 | 理由 |
|-----|------|
| **配置统一** | 所有模块使用同一套存储配置 |
| **查询频率低** | 仅上传时需要，跨库影响小 |
| **避免重复** | 不重复定义存储配置 |
| **易于管理** | 集中管理MinIO/OSS等配置 |

---

## ✅ 最佳实践

1. **infra_file**：用于系统级、通用型文件（Excel导出、临时文件等）
2. **content_file**：用于用户内容文件（视频、图片、封面等）
3. **infra_file_config**：统一管理存储配置（所有模块共享）
4. **性能优化**：同库JOIN > Feign API调用 > 跨库JOIN
5. **数据隔离**：业务文件与系统文件分开存储
6. **生命周期管理**：热温冷数据分级存储和归档

---

**结论**：content_file 在 xiaolvshu（内容库）是最优方案！✅
