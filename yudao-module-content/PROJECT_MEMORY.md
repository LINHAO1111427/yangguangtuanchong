# Content模块 - 内容存储改造方案

## 🔴 当前问题（P0级）

### 问题描述
Content模块的视频/图片文件存储调用infra-server，存在严重架构缺陷：

1. **文件元数据存储在infra_file表**（xiaolvshu_base库）
2. **Content无法独立管理文件生命周期**
3. **文件删除、清理、归档需要跨模块协调**
4. **Infra模块成为content的核心依赖**
5. **无法对文件做业务扩展（审核、秒传、分级存储）**

### 风险分析
- **数据安全风险**：content备份必须带infra表，无法单独备份
- **性能瓶颈风险**：infra_file表存储所有业务文件，查询性能下降
- **扩展性阻塞**：content想做视频转码、CDN加速，需要infra配合
- **维护成本高**：文件清理任务跨模块，容易遗漏造成垃圾数据

### 影响范围
- **阻塞功能**：视频转码、文件秒传、CDN预热、文件分级存储
- **涉及接口**：上传视频/图片、删除内容、文件清理定时任务
- **数据规模**：预计文件量10万→1000万，必须立即改造

---

## ✅ 改造目标

### 核心目标（Week 2完成）
1. **文件元数据独立存储**：创建`content_file`表（xiaolvshu_content库）
2. **文件服务内聚化**：content模块内部实现FileService
3. **业务字段扩展**：添加hash、biz_type、status等字段支持业务功能
4. **平滑迁移**：历史数据迁移，双写验证

### 新增功能
- ✅ **秒传功能**：通过file_hash判断文件已存在
- ✅ **文件状态管理**：正常、删除中、已归档
- ✅ **业务类型区分**：视频、图片、封面、头像等
- ✅ **文件CDN预热**：根据访问热度自动触发
- ✅ **分级存储**：热数据在MinIO，冷数据迁移到S3/Glacier

### 性能目标
- 文件上传：支持1GB+大文件分片上传
- 文件查询：< 5ms
- 文件删除：支持批量删除1000个文件

---

## 📐 数据库设计

### 1. 文件元数据主表

```sql
-- xiaolvshu_content.content_file
CREATE TABLE content_file (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    config_id        BIGINT       NOT NULL COMMENT '存储配置ID（关联infra_file_config）',
    name             VARCHAR(256) NOT NULL COMMENT '原文件名',
    path             VARCHAR(512) NOT NULL COMMENT '存储路径',
    url              VARCHAR(1024) NOT NULL COMMENT '访问URL',
    type             VARCHAR(128) NULL COMMENT 'MIME类型',
    size             BIGINT       NOT NULL COMMENT '文件大小',
    hash             VARCHAR(64)  NULL COMMENT '文件哈希（MD5/SHA256）',

    -- 业务字段（核心！）
    biz_type         SMALLINT     NOT NULL DEFAULT 0 COMMENT '业务类型：0=视频 1=图片 2=封面 3=头像 4=其他',
    author_id        BIGINT       NOT NULL COMMENT '上传者ID',
    post_id          BIGINT       NULL COMMENT '关联内容ID',
    status           SMALLINT     NOT NULL DEFAULT 0 COMMENT '状态：0=正常 1=删除中 2=已归档 3=已删除',
    storage_type     SMALLINT     NOT NULL DEFAULT 0 COMMENT '存储类型：0=MinIO 1=阿里云OSS 2=腾讯云COS 3=AWS S3',
    access_count     INTEGER      NOT NULL DEFAULT 0 COMMENT '访问次数',
    last_access_time TIMESTAMP    NULL COMMENT '最后访问时间',

    -- CDN相关
    cdn_url          VARCHAR(1024) NULL COMMENT 'CDN加速URL',
    cdn_status       SMALLINT     NOT NULL DEFAULT 0 COMMENT 'CDN状态：0=未预热 1=已预热 2=预热失败',

    -- 生命周期
    archive_time     TIMESTAMP    NULL COMMENT '归档时间',
    delete_time      TIMESTAMP    NULL COMMENT '删除时间',

    -- 芋道标准字段
    creator          VARCHAR(64)  DEFAULT '',
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          VARCHAR(64)  DEFAULT '',
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          SMALLINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    -- 索引（关键！）
    UNIQUE KEY uk_hash (hash) COMMENT '秒传唯一索引',
    INDEX idx_author_time (author_id, create_time DESC) COMMENT '作者文件列表',
    INDEX idx_post (post_id) COMMENT '内容关联文件',
    INDEX idx_biz_type (biz_type, create_time DESC) COMMENT '按业务类型查询',
    INDEX idx_status (status, create_time DESC) COMMENT '按状态查询',
    INDEX idx_last_access (last_access_time DESC) COMMENT '冷热数据识别'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容文件元数据表（独立存储）';
```

**关键设计说明：**
- **hash字段**：实现秒传核心，文件上传前先计算hash，查询是否已存在
- **biz_type**：区分业务类型，便于统计和管理
- **status**：支持文件生命周期管理，归档/删除异步处理
- **author_id**：每个文件必须记录上传者，便于后续清理
- **cdn_url**：支持CDN加速，可配置独立域名
- **索引优化**：覆盖90%的查询场景

---

### 2. 文件存储配置表

```sql
-- xiaolvshu_content.content_file_config
CREATE TABLE content_file_config (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    name          VARCHAR(63)  NOT NULL COMMENT '配置名称',
    storage       SMALLINT     NOT NULL COMMENT '存储类型：0=MinIO 1=阿里云OSS 2=腾讯云COS',
    base_path     VARCHAR(255) NOT NULL COMMENT '基础路径',
    domain        VARCHAR(255) NOT NULL COMMENT '访问域名',
    config        JSON         NOT NULL COMMENT '配置JSON（accessKey/secretKey/bucket等）',
    remark        VARCHAR(255) NULL COMMENT '备注',
    master        SMALLINT     NOT NULL DEFAULT 0 COMMENT '是否主配置：0=否 1=是',

    creator       VARCHAR(64)  DEFAULT '',
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       VARCHAR(64)  DEFAULT '',
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT     NOT NULL DEFAULT 0,

    UNIQUE KEY uk_name (name),
    INDEX idx_master (master)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储配置表';

-- 初始化MinIO配置
INSERT INTO content_file_config (id, name, storage, base_path, domain, config, master)
VALUES (1, 'MinIO-Primary', 0, 'content', 'https://minio.xiaolvshu.com',
    '{"endpoint": "http://127.0.0.1:9000", "accessKey": "minioadmin", "secretKey": "minioadmin", "bucket": "xiaolvshu"}', 1);
```

---

### 3. 文件访问日志表

```sql
-- xiaolvshu_content.content_file_access_log
CREATE TABLE content_file_access_log (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    file_id     BIGINT       NOT NULL COMMENT '文件ID',
    user_id     BIGINT       NULL COMMENT '访问者ID',
    ip          VARCHAR(50)  NULL COMMENT '访问IP',
    user_agent  VARCHAR(255) NULL COMMENT 'UserAgent',
    referer     VARCHAR(255) NULL COMMENT '来源页面',
    status      SMALLINT     NOT NULL DEFAULT 1 COMMENT '状态：0=失败 1=成功',
    error_msg   VARCHAR(200) NULL COMMENT '错误信息',
    cost_time   INTEGER      NULL COMMENT '耗时(ms)',

    creator     VARCHAR(64)  DEFAULT '',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    deleted     SMALLINT     NOT NULL DEFAULT 0,

    INDEX idx_file_id (file_id, create_time DESC),
    INDEX idx_user_id (user_id, create_time DESC),
    INDEX idx_ip (ip, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件访问日志表'
PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027)
);
```

---

## 🔧 模块代码改造

### 1. 从Infra调用改为内部调用

**改造前（❌ 错误）：**
```java
@RestController
public class AppContentMediaController {
    @Resource
    private FileApi fileApi;  // 跨模块调用！

    @PostMapping("/upload_video")
    public CommonResult<ContentMediaUploadRespVO> uploadVideo(...) {
        String url = fileApi.createFile(bytes, fileName, directory, contentType);
        // ...
    }
}
```

**改造后（✅ 正确）：**
```java
@RestController
public class AppContentMediaController {
    @Resource
    private ContentFileService contentFileService;  // 内部服务！

    @PostMapping("/upload_video")
    public CommonResult<ContentMediaUploadRespVO> uploadVideo(...) {
        // 1. 校验视频（大小/格式）
        validateVideoFile(file);

        // 2. 计算文件hash
        String hash = calculateFileHash(file.getBytes());

        // 3. 秒传检查
        ContentFileDO existingFile = contentFileService.getByHash(hash);
        if (existingFile != null) {
            // 秒传成功，直接返回已有URL
            return success(buildRespVO(existingFile));
        }

        // 4. 上传文件（内部服务）
        ContentFileCreateReqDTO req = new ContentFileCreateReqDTO();
        req.setContent(file.getBytes());
        req.setName(fileName);
        req.setDirectory(directory);
        req.setType(contentType);
        req.setHash(hash);
        req.setBizType(ContentFileBizTypeEnum.VIDEO.getValue());
        req.setAuthorId(getCurrentUserId());

        ContentFileDO fileDO = contentFileService.createFile(req);

        return success(buildRespVO(fileDO));
    }
}
```

---

### 2. ContentFileService接口

```java
public interface ContentFileService {

    /**
     * 创建文件（支持秒传）
     */
    ContentFileDO createFile(ContentFileCreateReqDTO req);

    /**
     * 根据hash查询文件（秒传）
     */
    ContentFileDO getByHash(String hash);

    /**
     * 删除文件（逻辑删除，异步物理删除）
     */
    void deleteFile(Long fileId);

    /**
     * 批量删除文件
     */
    void batchDeleteFiles(List<Long> fileIds);

    /**
     * 归档冷数据（定时任务）
     */
    void archiveColdFiles(LocalDateTime beforeTime);

    /**
     * 获取文件的CDN URL
     */
    String getCdnUrl(Long fileId);

    /**
     * 记录文件访问日志
     */
    void recordAccess(Long fileId, Long userId, String ip, String userAgent);

    /**
     * 查询用户文件列表
     */
    PageResult<ContentFileDO> getUserFilePage(Long userId, ContentFilePageReqVO pageReq);

    /**
     * 根据内容ID查询关联文件
     */
    List<ContentFileDO> getFilesByPostId(Long postId);

    /**
     * CDN预热文件
     */
    void preloadCdn(Long fileId);
}
```

---

### 3. 实现类

```java
@Service
@Slf4j
public class ContentFileServiceImpl implements ContentFileService {

    @Resource
    private ContentFileMapper fileMapper;
    @Resource
    private ContentFileConfigMapper configMapper;
    @Resource
    private FileStorageClient fileStorageClient;  // 存储客户端
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // Redis缓存key
    private static final String FILE_CACHE_KEY = "content:file:%d";
    private static final String HASH_CACHE_KEY = "content:file:hash:%s";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentFileDO createFile(ContentFileCreateReqDTO req) {
        // 1. 参数校验
        validateFile(req.getContent(), req.getName());

        // 2. 秒传检查（如果包含hash）
        if (StrUtil.isNotBlank(req.getHash())) {
            ContentFileDO existing = getByHash(req.getHash());
            if (existing != null) {
                log.info("秒传成功：hash={}, fileId={}", req.getHash(), existing.getId());
                return existing;
            }
        }

        // 3. 查询存储配置
        ContentFileConfigDO config = getMasterConfig();

        // 4. 构造存储路径
        String path = generatePath(req.getDirectory(), req.getName());

        // 5. 上传文件到存储系统
        FileStorageResult storageResult;
        try {
            storageResult = fileStorageClient.upload(
                config, req.getContent(), path, req.getType()
            );
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw exception(FILE_UPLOAD_FAILED);
        }

        // 6. 构造文件元数据
        ContentFileDO file = new ContentFileDO();
        file.setConfigId(config.getId());
        file.setName(req.getName());
        file.setPath(path);
        file.setUrl(storageResult.getUrl());
        file.setType(req.getType());
        file.setSize((long) req.getContent().length);
        file.setHash(req.getHash());
        file.setBizType(req.getBizType());
        file.setAuthorId(req.getAuthorId());
        file.setPostId(req.getPostId());
        file.setStorageType(config.getStorage());
        file.setStatus(ContentFileStatusEnum.NORMAL.getValue());

        // 7. 保存到数据库
        fileMapper.insert(file);

        // 8. 记录访问日志
        recordAccess(file.getId(), req.getAuthorId(), req.getIp(), req.getUserAgent());

        // 9. 写入缓存
        cacheFile(file);

        log.info("文件上传成功：fileId={}, size={}", file.getId(), file.getSize());
        return file;
    }

    @Override
    public ContentFileDO getByHash(String hash) {
        // 1. 从缓存读取
        String cacheKey = String.format(HASH_CACHE_KEY, hash);
        Long fileId = (Long) redissonClient.getBucket(cacheKey).get();

        if (fileId != null) {
            // 2. 查询详情
            return getFile(fileId);
        }

        // 3. 从数据库查询
        ContentFileDO file = fileMapper.selectOne(
            new LambdaQueryWrapper<ContentFileDO>()
                .eq(ContentFileDO::getHash, hash)
                .eq(ContentFileDO::getStatus, ContentFileStatusEnum.NORMAL.getValue())
        );

        // 4. 写入缓存
        if (file != null) {
            cacheFile(file);
        }

        return file;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        // 1. 查询文件
        ContentFileDO file = fileMapper.selectById(fileId);
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 2. 逻辑删除（状态改为删除中）
        fileMapper.updateStatus(fileId, ContentFileStatusEnum.DELETING.getValue());

        // 3. 发送MQ消息，异步物理删除
        rocketMQTemplate.sendOneWay("FILE_DELETE_TOPIC", file);

        // 4. 清除缓存
        String cacheKey = String.format(FILE_CACHE_KEY, fileId);
        redissonClient.getBucket(cacheKey).delete();

        log.info("文件删除已提交：fileId={}", fileId);
    }

    /**
     * CDN预热（定时任务）
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
    public void preloadHotFiles() {
        // 1. 查询热门文件（最近7天访问>100次）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ContentFileDO> hotFiles = fileMapper.selectHotFiles(sevenDaysAgo, 100);

        // 2. 批量CDN预热
        for (ContentFileDO file : hotFiles) {
            if (StrUtil.isBlank(file.getCdnUrl())) {
                continue;
            }

            try {
                cdnService.preload(file.getCdnUrl());
                fileMapper.updateCdnStatus(file.getId(), ContentFileCdnStatusEnum.PRELOADED.getValue());
                log.info("CDN预热成功：fileId={}", file.getId());
            } catch (Exception e) {
                log.error("CDN预热失败：fileId={}", file.getId(), e);
            }
        }
    }

    /**
     * 归档冷数据（定时任务）
     */
    @Scheduled(cron = "0 0 3 * * ?")  // 每天凌晨3点
    public void archiveColdFiles() {
        // 1. 查询冷数据（90天未访问）
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<ContentFileDO> coldFiles = fileMapper.selectColdFiles(ninetyDaysAgo, 1000);

        // 2. 迁移到S3/Glacier
        for (ContentFileDO file : coldFiles) {
            try {
                fileStorageClient.archiveToS3(file);
                fileMapper.updateStatus(file.getId(), ContentFileStatusEnum.ARCHIVED.getValue());
                log.info("文件归档成功：fileId={}", file.getId());
            } catch (Exception e) {
                log.error("文件归档失败：fileId={}", file.getId(), e);
            }
        }
    }

    private void cacheFile(ContentFileDO file) {
        String cacheKey = String.format(FILE_CACHE_KEY, file.getId());
        redissonClient.getBucket(cacheKey).set(file, 30, TimeUnit.MINUTES);

        if (StrUtil.isNotBlank(file.getHash())) {
            String hashCacheKey = String.format(HASH_CACHE_KEY, file.getHash());
            redissonClient.getBucket(hashCacheKey).set(file.getId(), 30, TimeUnit.MINUTES);
        }
    }

    private ContentFileConfigDO getMasterConfig() {
        ContentFileConfigDO config = configMapper.selectOne(
            new LambdaQueryWrapper<ContentFileConfigDO>()
                .eq(ContentFileConfigDO::getMaster, 1)
                .eq(ContentFileConfigDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
        );
        if (config == null) {
            throw exception(CONFIG_NOT_EXISTS);
        }
        return config;
    }

    private String generatePath(String directory, String filename) {
        // 路径格式：content/video/2025/11/12/897e34f2-1234-4567-abcdef.mp4
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = IdUtil.fastSimpleUUID();
        String ext = FileUtil.extName(filename);
        return StrUtil.format("{}/{}/{}", directory, datePath, uuid + "." + ext);
    }

    private void validateFile(byte[] content, String filename) {
        if (ArrayUtil.isEmpty(content)) {
            throw exception(FILE_CONTENT_EMPTY);
        }
        if (content.length > 600 * 1024 * 1024) {  // 600MB
            throw exception(FILE_SIZE_TOO_LARGE);
        }
        // TODO: 校验文件类型白名单
    }
}
```

---

## 🔄 迁移方案

### 历史数据迁移

```sql
-- 1. 从infra_file迁移到content_file
INSERT INTO content_file (
    id, config_id, name, path, url, type, size, hash, biz_type, author_id,
    status, storage_type, create_time, update_time, deleted
)
SELECT
    id + 10000000 AS id,  -- ID偏移避免冲突
    config_id,
    name,
    path,
    url,
    type,
    CAST(size AS BIGINT) AS size,
    NULL AS hash,  -- 历史数据无hash，需要异步补充
    CASE
        WHEN type LIKE 'video/%' THEN 0  -- 视频
        WHEN type LIKE 'image/%' THEN 1  -- 图片
        ELSE 4
    END AS biz_type,
    0 AS author_id,  -- 历史数据无法获取作者，需要后续补充
    0 AS status,  -- 正常
    0 AS storage_type,  -- MinIO
    create_time,
    update_time,
    deleted
FROM xiaolvshu_base.infra_file
WHERE path LIKE 'content/%';  -- 只迁移content相关文件

-- 2. 更新hash（可以通过文件内容重新计算）
-- TODO: 需要开发脚本读取文件重新计算hash

-- 3. 更新author_id（需要根据业务关联查询）
-- TODO: 根据content_post表关联更新author_id
```

---

### 双写方案

**Week 1-2：双写阶段**
- Content模块同时写入`infra_file`和`content_file`
- 验证数据一致性
- 监控写入性能

**Week 3：切换阶段**
- 读取切换到`content_file`
- 停止写入`infra_file`
- 观察1周无问题后，删除infra中的content文件记录

**Week 4：清理阶段**
- 物理删除`infra_file`中`path LIKE 'content/%'`的数据
- 优化content_file表索引

---

## 📊 性能优化

### 1. 秒传功能

```java
/**
 * 秒传实现流程
 */
public ContentFileDO createFile(ContentFileCreateReqDTO req) {
    // 1. 计算文件hash（前端或后端）
    String hash = calculateHash(req.getContent());

    // 2. 查询文件是否存在
    ContentFileDO existingFile = getByHash(hash);
    if (existingFile != null) {
        // 3. 返回已有文件（秒传成功）
        return existingFile;
    }

    // 4. 正常上传流程
    return uploadNewFile(req, hash);
}

private String calculateHash(byte[] content) {
    // 使用SHA256计算hash
    return SecureUtil.sha256().digestHex(content);
}
```

### 2. CDN优化

```java
/**
 * CDN预热功能
 */
public void preloadCdn(Long fileId) {
    // 1. 查询文件
    ContentFileDO file = getFile(fileId);
    if (file == null || StrUtil.isBlank(file.getUrl())) {
        return;
    }

    // 2. 判断是否需要预热（访问次数>100或者视频文件）
    if (file.getAccessCount() < 100 && !file.getType().startsWith("video/")) {
        return;
    }

    // 3. 调用CDN预热接口
    boolean success = cdnClient.pushCache(file.getUrl());

    // 4. 更新CDN状态
    if (success) {
        fileMapper.updateCdnStatus(fileId, ContentFileCdnStatusEnum.PRELOADED.getValue());
    }
}
```

### 3. 冷热数据分离

```java
/**
 * 冷热数据判断逻辑
 */
public void classifyHotCold(Long fileId) {
    ContentFileDO file = getFile(fileId);

    // 热数据标准（满足任一条件）：
    // 1. 最近7天访问>100次
    // 2. 视频文件（播放数>10）
    // 3. 被推荐的内容封面
    boolean isHot = file.getAccessCount() > 100 ||
                   file.getType().startsWith("video/");

    if (isHot) {
        // 热数据：保持在MinIO高性能存储
        fileMapper.updateStorageType(fileId, FileStorageType.MINIO.getValue());
    } else if (file.getLastAccessTime() != null &&
               file.getLastAccessTime().isBefore(LocalDateTime.now().minusDays(90))) {
        // 冷数据（90天未访问）：归档到S3/Glacier
        archiveToS3(file);
    }
}
```

---

## 🚀 实施步骤

### Week 1: 数据库与基础代码

#### Day 1-2: 数据库准备
- [ ] 在`xiaolvshu_content`库创建`content_file`表
- [ ] 在`xiaolvshu_content`库创建`content_file_config`表
- [ ] 在`xiaolvshu_content`库创建`content_file_access_log`表
- [ ] 初始化MinIO配置数据
- [ ] 创建索引与分区表

#### Day 3-4: 模块代码重构
- [ ] 创建`ContentFileService`接口
- [ ] 创建`ContentFileServiceImpl`实现类
- [ ] 创建`ContentFileCreateReqDTO`请求对象
- [ ] 创建`FileStorageClient`存储客户端
- [ ] 创建`ContentFileMapper`数据库操作类

#### Day 5: 存储配置抽象
- [ ] 抽象FileStorage接口（支持MinIO/阿里云OSS/腾讯云COS）
- [ ] 实现MinIO存储客户端
- [ ] 实现阿里云OSS客户端（预留）
- [ ] 实现腾讯云COS客户端（预留）

---

### Week 2: 核心功能实现

#### Day 1-2: 上传功能
- [ ] `createFile()` - 文件上传（支持秒传）
- [ ] `getByHash()` - 根据hash查询文件
- [ ] `generatePath()` - 生成存储路径
- [ ] `calculateHash()` - 计算文件hash

#### Day 3: 查询功能
- [ ] `getFile()` - 根据ID获取文件详情
- [ ] `getUserFilePage()` - 查询用户文件列表
- [ ] `getFilesByPostId()` - 根据内容ID查询文件
- [ ] `getCdnUrl()` - 获取CDN URL

#### Day 4: 删除功能
- [ ] `deleteFile()` - 删除单个文件（逻辑删除+MQ异步物理删除）
- [ ] `batchDeleteFiles()` - 批量删除
- [ ] FileDeleteConsumer - MQ消费者处理物理删除

#### Day 5: 扩展功能
- [ ] `recordAccess()` - 记录文件访问日志
- [ ] `preloadCdn()` - CDN预热功能
- [ ] `archiveColdFiles()` - 归档冷数据定时任务

---

### Week 3: 业务集成与迁移

#### Day 1-2: 修改Controller
- [ ] `AppContentMediaController`改为调用`ContentFileService`
- [ ] `AppContentPostController`发布内容时关联文件
- [ ] 删除对infra-api的依赖

#### Day 3-4: 数据迁移
- [ ] 编写迁移脚本：`infra_file` → `content_file`
- [ ] 迁移历史数据（ID偏移+10000000）
- [ ] 补充author_id字段（关联content_post）

#### Day 5: 双写验证
- [ ] 同时写入`infra_file`和`content_file`
- [ ] 对比数据一致性
- [ ] 性能压测

---

### Week 4: 清理与优化

#### Day 1-2: 切换与观察
- [ ] 读取切换到`content_file`
- [ ] 观察1周确认无问题
- [ ] 停止写入`infra_file`

#### Day 3-4: 清理历史数据
- [ ] 删除`infra_file`中`path LIKE 'content/%'`的记录
- [ ] 优化`content_file`表索引
- [ ] 代码清理（删除双写逻辑）

#### Day 5: 监控与告警
- [ ] 监控文件上传失败率
- [ ] 监控文件查询延迟
- [ ] 设置告警规则（错误率>1%告警）

---

## 📋 测试用例

### 1. 秒传功能测试
```java
@Test
public void testSecondUpload() {
    // 第一次上传
    ContentFileDO file1 = contentFileService.createFile(req1);
    assertNotNull(file1);

    // 第二次上传相同文件
    ContentFileDO file2 = contentFileService.createFile(req2);
    assertNotNull(file2);

    // 验证返回的是同一个文件
    assertEquals(file1.getId(), file2.getId());
    assertEquals(file1.getUrl(), file2.getUrl());
}
```

### 2. 大文件上传测试
```java
@Test
public void testLargeFileUpload() {
    // 600MB文件
    byte[] content = new byte[600 * 1024 * 1024];
    req.setContent(content);

    ContentFileDO file = contentFileService.createFile(req);
    assertNotNull(file);
    assertEquals(600 * 1024 * 1024, file.getSize());
}
```

### 3. 并发上传测试
```java
@Test
public void testConcurrentUpload() throws InterruptedException {
    int threadCount = 100;
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
        new Thread(() -> {
            try {
                ContentFileDO file = contentFileService.createFile(req);
                if (file != null) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        }).start();
    }

    latch.await();
    assertEquals(threadCount, successCount.get());
}
```

---

## 📈 监控指标

### 关键指标
- 文件上传成功率 > 99.9%
- 秒传命中率 > 30%
- 文件查询平均延迟 < 5ms
- CDN命中率 > 85%

### 告警规则
- 上传失败率 > 0.1%
- 查询延迟 > 50ms（95线）
- 存储空间使用率 > 80%
- CDN流量异常（突增100%+）

---

## 🔗 依赖接口

### 依赖下游
- **infra-server**：仅查询存储配置（`infra_file_config`），不依赖文件操作
- **member-server**：查询上传者信息（用户昵称/头像，用于冗余存储）

### 被上游依赖
- **content-server**：内部调用（本模块）
- **message-server**：如果message支持文件，需要调用本模块
- **mall-server**：商品图片/视频上传

---

## 📅 实施时间线

| 周次 | 任务 | 负责人 | 预计完成 |
|------|------|--------|----------|
| Week 1 | 数据库 + 基础代码 | - | 2025-11-19 |
| Week 2 | 核心功能实现 | - | 2025-11-26 |
| Week 3 | 业务集成 + 迁移 | - | 2025-12-03 |
| Week 4 | 优化 + 监控 | - | 2025-12-10 |

**总计**：4周

---

**创建时间**：2025-11-12
**负责人**：后端负责人
**状态**：等待实施
