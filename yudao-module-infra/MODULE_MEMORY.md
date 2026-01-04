# 微服务 Memory - infra（基础设施模块）

[⬅️ 返回项目总览](../../PROJECT_MEMORY.md)

**模块关系**：独立模块，无依赖其他模块 | **被依赖于**：[member](../yudao-module-member/MODULE_MEMORY.md) • [content](../yudao-module-content/MODULE_MEMORY.md) • [message](../yudao-module-message/MODULE_MEMORY.md)

---

## 🔴 强制规则
1. **所有第三方API调用必须有缓存机制（防止QPS超限）**
2. **文件上传必须验证文件类型和大小**
3. **大文件上传必须支持断点续传**
4. **所有API请求必须记录日志（便于排查问题）**
5. **OSS文件删除必须做幂等性处理**

## 📋 模块快速理解
- **一句话描述**：基础设施服务，包括文件上传、第三方API集成（定位、天气、审核等）
- **核心职责**：OSS文件上传 + 第三方API集成 + 断点续传 + 内容审核
- **服务端口**：48082
- **数据库**：无业务表（仅记录文件和审核日志）

## 🏗️ 技术架构
### 依赖关系
```
infra → 独立，无其他模块依赖
infra 提供给 → member、content、message（文件上传、审核等）
```

### 项目结构
```
yudao-module-infra/
├── yudao-module-infra-api/       # API接口定义
│   └── cn.iocoder.yudao.module.infra.api/
│       ├── file/                 # 文件API
│       ├── location/             # 定位API
│       ├── weather/              # 天气API
│       └── moderation/           # 审核API
├── yudao-module-infra-server/    # 业务实现
│   └── cn.iocoder.yudao.module.infra/
│       ├── controller/
│       │   ├── app/              # C端APP接口
│       │   │   ├── file/         # 文件上传接口
│       │   │   ├── location/     # 定位接口
│       │   │   ├── weather/      # 天气接口
│       │   │   ├── solarterm/    # 节气接口
│       │   │   └── moderation/   # 审核接口
│       ├── service/
│       │   ├── file/             # 文件服务
│       │   │   ├── OssFileService.java       # OSS上传服务
│       │   │   └── FileStorageService.java   # 文件存储服务
│       │   ├── location/         # 定位服务
│       │   │   └── LocationService.java      # 高德地图API集成
│       │   ├── weather/          # 天气服务
│       │   │   └── WeatherService.java       # 和风天气API集成
│       │   ├── solarterm/        # 节气服务
│       │   │   └── SolarTermService.java     # 24节气API
│       │   └── moderation/       # 审核服务
│       │       └── ContentModerationService.java  # 腾讯内容安全
│       ├── dal/
│       │   ├── dataobject/       # 数据对象
│       │   │   ├── FileRecordDO.java         # 文件记录
│       │   │   └── AuditLogDO.java          # 审核日志
│       │   └── mapper/           # Mapper
│       └── job/
│           └── FileCleanupJob.java          # 文件清理定时任务
```

## 🚀 已实现功能

### ✅ OSS文件上传（100%）
- **OssFileService.java**：OSS文件上传服务（500+行）
  - 上传图片（最大10MB，自动格式校验）
  - 上传视频（最大500MB）
  - 上传大文件分片上传（>500MB，每片10MB）
  - 生成临时访问链接
  - 删除文件

- **文件上传接口**：`AppFileUploadController.java`
  ```
  POST /api/v1.0.1/infra/file/upload-image              # 上传图片
  POST /api/v1.0.1/infra/file/upload-video              # 上传视频
  POST /api/v1.0.1/infra/file/upload-file               # 上传文件
  POST /api/v1.0.1/infra/file/upload-images-batch       # 批量上传图片（最多9张）
  GET  /api/v1.0.1/infra/file/generate-url              # 生成临时访问链接
  DELETE /api/v1.0.1/infra/file/delete                  # 删除文件
  POST /api/v1.0.1/infra/file/upload-large-video        # 大文件分片上传初始化
  POST /api/v1.0.1/infra/file/upload-chunk              # 上传分片
  POST /api/v1.0.1/infra/file/complete-upload           # 完成上传
  ```

- **技术亮点**：分片上传断点续传、自动格式校验、CDN加速支持

### ✅ 定位服务（100%）
- **LocationService.java**：定位服务（集成高德地图API）
  - IP定位：根据用户IP获取地理位置
  - 逆地理编码：根据经纬度获取地址信息
  - POI搜索：搜索周边位置（餐厅、商店等）
  - Redis缓存优化，2小时TTL

- **定位接口**：`AppLocationController.java`
  ```
  GET /api/v1.0.1/location/ip-location              # 根据IP定位
  GET /api/v1.0.1/location/reverse-geocoding        # 逆地理编码
  GET /api/v1.0.1/location/poi-search               # POI搜索
  ```

### ✅ 天气服务（100%）
- **WeatherService.java**：天气服务（集成和风天气API）
  - 实时天气：获取当前天气情况
  - 7天预报：获取7天天气预报
  - 空气质量：获取空气质量信息
  - Redis缓存优化，6小时TTL

- **天气接口**：`AppWeatherController.java`
  ```
  GET /api/v1.0.1/weather/current                   # 实时天气
  GET /api/v1.0.1/weather/forecast-7days            # 7天预报
  GET /api/v1.0.1/weather/air-quality               # 空气质量
  POST /api/v1.0.1/weather/subscribe                # 订阅天气预警
  ```

### ✅ 节气服务（100%）
- **SolarTermService.java**：24节气服务（集成lunar-java库）
  - 获取今年所有节气（开始日期和时间）
  - 获取农历信息（年月日、生肖、天干地支）
  - 查询指定日期的节气

- **节气接口**：`AppSolarTermController.java`
  ```
  GET /api/v1.0.1/solar-term/all-terms               # 获取所有节气
  GET /api/v1.0.1/solar-term/lunar-info              # 获取农历信息
  GET /api/v1.0.1/solar-term/by-date                 # 查询指定日期节气
  POST /api/v1.0.1/solar-term/batch-lunar-info       # 批量获取农历信息
  GET /api/v1.0.1/solar-term/zodiac                  # 获取生肖信息
  ```

### ✅ 内容审核（100%）
- **ContentModerationService.java**：内容审核服务（集成腾讯内容安全API）
  - 文本审核（涉政/涉黄/广告/违法）
  - 图片审核（色情/暴恐/广告）
  - 视频审核（异步任务）
  - 批量文本审核

- **审核接口**：`AppModerationController.java`
  ```
  POST /api/v1.0.1/moderation/text                  # 文本审核
  POST /api/v1.0.1/moderation/image                 # 图片审核
  POST /api/v1.0.1/moderation/video                 # 视频审核
  POST /api/v1.0.1/moderation/batch-text            # 批量文本审核
  GET  /api/v1.0.1/moderation/result/:task-id       # 查询审核结果
  ```

- **审核结果枚举**：PASS(通过) / REVIEW(人工复审) / BLOCK(拦截)

### ⚠️ 待完善功能

#### 微信OAuth配置（50%）- P0优先级
**需求**：集成微信登录功能
- 微信APP认证
- OAuth 2.0流程实现
- access_token刷新机制
- 用户信息获取

**开发计划**：2人天
- Day1: 配置微信API接口和秘钥
- Day2: 实现OAuth流程和token管理

#### 断点续传完善（60%）- P1优先级
**需求**：完善大文件分片上传断点续传
- 分片校验（MD5验证）
- 秒传功能（已上传文件不重复上传）
- 断点记录（Redis缓存）
- 超时清理（24小时内无操作自动删除分片）

**开发计划**：2人天

#### 语音转文字（0%）- P2优先级
**需求**：集成语音转文字服务
- 支持多种语言
- 实时转录
- 离线缓存

**开发计划**：2人天

## 🔧 核心代码位置

### Controller（接口层）
- `AppFileUploadController.java` - 文件上传接口 - `/api/v1.0.1/infra/file/*`
- `AppLocationController.java` - 定位接口 - `/api/v1.0.1/location/*`
- `AppWeatherController.java` - 天气接口 - `/api/v1.0.1/weather/*`
- `AppSolarTermController.java` - 节气接口 - `/api/v1.0.1/solar-term/*`
- `AppModerationController.java` - 审核接口 - `/api/v1.0.1/moderation/*`

### Service（业务层）
- `OssFileService.java` - OSS文件服务
- `LocationService.java` - 定位服务
- `WeatherService.java` - 天气服务
- `SolarTermService.java` - 节气服务
- `ContentModerationService.java` - 内容审核服务

### 配置类
- `AliyunOssConfig.java` - 阿里云OSS配置
- `ThirdPartyApiConfig.java` - 第三方API配置

## ⚡ 性能优化记录
- Redis缓存API响应（2-6小时TTL，防止API超限）
- 文件上传异步处理（上传后异步生成缩略图）
- 大文件分片上传（每片10MB，支持并发上传）
- OSS访问使用CDN加速

## ⚠️ 注意事项

### 开发规范
1. **第三方API调用必须有缓存**
2. **文件上传必须校验文件类型和大小**
3. **审核结果必须记录日志（便于审查）**
4. **API密钥必须从配置中心读取（不要硬编码）**

### 常见坑点
1. 第三方API限流要妥善处理（缓存热数据）
2. 文件上传超时要设置合理的超时时间
3. 视频审核是异步任务（需要轮询结果）
4. 定位API每天有免费额度限制

### API密钥配置
所有API密钥都应该配置在Nacos：
- `aliyun.oss.access-key-id`
- `aliyun.oss.access-key-secret`
- `amap.api-key` （高德地图）
- `qweather.api-key` （和风天气）
- `tencent.content-moderation.secret-id` （腾讯内容安全）

## 📊 数据库表设计

### 核心表
1. **file_record**（文件记录表）
   - 主键：id
   - 字段：file_name, file_size, file_type, oss_path, upload_user_id, upload_time
   - 索引：idx_upload_user_id, idx_upload_time

2. **audit_log**（审核日志表）
   - 主键：id
   - 字段：content, audit_type（1文本/2图片/3视频）, audit_result, reason, create_time
   - 索引：idx_audit_type, idx_create_time

3. **upload_session**（上传会话表）- Redis存储
   - key: `upload:{file_hash}:{user_id}`
   - value: {chunks_uploaded, total_chunks, upload_time}
   - TTL: 24小时

## 🔄 更新记录
- 2025-10-09：OSS文件上传服务完成
- 2025-10-09：定位/天气/节气服务完成
- 2025-10-09：内容审核服务完成
- 2025-10-16：初始创建MODULE_MEMORY


## ⏳ 待开发功能（前端对接需要）

### 🟢 P2 - 天气信息API（0.5人天）✅ 前端需要
**需求来源**: 前端channel/channel.vue需要显示天气信息
**API**: GET /api/v1.0.1/weather/info
**实现方式**: 对接第三方天气API（高德/和风） + Redis缓存30分钟
**无需数据表**: 纯API转发服务

详细设计见 interface-new/API_STATUS.md
