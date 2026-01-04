# Content 模块优化方案

## 🔴 一、表映射冲突解决方案

### 问题分析

**严重问题**：两个 DO 类映射同一张数据库表 `content_post`

```java
// ContentDO.java
@TableName(value = "content_post", autoResultMap = true)
public class ContentDO extends BaseDO {
    // 50+ 字段（完整）
}

// ContentPostDO.java
@TableName(value = "content_post", autoResultMap = true)
public class ContentPostDO extends BaseDO {
    // 仅 5 个字段：id, author_id, title, cover_image, boost_level, boost_expire_at
}
```

**当前使用情况**：
- ✅ **ContentDO** - 被大量使用
  - `ContentMapper.java` - 主 Mapper
  - `ContentServiceImpl.java` - 所有业务逻辑
  - `ContentApiImpl.java` - API 接口实现

- ❌ **ContentPostDO** - 基本没用
  - `ContentPostMapper.java` - 空 Mapper（无任何方法）
  - **无任何业务代码引用**

### ✅ 解决方案

**立即删除 `ContentPostDO` 和 `ContentPostMapper`！**

```bash
# 删除文件
rm yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/dal/dataobject/ContentPostDO.java
rm yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/dal/mysql/ContentPostMapper.java
```

**理由**：
1. `ContentPostDO` 仅 5 个字段，严重不完整
2. 没有任何业务代码使用它
3. 与 `ContentDO` 冲突，容易造成数据不一致
4. `ContentDO` 字段完整（50+），满足所有业务需求

**风险评估**：✅ **零风险**（无代码引用）

---

## 📊 二、分表策略评估

### 2.1 当前表规模预估

#### content_post 表数据量预测

| 时间阶段 | 用户数 | 日均发帖 | 累计帖子数 | 数据库大小（估算） |
|---------|--------|---------|-----------|------------------|
| 第 1 个月 | 1,000 | 2,000 | 60,000 | ~30MB |
| 第 3 个月 | 10,000 | 20,000 | 1,800,000 | ~900MB |
| 第 6 个月 | 50,000 | 100,000 | 18,000,000 | ~9GB |
| 第 1 年 | 100,000 | 200,000 | 73,000,000 | ~36GB |
| 第 2 年 | 500,000 | 1,000,000 | 438,000,000 | ~219GB |

**字段大小估算**：
```
基础字段: ~200 bytes
content 字段: ~500 bytes (平均)
images JSON: ~200 bytes (3张图)
video字段: ~150 bytes
extra JSON: ~100 bytes
─────────────────────
单条记录: ~1,150 bytes ≈ 1.1KB
```

### 2.2 分表触发阈值

MySQL InnoDB 推荐：
- ✅ **单表 < 2000万行** - 无需分表
- ⚠️ **2000万 ~ 5000万行** - 建议分表
- 🔴 **> 5000万行** - 必须分表

**小绿书预测**：
- **第 6 个月**：1800万行 → ✅ 暂不分表
- **第 1 年**：7300万行 → 🔴 **必须分表**

### 2.3 分表策略设计

#### 方案一：按时间分表（推荐）⭐

**分表规则**：`content_post_YYYYMM`

```sql
content_post_202501  -- 2025年1月
content_post_202502  -- 2025年2月
content_post_202503  -- 2025年3月
...
```

**优点**：
- ✅ 符合内容平台特点（时间序列）
- ✅ 历史数据可归档/冷存储
- ✅ 查询路由简单（根据 create_time / publish_time）
- ✅ 易于删除过期数据
- ✅ 热点数据集中（近期内容）

**缺点**：
- ❌ 跨月份查询需要union
- ❌ 按用户查询需要多表扫描（可通过索引优化）

**实现方案**：使用 **ShardingSphere** 自动分表

```yaml
# application-sharding.yaml
spring:
  shardingsphere:
    rules:
      sharding:
        tables:
          content_post:
            actual-data-nodes: ds0.content_post_$->{202501..202612}
            table-strategy:
              standard:
                sharding-column: publish_time
                sharding-algorithm-name: content_post_sharding_algorithm
        sharding-algorithms:
          content_post_sharding_algorithm:
            type: INTERVAL
            props:
              datetime-pattern: yyyy-MM-dd HH:mm:ss
              datetime-interval-amount: 1
              datetime-interval-unit: MONTHS
```

#### 方案二：按用户 Hash 分表

**分表规则**：`content_post_0` ~ `content_post_31`（32张表）

```sql
content_post_0   -- user_id % 32 = 0
content_post_1   -- user_id % 32 = 1
...
content_post_31  -- user_id % 32 = 31
```

**优点**：
- ✅ 按用户查询高效（单表）
- ✅ 数据分布均匀

**缺点**：
- ❌ 全局查询（首页Feed）需要union 32张表
- ❌ 按时间排序复杂
- ❌ 不符合内容平台查询特征

**结论**：❌ **不推荐**

#### 方案三：混合策略（高级）

**分表规则**：`content_post_YYYYMM_N`

```sql
content_post_202501_0  -- 2025年1月, hash=0
content_post_202501_1  -- 2025年1月, hash=1
content_post_202501_2  -- 2025年1月, hash=2
content_post_202501_3  -- 2025年1月, hash=3
```

**适用场景**：月数据量 > 1000万

### 2.4 分表实施时间线

| 阶段 | 数据量 | 操作 |
|------|-------|------|
| **当前** | < 100万 | 无需分表，优化索引 |
| **3个月内** | < 200万 | 监控性能，准备分表方案 |
| **6个月内** | < 2000万 | 引入 ShardingSphere，测试环境验证 |
| **1年内** | > 2000万 | **正式分表**，按月分表 |

### 2.5 ShardingSphere 集成方案

#### 依赖引入

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>5.4.0</version>
</dependency>
```

#### 配置示例

```yaml
spring:
  shardingsphere:
    datasource:
      names: ds0
      ds0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://localhost:3306/xiaolvshu
        username: root
        password: root

    rules:
      sharding:
        tables:
          # content_post 按月分表
          content_post:
            actual-data-nodes: ds0.content_post_$->{202501..202612}
            table-strategy:
              standard:
                sharding-column: publish_time
                sharding-algorithm-name: content_post_month
            key-generate-strategy:
              column: id
              key-generator-name: snowflake

          # content_comment 按月分表
          content_comment:
            actual-data-nodes: ds0.content_comment_$->{202501..202612}
            table-strategy:
              standard:
                sharding-column: create_time
                sharding-algorithm-name: comment_month

          # content_interaction 按月分表（数据量最大）
          content_interaction:
            actual-data-nodes: ds0.content_interaction_$->{202501..202612}
            table-strategy:
              standard:
                sharding-column: create_time
                sharding-algorithm-name: interaction_month

        sharding-algorithms:
          content_post_month:
            type: INTERVAL
            props:
              datetime-pattern: yyyy-MM-dd HH:mm:ss
              datetime-interval-amount: 1
              datetime-interval-unit: MONTHS
              datetime-lower: 2025-01-01 00:00:00

          comment_month:
            type: INTERVAL
            props:
              datetime-pattern: yyyy-MM-dd HH:mm:ss
              datetime-interval-amount: 1
              datetime-interval-unit: MONTHS
              datetime-lower: 2025-01-01 00:00:00

          interaction_month:
            type: INTERVAL
            props:
              datetime-pattern: yyyy-MM-dd HH:mm:ss
              datetime-interval-amount: 1
              datetime-interval-unit: MONTHS
              datetime-lower: 2025-01-01 00:00:00

        key-generators:
          snowflake:
            type: SNOWFLAKE
            props:
              worker-id: 1

    props:
      sql-show: true  # 开发环境显示SQL
```

### 2.6 分表后的查询优化

#### 带分片键查询（高效）

```java
// ✅ 单表查询 - 自动路由到 content_post_202501
contentMapper.selectList(new LambdaQueryWrapper<ContentDO>()
    .eq(ContentDO::getUserId, userId)
    .between(ContentDO::getPublishTime,
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2025, 1, 31, 23, 59))
);
```

#### 不带分片键查询（需优化）

```java
// ⚠️ 全表扫描 - 会查询所有分表
contentMapper.selectList(new LambdaQueryWrapper<ContentDO>()
    .eq(ContentDO::getUserId, userId)
    // 没有 publish_time 条件，会扫描所有分表
);

// ✅ 优化方案：增加时间范围
LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
contentMapper.selectList(new LambdaQueryWrapper<ContentDO>()
    .eq(ContentDO::getUserId, userId)
    .ge(ContentDO::getPublishTime, oneMonthAgo)  // 添加时间过滤
);
```

### 2.7 分表总结

| 评估项 | 结论 |
|--------|------|
| **当前是否需要分表** | ❌ **不需要** - 数据量 < 100万 |
| **何时需要分表** | 🟡 **1年内** - 预计数据量 > 2000万 |
| **推荐方案** | ⭐ **按月分表** - 符合内容平台特性 |
| **技术选型** | ShardingSphere 5.x |
| **分表对象** | `content_post`, `content_comment`, `content_interaction` |
| **对现有代码影响** | ✅ **零改动** - ShardingSphere 透明代理 |

---

## 🔒 三、安全策略分析

### 3.1 芋道框架已有安全机制

#### ✅ 1. 认证授权（Spring Security）

**配置位置**：`YudaoWebSecurityConfigurerAdapter.java`

```java
核心功能：
- Token 认证（Header: Authorization）
- 基于注解的权限控制（@PreAuthorize, @PermitAll）
- Session 禁用（Stateless）
- CSRF 禁用（Token 机制）
```

**使用示例**：
```java
@PreAuthorize("isAuthenticated()")  // 需要登录
public void createContent() {}

@PreAuthorize("hasRole('ADMIN')")  // 需要管理员角色
public void auditContent() {}

@PermitAll  // 公开访问
public void getHotContents() {}
```

#### ✅ 2. XSS 防护

**配置位置**：`YudaoXssAutoConfiguration.java`

```java
核心功能：
- 自动过滤 HTML 标签
- 使用 Jsoup 清理恶意脚本
- 支持 JSON 反序列化自动清理
```

**配置**：
```yaml
yudao:
  xss:
    enable: true  # 默认开启
    exclude-urls:  # 排除URL（如富文本编辑器）
      - /api/v1.0.1/content/*/rich-text
```

#### ✅ 3. 接口限流（Rate Limiter）

**配置位置**：`RateLimiter` 注解

```java
核心功能：
- 基于 Redis 的分布式限流
- 支持多种限流维度（全局/用户/IP/自定义）
```

**使用示例**：
```java
// 全局限流：1秒100次
@RateLimiter(time = 1, count = 100, keyResolver = DefaultRateLimiterKeyResolver.class)
public void getContentList() {}

// 用户限流：1秒10次
@RateLimiter(time = 1, count = 10, keyResolver = UserRateLimiterKeyResolver.class)
public void createContent() {}

// IP限流：1秒5次（防刷）
@RateLimiter(time = 1, count = 5, keyResolver = ClientIpRateLimiterKeyResolver.class)
public void likeContent() {}
```

#### ✅ 4. 数据权限

**MyBatis-Plus 支持**：
- 逻辑删除（`deleted` 字段）
- 租户隔离（可选）

#### ✅ 5. API 日志

**配置位置**：`ApiAccessLogFilter.java`

```java
核心功能：
- 记录所有 API 访问
- IP、User-Agent、请求参数、响应结果
```

### 3.2 Content 模块需要增强的安全点

#### 🔴 高优先级

##### 1. 内容审核机制 ⭐

**当前状态**：
```java
// 已有字段，但审核逻辑未完善
content.setAuditStatus(ContentDO.AuditStatusEnum.PENDING.getStatus());
```

**需要增强**：
```java
// 1. 敏感词过滤
@Service
public class ContentAuditService {

    @Resource
    private SensitiveWordFilter sensitiveWordFilter;

    public void autoAudit(ContentDO content) {
        // 检测标题
        if (sensitiveWordFilter.contains(content.getTitle())) {
            content.setAuditStatus(AuditStatusEnum.REJECTED.getStatus());
            content.setAuditRemark("标题包含敏感词");
            return;
        }

        // 检测内容
        if (sensitiveWordFilter.contains(content.getContent())) {
            content.setAuditStatus(AuditStatusEnum.REJECTED.getStatus());
            content.setAuditRemark("内容包含敏感词");
            return;
        }

        // 通过
        content.setAuditStatus(AuditStatusEnum.APPROVED.getStatus());
    }
}

// 2. 图片鉴黄（调用第三方服务）
public void auditImages(List<String> imageUrls) {
    for (String url : imageUrls) {
        // 调用阿里云/腾讯云图片审核API
        ImageAuditResult result = imageAuditApi.check(url);
        if (result.isPorn()) {
            throw exception(ErrorCodeConstants.IMAGE_AUDIT_FAILED);
        }
    }
}
```

##### 2. 防刷机制 ⭐

**已有**：`@RateLimiter` 注解

**需要增强**：
```java
@Service
public class AntiSpamService {

    // 1. 发帖频率限制
    @RateLimiter(time = 60, count = 5, keyResolver = UserRateLimiterKeyResolver.class)
    public void createContent(ContentCreateReqVO reqVO) {
        // 业务逻辑
    }

    // 2. 点赞防刷（1秒最多点赞1次）
    @RateLimiter(time = 1, count = 1, keyResolver = UserRateLimiterKeyResolver.class)
    public void likeContent(Long contentId, Long userId) {
        // 检查是否重复点赞
        if (isAlreadyLiked(contentId, userId)) {
            throw exception(ErrorCodeConstants.ALREADY_LIKED);
        }
        // 业务逻辑
    }

    // 3. 评论防刷（1分钟最多10条）
    @RateLimiter(time = 60, count = 10, keyResolver = UserRateLimiterKeyResolver.class)
    public void createComment(CommentCreateReqVO reqVO) {
        // 检查是否重复评论
        if (isDuplicateComment(reqVO.getContent(), reqVO.getUserId())) {
            throw exception(ErrorCodeConstants.DUPLICATE_COMMENT);
        }
        // 业务逻辑
    }

    // 4. IP 黑名单
    private final Set<String> ipBlacklist = new ConcurrentHashSet<>();

    public void checkIpBlacklist(String ipAddress) {
        if (ipBlacklist.contains(ipAddress)) {
            throw exception(ErrorCodeConstants.IP_BLOCKED);
        }
    }
}
```

##### 3. 输入验证增强

**当前**：基础验证

**增强**：
```java
@Service
public class ContentValidator {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_IMAGES = 9;

    public void validate(ContentCreateReqVO reqVO) {
        // 1. 标题验证
        if (StrUtil.isBlank(reqVO.getTitle())) {
            throw exception(ErrorCodeConstants.TITLE_EMPTY);
        }
        if (reqVO.getTitle().length() > MAX_TITLE_LENGTH) {
            throw exception(ErrorCodeConstants.TITLE_TOO_LONG);
        }

        // 2. 内容验证
        if (StrUtil.isBlank(reqVO.getContent())) {
            throw exception(ErrorCodeConstants.CONTENT_EMPTY);
        }
        if (reqVO.getContent().length() > MAX_CONTENT_LENGTH) {
            throw exception(ErrorCodeConstants.CONTENT_TOO_LONG);
        }

        // 3. 图片验证
        if (reqVO.getImages() != null && reqVO.getImages().size() > MAX_IMAGES) {
            throw exception(ErrorCodeConstants.IMAGES_TOO_MANY);
        }

        // 4. URL 验证
        if (reqVO.getVideoUrl() != null && !isValidUrl(reqVO.getVideoUrl())) {
            throw exception(ErrorCodeConstants.INVALID_VIDEO_URL);
        }

        // 5. XSS 过滤（芋道框架已自动处理，但可二次验证）
        reqVO.setContent(xssCleaner.clean(reqVO.getContent()));
    }
}
```

#### 🟡 中优先级

##### 4. 数据脱敏

```java
@Service
public class DataMaskService {

    // 脱敏手机号
    public String maskPhone(String phone) {
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    // 脱敏邮箱
    public String maskEmail(String email) {
        return email.replaceAll("(\\w{2})\\w+(\\w@.*)", "$1***$2");
    }

    // 脱敏IP
    public String maskIp(String ip) {
        return ip.replaceAll("(\\d+\\.\\d+\\.)\\d+\\.\\d+", "$1*.*");
    }
}

// VO 层使用
public class ContentDetailRespVO {
    private String ipAddress;  // 存储时是完整IP

    @JsonSerialize(using = IpMaskSerializer.class)  // 返回时脱敏
    public String getIpAddress() {
        return ipAddress;
    }
}
```

##### 5. 日志审计

```java
@Aspect
@Component
public class ContentAuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        // 记录操作日志
        AuditLogDO log = new AuditLogDO();
        log.setModule("content");
        log.setOperation(auditLog.value());
        log.setUserId(getCurrentUserId());
        log.setIpAddress(getRequestIp());
        log.setCreateTime(LocalDateTime.now());

        try {
            Object result = point.proceed();
            log.setStatus("success");
            return result;
        } catch (Exception e) {
            log.setStatus("failure");
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            auditLogService.save(log);
        }
    }
}

// 使用
@AuditLog("删除内容")
public void deleteContent(Long id) {
    // 删除逻辑
}
```

#### 🟢 低优先级

##### 6. HTTPS 强制

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your_password
    key-store-type: PKCS12
```

##### 7. 图片水印

```java
@Service
public class WatermarkService {

    public String addWatermark(String imageUrl) {
        // 下载原图
        byte[] image = downloadImage(imageUrl);

        // 添加水印
        byte[] watermarked = ImageUtil.addWatermark(image, "xiaolvshu.com");

        // 上传到MinIO
        return fileApi.createFile(watermarked, "watermark/" + UUID.randomUUID(), "image/jpeg");
    }
}
```

### 3.3 安全增强方案总结

| 安全点 | 芋道已有 | 需要增强 | 优先级 |
|-------|----------|---------|--------|
| **认证授权** | ✅ Spring Security | - | - |
| **XSS 防护** | ✅ Jsoup 过滤 | - | - |
| **CSRF 防护** | ✅ Token 机制 | - | - |
| **接口限流** | ✅ RateLimiter 注解 | 业务场景细化 | 🔴 高 |
| **内容审核** | ⚠️ 字段存在，逻辑缺失 | 敏感词+图片鉴黄 | 🔴 高 |
| **防刷机制** | ⚠️ 部分支持 | IP黑名单+行为检测 | 🔴 高 |
| **输入验证** | ⚠️ 基础验证 | 长度+格式+XSS二次 | 🔴 高 |
| **数据脱敏** | ❌ 无 | IP/手机号脱敏 | 🟡 中 |
| **日志审计** | ✅ API日志 | 业务操作审计 | 🟡 中 |
| **SQL注入** | ✅ MyBatis-Plus | - | - |
| **敏感信息加密** | ❌ 无 | 手机号/身份证加密 | 🟢 低 |

---

## 🎯 四、实施优先级与时间线

### 阶段一：紧急修复（1天）

- [x] **删除 ContentPostDO 和 ContentPostMapper** - 零风险

### 阶段二：安全增强（1周）

- [ ] 实现敏感词过滤
- [ ] 增加接口限流（发帖/点赞/评论）
- [ ] 完善输入验证
- [ ] IP 黑名单机制

### 阶段三：数据库优化（2周）

- [ ] 添加必要索引
- [ ] 优化查询语句
- [ ] 监控慢查询

### 阶段四：分表准备（3-6个月）

- [ ] 集成 ShardingSphere
- [ ] 测试环境验证
- [ ] 编写数据迁移脚本
- [ ] 正式分表（数据量 > 2000万时）

---

## 📝 五、具体实施代码

### 5.1 删除冗余类（立即执行）

```bash
# 删除文件
rm -f yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/dal/dataobject/ContentPostDO.java
rm -f yudao-module-content-server/src/main/java/cn/iocoder/yudao/module/content/dal/mysql/ContentPostMapper.java

# 清理 MODULE_MEMORY.md 中的引用
# 手动编辑，删除 ContentPostDO 相关描述
```

### 5.2 添加数据库索引（必须）

```sql
-- content_post 表索引
ALTER TABLE content_post ADD INDEX idx_author_status_publish (author_id, status, publish_time DESC);
ALTER TABLE content_post ADD INDEX idx_topic_status (publish_topic_id, status, publish_time DESC);
ALTER TABLE content_post ADD INDEX idx_hot_score (status, hot_score DESC, publish_time DESC);
ALTER TABLE content_post ADD INDEX idx_publish_time (publish_time DESC);
ALTER TABLE content_post ADD INDEX idx_audit_status (audit_status, create_time DESC);

-- content_comment 表索引
ALTER TABLE content_comment ADD INDEX idx_content_status (content_id, status, create_time DESC);
ALTER TABLE content_comment ADD INDEX idx_root_parent (root_id, parent_id, create_time DESC);
ALTER TABLE content_comment ADD INDEX idx_user (user_id, create_time DESC);

-- content_interaction 表索引（唯一索引防止重复操作）
ALTER TABLE content_interaction ADD UNIQUE INDEX uk_content_user_type (content_id, user_id, interaction_type);
ALTER TABLE content_interaction ADD INDEX idx_user_type (user_id, interaction_type, create_time DESC);
```

### 5.3 敏感词过滤实现

```java
package cn.iocoder.yudao.module.content.service.support;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词过滤器（基于 DFA 算法）
 */
@Component
public class SensitiveWordFilter {

    private final Set<String> sensitiveWords = new HashSet<>();
    private final SensitiveWordNode root = new SensitiveWordNode();

    public SensitiveWordFilter() {
        // 初始化敏感词库（实际应从数据库或配置文件加载）
        initSensitiveWords();
        buildDFATree();
    }

    private void initSensitiveWords() {
        sensitiveWords.add("色情");
        sensitiveWords.add("赌博");
        sensitiveWords.add("毒品");
        sensitiveWords.add("暴力");
        // ... 更多敏感词
    }

    private void buildDFATree() {
        for (String word : sensitiveWords) {
            SensitiveWordNode node = root;
            for (char c : word.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new SensitiveWordNode());
            }
            node.isEnd = true;
        }
    }

    public boolean contains(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = checkWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }

    public String filter(String text, char replacement) {
        if (text == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < text.length(); i++) {
            int length = checkWord(text, i);
            if (length > 0) {
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, replacement);
                }
                i += length - 1;
            }
        }
        return result.toString();
    }

    private int checkWord(String text, int start) {
        SensitiveWordNode node = root;
        int length = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            node = node.children.get(c);
            if (node == null) {
                return 0;
            }
            length++;
            if (node.isEnd) {
                return length;
            }
        }
        return 0;
    }

    private static class SensitiveWordNode {
        private final Map<Character, SensitiveWordNode> children = new HashMap<>();
        private boolean isEnd = false;
    }
}
```

### 5.4 Controller 层限流示例

```java
@RestController
@RequestMapping("/api/v1.0.1/content")
public class AppContentController {

    // 发帖限流：1分钟最多5次
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(time = 60, count = 5,
                 keyResolver = UserRateLimiterKeyResolver.class,
                 message = "发帖太频繁，请稍后再试")
    public CommonResult<Long> createContent(@RequestBody @Valid ContentCreateReqVO reqVO) {
        // 业务逻辑
    }

    // 点赞限流：1秒最多1次
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(time = 1, count = 1,
                 keyResolver = UserRateLimiterKeyResolver.class,
                 message = "操作太频繁")
    public CommonResult<Boolean> likeContent(@PathVariable Long id) {
        // 业务逻辑
    }

    // 评论限流：1分钟最多10次
    @PostMapping("/{id}/comment")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(time = 60, count = 10,
                 keyResolver = UserRateLimiterKeyResolver.class,
                 message = "评论太频繁")
    public CommonResult<Long> createComment(@PathVariable Long id,
                                            @RequestBody @Valid CommentCreateReqVO reqVO) {
        // 业务逻辑
    }
}
```

---

## ✅ 总结

### 立即执行（零风险）

1. ✅ **删除 ContentPostDO 和 ContentPostMapper**
2. ✅ **添加数据库索引**

### 短期（1周内）

3. 🔴 **实现敏感词过滤**
4. 🔴 **增加接口限流注解**
5. 🔴 **完善输入验证**

### 中期（1个月内）

6. 🟡 **数据脱敏**
7. 🟡 **日志审计**
8. 🟡 **性能监控**

### 长期（6个月-1年）

9. 🟢 **ShardingSphere 集成**
10. 🟢 **正式分表（数据量 > 2000万）**

**芋道框架安全机制已经非常完善，Content 模块只需要在业务层面增强即可！**
