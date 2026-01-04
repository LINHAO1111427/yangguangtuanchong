# 微服务 Memory - message（消息通讯模块）

[⬅️ 返回项目总览](../../PROJECT_MEMORY.md)

**依赖模块**：[member](../yudao-module-member/MODULE_MEMORY.md)（用户信息） • [content](../yudao-module-content/MODULE_MEMORY.md)（内容信息）

---

## 🔴 强制规则
1. **所有实时消息必须通过WebSocket推送（HTTP补救）**
2. **消息必须使用Kafka保障数据不丢失**
3. **消息必须持久化到PostgreSQL**
4. **未读消息必须实时更新**
5. **消息删除必须做软删除，保留审计日志**
6. **🚫 不支持实时语音/视频通话（后续对接第三方）**
7. **✅ 仅支持异步语音消息和自动转文字**

## 📋 模块快速理解
- **一句话描述**：实时消息推送系统，包括WebSocket推送、Kafka消息队列、私信、群聊、系统通知等
- **核心职责**：WebSocket实时推送 + Kafka消息队列 + 消息持久化 + 私信群聊 + 系统通知
- **服务端口**：48086
- **数据库**：message_db（消息库）

## 🏗️ 技术架构
### 依赖关系
```
message → member（用户信息查询）、content（内容信息查询）
message 提供给 → 前端实时消息推送
```

### 项目结构
```
yudao-module-message/
├── yudao-module-message-api/     # API接口定义
│   └── cn.iocoder.yudao.module.message.api/
│       ├── private/              # 私信API
│       ├── group/                # 群聊API
│       └── notify/               # 系统通知API
├── yudao-module-message-server/  # 业务实现
│   └── cn.iocoder.yudao.module.message/
│       ├── controller/
│       │   ├── app/              # C端APP接口
│       │   │   ├── private/      # 私信接口
│       │   │   ├── group/        # 群聊接口
│       │   │   └── notify/       # 通知接口
│       ├── service/
│       │   ├── websocket/        # WebSocket服务
│       │   │   ├── WebSocketConfig.java           # WebSocket配置
│       │   │   ├── WebSocketAuthInterceptor.java  # JWT鉴权拦截器
│       │   │   └── WebSocketMessageHandler.java   # 消息处理器
│       │   ├── kafka/            # Kafka服务
│       │   │   ├── KafkaConfig.java               # Kafka配置
│       │   │   ├── KafkaMessageProducer.java      # 消息生产者
│       │   │   └── KafkaMessageConsumer.java      # 消息消费者
│       │   ├── private/          # 私信服务
│       │   │   └── PrivateMessageService.java     # 私信服务
│       │   ├── group/            # 群聊服务
│       │   │   ├── GroupService.java              # 群组服务
│       │   │   └── GroupMemberService.java        # 群成员服务
│       │   ├── notify/           # 系统通知服务
│       │   │   └── NotificationService.java       # 通知服务
│       │   └── persistence/      # 持久化服务
│       │       └── MessagePersistenceService.java # 消息持久化
│       ├── dal/
│       │   ├── dataobject/       # 数据对象
│       │   │   ├── PrivateMessageDO.java          # 私信实体
│       │   │   ├── GroupDO.java                   # 群组实体
│       │   │   ├── GroupMemberDO.java             # 群成员实体
│       │   │   └── NotificationDO.java            # 通知实体
│       │   └── mapper/           # Mapper
│       └── job/
│           └── OfflineMessageCleanupJob.java      # 离线消息清理定时任务
```

## 🚀 已实现功能

### ✅ WebSocket实时推送（100%）
- **WebSocketConfig.java**：WebSocket配置类
  - STOMP协议支持
  - 消息格式规范
  - 连接管理

- **WebSocketAuthInterceptor.java**：JWT鉴权拦截器
  - Token验证
  - 用户身份识别
  - 连接权限检查

- **WebSocketMessageHandler.java**：消息处理器（支持在线管理/心跳/广播）
  - 在线用户管理
  - 心跳保活（30秒间隔）
  - 消息广播
  - 单播推送

- **WebSocket特性**：
  - 支持多种消息类型（私信、群聊、系统通知）
  - 自动心跳保活
  - 断线重连（最多5次，3秒间隔）

### ✅ Kafka消息队列（100%）
- **KafkaConfig.java**：Kafka配置类
  - 生产者配置（分区、重试策略）
  - 消费者配置（消费者组、offset管理）

- **KafkaMessageProducer.java**：消息生产者服务（4个Topic）
  - `private-message` - 私信Topic
  - `group-message` - 群聊Topic
  - `system-notify` - 系统通知Topic
  - `behavior-event` - 用户行为Topic（如点赞、评论通知）

- **KafkaMessageConsumer.java**：消息消费者服务（4个监听器）
  - 消费私信消息
  - 消费群聊消息
  - 消费系统通知
  - 消费用户行为

- **Kafka特性**：
  - 生产者ACK=all（确保消息不丢失）
  - 消费者自动提交offset
  - 消费者组保证消息顺序性

### ✅ 消息持久化（100%）
- **MessagePersistenceService.java**：消息持久化服务
  - Redis缓存最近消息（List结构，保留100条）
  - 未读消息计数（Redis Counter）
  - PostgreSQL持久化存储

- **架构优势**：WebSocket推送 + Kafka削峰 + Redis缓存 + PostgreSQL持久化

### ✅ 私信功能（100%）
- **PrivateMessageService.java**：私信服务
  - 发送私信
  - 删除私信（软删除）
  - 私信列表（会话列表）
  - 对话历史
  - 标记已读

- **私信接口**：`AppPrivateMessageController.java`
  ```
  POST /api/v1.0.1/message/private/send               # 发送私信
  GET  /api/v1.0.1/message/private/conversations      # 获取会话列表
  GET  /api/v1.0.1/message/private/history            # 获取对话历史
  DELETE /api/v1.0.1/message/private/delete           # 删除私信
  POST /api/v1.0.1/message/private/mark-read          # 标记已读
  GET  /api/v1.0.1/message/private/unread-count       # 获取未读数
  ```

### ✅ 系统通知（100%）
- **NotificationService.java**：通知服务
  - 发送系统通知（点赞、评论、关注、系统公告等）
  - 通知列表
  - 标记已读
  - 删除通知

- **通知接口**：`AppNotificationController.java`
  ```
  GET  /api/v1.0.1/message/notify/list               # 通知列表
  POST /api/v1.0.1/message/notify/mark-read          # 标记已读
  DELETE /api/v1.0.1/message/notify/delete           # 删除通知
  GET  /api/v1.0.1/message/notify/unread-count       # 未读通知数
  ```

### ✅ 群聊功能（100%）
- **GroupService.java + GroupServiceImpl.java**：群组服务完整实现
  - 创建群组（支持初始成员邀请）
  - 解散群组（仅群主）
  - 添加/移除群成员（权限校验）
  - 退出群组（群主需先转让）
  - 发送群消息（包含Kafka集成）
  - 撤回群消息（2分钟内）
  - 更新群信息（群名、头像、公告、简介）
  - 设置成员角色（群主/管理员/普通成员）
  - 禁言/取消禁言成员
  - 设置全员禁言
  - 转让群主

- **群聊接口**：`AppGroupController.java`
  ```
  POST   /app-api/message/group/create                # 创建群组
  DELETE /app-api/message/group/dissolve/{groupId}    # 解散群组
  GET    /app-api/message/group/info/{groupId}        # 获取群信息
  PUT    /app-api/message/group/update                # 更新群信息
  GET    /app-api/message/group/my-groups             # 获取我的群组列表

  POST   /app-api/message/group/member/add            # 添加群成员
  DELETE /app-api/message/group/member/remove         # 移除群成员
  POST   /app-api/message/group/member/quit           # 退出群组
  GET    /app-api/message/group/member/list           # 获取群成员列表
  POST   /app-api/message/group/member/set-role       # 设置成员角色
  POST   /app-api/message/group/member/mute           # 禁言成员
  POST   /app-api/message/group/member/unmute         # 取消禁言
  POST   /app-api/message/group/mute-all              # 设置全员禁言
  POST   /app-api/message/group/transfer-owner        # 转让群主

  POST   /app-api/message/group/message/send          # 发送群消息
  POST   /app-api/message/group/message/recall        # 撤回群消息
  GET    /app-api/message/group/message/list          # 获取群聊天记录
  ```

- **数据对象**：
  - `GroupInfoDO.java` - 群组信息实体
  - `GroupMemberDO.java` - 群成员实体
  - `GroupMessageDO.java` - 群消息实体

- **Mapper层**：
  - `GroupInfoMapper.java` - 群组信息Mapper（包含成员计数、解散群组等方法）
  - `GroupMemberMapper.java` - 群成员Mapper（包含角色管理、禁言管理等方法）
  - `GroupMessageMapper.java` - 群消息Mapper（包含消息查询、撤回等方法）

- **VO类**：
  - `GroupCreateReqVO.java` - 创建群组请求
  - `GroupUpdateReqVO.java` - 更新群组请求
  - `GroupAddMemberReqVO.java` - 添加成员请求
  - `GroupMuteMemberReqVO.java` - 禁言成员请求
  - `GroupMessageSendReqVO.java` - 发送群消息请求

- **错误码**：已在 `ErrorCodeConstants.java` 中定义完整的群聊错误码（14-001-000 ~ 14-002-099）

- **数据库表**：已在 `xiaolvshu-message.sql` 中定义
  - `group_info` - 群组信息表
  - `group_member` - 群成员表（包含角色、禁言等字段）
  - `group_message` - 群消息表（按年分区）

- **核心特性**：
  - 完整的权限控制（群主/管理员/普通成员）
  - 禁言管理（单人禁言+全员禁言）
  - 消息撤回（2分钟内）
  - Kafka异步推送
  - 软删除（审计保留）

### ⚠️ 待开发功能

#### 群聊功能优化（0%）- P1优先级
**需求**：群聊高级功能
- 群聊@提醒功能（@全体成员、@指定成员）
- 群公告推送（发布公告时推送给全体成员）
- 加群验证流程（群主/管理员审核）
- 群聊消息已读回执（显示已读/未读成员列表）
- 群聊搜索（搜索群名、群公告）
- 群二维码（扫码加群）

**开发计划**：3人天
- Day1: @功能和已读回执
- Day2: 加群验证流程
- Day3: 群聊搜索和二维码

#### 文件断点续传（40%）- P1优先级
**需求**：消息中支持文件上传
- 文件分片上传
- 断点续传
- 文件预览

**开发计划**：2人天

#### 消息撤回（0%）- P2优先级
**需求**：支持消息撤回
- 撤回时间限制（发送后2分钟内可撤回）
- 撤回记录（显示"对方撤回了一条消息"）
- 撤回通知

**开发计划**：1人天

#### 语音消息与转文字（0%）- P1优先级
**需求**：支持语音消息和语音转文字功能
- 语音录制上传（通过消息系统发送）
- 语音播放
- 语音转文字（自动转写为文本）
- 支持显示文字稿和音频并行展示

**说明**：
- 🚫 不支持语音/视频实时通话（后续对接第三方服务）
- ✅ 仅支持异步语音消息（录音文件发送）
- ✅ 支持一键转文字（Aliyun/Tencent ASR服务）
- ✅ 可在设置中开启自动转文字

**表设计**：
- `message_voice` 表（扩展字段）
  - voice_url、duration、transcription_text、transcription_status（0待转/1转中/2已完成）
  - 使用 webhook 异步更新转文字结果

**API接口**：
```
POST   /api/v1.0.1/message/private/send-voice    # 上传语音消息
GET    /api/v1.0.1/message/voice/transcription   # 获取转文字结果
PUT    /api/v1.0.1/message/voice/auto-transcribe # 切换自动转文字开关
```

**第三方服务集成**：
- Aliyun Speech Recognition (阿里云语音识别)
- Tencent Speech Recognition (腾讯云语音识别)
- 支持中/英文识别，可配置置信度阈值

**开发计划**：2人天
- Day1: 语音上传、存储、播放功能
- Day2: ASR集成、转文字实现、webhook处理

## 🔧 核心代码位置

### Controller（接口层）
- `AppPrivateMessageController.java` - 私信接口 - `/api/v1.0.1/message/private/*`
- `AppGroupController.java` - 群聊接口 - `/api/v1.0.1/message/group/*`
- `AppNotificationController.java` - 通知接口 - `/api/v1.0.1/message/notify/*`
- `WebSocketController.java` - WebSocket接口 - `/ws`

### Service（业务层）
- `WebSocketMessageHandler.java` - WebSocket消息处理
- `KafkaMessageProducer.java` - Kafka消息生产者
- `KafkaMessageConsumer.java` - Kafka消息消费者
- `MessagePersistenceService.java` - 消息持久化
- `PrivateMessageService.java` - 私信服务
- `GroupService.java` - 群组服务
- `NotificationService.java` - 通知服务

### DataObject（数据对象）
- `PrivateMessageDO.java` - 私信实体
- `GroupDO.java` - 群组实体
- `GroupMemberDO.java` - 群成员实体
- `NotificationDO.java` - 通知实体

## ⚡ 性能优化记录
- Redis缓存未读消息数（Counter结构）
- Redis缓存最近消息（List结构，100条）
- Kafka用于削峰（消费者可以慢速消费）
- 私信查询使用索引优化（idx_sender_receiver_create_time）

## ⚠️ 注意事项

### 开发规范
1. **所有消息必须通过Kafka（确保不丢失）**
2. **消息删除必须做软删除（保留审计日志）**
3. **未读消息计数必须准确（不能重复计算）**
4. **WebSocket断线重连要有指数退避策略**

### 常见坑点
1. Kafka消费者要处理重复消费（幂等性）
2. 私信查询要处理大数据量（分页、缓存）
3. 群聊消息要保证顺序性（使用同一partition）
4. 通知类型要区分用户通知和系统通知

### WebSocket URL
- 建立连接：`ws://localhost:48080/ws?token={jwt_token}`
- STOMP端点：`/app/message`
- 订阅主题：`/user/queue/private`, `/queue/system`, `/topic/broadcast`

## 📊 数据库表设计

### 核心表
1. **private_message**（私信表）
   - 主键：id
   - 字段：sender_id, receiver_id, content, status（1未读/2已读/3已删除）, create_time
   - 唯一索引：uk_sender_receiver_create_time
   - 索引：idx_receiver_id, idx_create_time
   - 分区：按月分区（保留3年）

2. **message_group**（群组表）
   - 主键：id
   - 字段：group_name, group_avatar, creator_id, member_count, create_time
   - 索引：idx_creator_id

3. **message_group_member**（群成员表）
   - 主键：id
   - 字段：group_id, user_id, join_time
   - 唯一索引：uk_group_user（group_id + user_id）

4. **notification**（系统通知表）
   - 主键：id
   - 字段：user_id, notify_type（1点赞/2评论/3关注/4系统）, source_user_id, source_content_id, status（1未读/2已读）, create_time
   - 索引：idx_user_id, idx_notify_type, idx_create_time
   - 分区：按月分区（保留2年）

## 🔄 更新记录
- 2025-10-09：WebSocket和Kafka系统完成
- 2025-10-10：前端聊天功能集成完成
- 2025-10-16：初始创建MODULE_MEMORY
- 2025-10-16：✅ P1修正 - 添加语音转文字功能（2人天），排除实时语音/视频通话
- 2025-10-16：✅ 群聊功能完成 - 实现完整的群聊系统（创建群组、成员管理、消息发送、权限控制、禁言管理等）
