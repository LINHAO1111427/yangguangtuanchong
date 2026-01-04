# 微服务 Memory - system（系统管理模块）

[⬅️ 返回项目总览](../../PROJECT_MEMORY.md)

**模块关系**：独立模块，无依赖其他模块

---

## 🔴 强制规则
1. **所有操作必须经过权限校验**
2. **管理员操作必须记录审计日志**
3. **不能删除超级管理员和平台管理员**
4. **菜单路由必须与前端导航对应**
5. **操作日志必须记录完整的操作信息**
6. **所有API接口必须记录调用次数（供超级管理员查看统计）** ✅ 新增
7. **所有后端服务必须集成完整的日志、监控、链路追踪体系** ✅ 新增

## 📋 模块快速理解
### Implementation Gaps (2025-10-21)
- Admin level matrix still missing: `AdminUserDO` lacks `admin_level` field and related mapper/table adjustments (see `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/user/AdminUserDO.java`).
- Function call analytics not implemented: no `analytics_function_call` table, AOP, or admin UI; keep this in P0 backlog.
- REST path alignment pending: controllers continue to expose `/system/user` etc.; add `/api/v1.0.1/` prefixed snake_case routes to satisfy gateway contract.
- **一句话描述**：管理后台的用户、角色、权限、菜单、操作日志等系统管理功能
- **核心职责**：RBAC权限管理 + 菜单管理 + 用户管理 + 操作审计
- **服务端口**：48081
- **数据库**：系统库（与其他模块共享主库）

## 🏗️ 技术架构
### 依赖关系
```
system → 独立，无其他模块依赖
```

### 项目结构
```
yudao-module-system/
├── yudao-module-system-api/      # API接口定义
│   └── cn.iocoder.yudao.module.system.api/
│       ├── user/                 # 用户API
│       ├── role/                 # 角色API
│       ├── permission/           # 权限API
│       └── menu/                 # 菜单API
├── yudao-module-system-server/   # 业务实现
│   └── cn.iocoder.yudao.module.system/
│       ├── controller/
│       │   ├── admin/            # 管理后台接口
│       │   │   ├── user/         # 用户管理接口
│       │   │   ├── role/         # 角色管理接口
│       │   │   ├── permission/   # 权限管理接口
│       │   │   ├── menu/         # 菜单管理接口
│       │   │   └── operatelog/   # 操作日志接口
│       ├── service/
│       │   ├── user/             # 用户服务
│       │   ├── role/             # 角色服务
│       │   ├── permission/       # 权限服务
│       │   ├── menu/             # 菜单服务
│       │   └── operatelog/       # 操作日志服务
│       ├── dal/
│       │   ├── dataobject/       # 数据对象
│       │   └── mapper/           # Mapper
│       └── job/
│           └── OperateLogCleanJob.java  # 操作日志清理定时任务
```

## 🚀 已实现功能

### ✅ 用户管理（100%）
- **AdminUserService.java**：用户服务
  - 创建用户
  - 修改用户信息
  - 删除用户（不能删除超级管理员）
  - 修改密码
  - 重置密码

- **用户管理接口**：`AdminUserController.java`
  ```
  POST   /admin/system/user/create           # 创建用户
  PUT    /admin/system/user/update           # 修改用户
  DELETE /admin/system/user/delete           # 删除用户
  GET    /admin/system/user/list             # 用户列表
  GET    /admin/system/user/get              # 获取用户详情
  POST   /admin/system/user/update-password  # 修改密码
  POST   /admin/system/user/reset-password   # 重置密码
  ```

### ✅ 角色权限管理（100%）
- **AdminRoleService.java**：角色服务
  - 创建角色
  - 修改角色
  - 删除角色
  - 分配权限
  - 角色绑定用户

- **AdminPermissionService.java**：权限服务
  - 权限列表
  - 权限树
  - 用户权限检查

- **角色权限接口**：`AdminRoleController.java`
  ```
  POST   /admin/system/role/create           # 创建角色
  PUT    /admin/system/role/update           # 修改角色
  DELETE /admin/system/role/delete           # 删除角色
  GET    /admin/system/role/list             # 角色列表
  POST   /admin/system/role/grant-permission # 分配权限
  GET    /admin/system/role/permission-tree  # 权限树
  ```

### ✅ 菜单管理（100%）
- **AdminMenuService.java**：菜单服务
  - 创建菜单
  - 修改菜单
  - 删除菜单
  - 菜单树
  - 用户菜单列表（根据权限）

- **菜单管理接口**：`AdminMenuController.java`
  ```
  POST   /admin/system/menu/create           # 创建菜单
  PUT    /admin/system/menu/update           # 修改菜单
  DELETE /admin/system/menu/delete           # 删除菜单
  GET    /admin/system/menu/list             # 菜单树
  GET    /admin/system/menu/user-menu-list   # 用户菜单列表（根据权限）
  ```

### ✅ 操作日志审计（100%）
- **AdminOperateLogService.java**：操作日志服务
  - 记录操作日志
  - 操作日志查询
  - 操作日志删除

- **操作日志接口**：`AdminOperateLogController.java`
  ```
  GET    /admin/system/operatelog/list       # 操作日志列表
  DELETE /admin/system/operatelog/delete     # 删除操作日志
  DELETE /admin/system/operatelog/clean      # 清空操作日志
  ```

### ⚠️ 待完善功能

#### 管理员等级体系（0%）- P0优先级 ⚠️ 关键修正
**需求**（来自需求分析新版.md 6章）：支持4个管理员等级体系

| 等级 | 名称 | 权限范围 | 操作权限 |
|------|------|---------|---------|
| **0级** | 俱生管理员 | 系统全部权限 | 创建/编辑其他管理员、系统配置、权限管理 |
| **2级** | 2级管理员 | 广告管理 | 发布/下架广告作品、查看广告收益 |
| **7级** | 7级管理员 | 商城资质审核 | 审核团队权限申请、商城资质审核 |
| **8级** | 8级管理员 | 运营数据 | 查看后台数据、用户管理、访问量、注册量、营收额、操作日志 |

**表设计修正**：
- `admin_user` 表中 `admin_level` 字段定义为 ENUM 或 Integer
  ```
  ADMIN_LEVEL: 0(俱生) / 2(广告管理) / 7(资质审核) / 8(运营数据)
  ```
- 权限矩阵应按等级分配，而不是按模块

**开发计划**：2人天
- 修改AdminUserService，支持4个等级的权限管理
- 修改AdminPermissionService，根据等级返回对应权限集合
- 在AdminUserController添加等级判断，防止越权操作
- 添加权限检查AOP拦截器，对所有Admin接口进行权限校验

**权限矩阵示例**：
```
俱生(0级) → [用户管理、角色管理、菜单管理、操作日志、系统配置、等级设置...]
2级(广告) → [广告管理、审计日志查看]
7级(资质) → [团队认证申请审核、商城资质管理]
8级(运营) → [用户统计、访问统计、营收报表、操作日志]
```

#### 功能调用统计（0%）- P1优先级 ✅ 新增
**需求**（来自开发规范.md）：超级管理员能看见每一项功能代码的统计，被调用了多少次

**功能说明**：
- 所有API接口自动记录调用次数
- 按天/周/月统计调用频率
- 超级管理员后台可查看各功能的调用热度
- 用于识别热点功能和冷功能
- 为优化决策提供数据支持

**实现方式**：
- 在AOP中拦截所有@RequestMapping接口
- 记录调用次数到Redis中（日、周、月计数器）
- 定期聚合到数据库（analytics_function_call表）
- 后台提供查询接口和看板

**表设计**：
- `analytics_function_call`（功能调用统计表）
  - function_id、function_name、call_count、call_date, avg_response_time
  - 索引：idx_call_date, idx_function_name

**开发计划**：2人天
- Day1: 设计统计数据模型，实现AOP拦截器
- Day2: 实现后台查询接口和看板展示

#### 平台协议管理（0%）- P2优先级
**需求**：管理平台用户协议、隐私政策等
- 协议类型（用户协议、隐私政策、免责声明）
- 协议内容管理
- 协议版本控制
- 用户协议同意记录

**表设计**：
- `system_protocol`（协议表）
  - protocol_id、type、content、version、create_time

**开发计划**：2人天

## 🔧 核心代码位置

### Controller（接口层）
- `AdminUserController.java` - 用户管理接口 - `/admin/system/user/*`
- `AdminRoleController.java` - 角色管理接口 - `/admin/system/role/*`
- `AdminMenuController.java` - 菜单管理接口 - `/admin/system/menu/*`
- `AdminOperateLogController.java` - 操作日志接口 - `/admin/system/operatelog/*`

### Service（业务层）
- `AdminUserService.java` - 用户服务
- `AdminRoleService.java` - 角色服务
- `AdminPermissionService.java` - 权限服务
- `AdminMenuService.java` - 菜单服务
- `AdminOperateLogService.java` - 操作日志服务

### DataObject（数据对象）
- `AdminUserDO.java` - 用户实体
- `AdminRoleDO.java` - 角色实体
- `AdminMenuDO.java` - 菜单实体
- `AdminOperateLogDO.java` - 操作日志实体

## ⚡ 性能优化记录
- Redis缓存角色权限树（30分钟TTL）
- Redis缓存用户菜单列表（1小时TTL）
- 操作日志定时清理（每天凌晨2点清理90天前的数据）

## ⚠️ 注意事项

### 开发规范
1. **所有管理员操作必须记录审计日志**
2. **权限检查必须在Controller层进行**
3. **不能修改超级管理员的角色权限**
4. **菜单删除必须检查子菜单**

### 常见坑点
1. 权限检查要支持多个权限的OR/AND逻辑
2. 菜单树序号要保证唯一性
3. 操作日志要记录修改前后的值（便于审计）
4. 角色权限修改要清除缓存

## 📊 数据库表设计

### 核心表
1. **admin_user**（用户表）
   - 主键：id
   - 字段：username, password_hash, real_name, email, phone, admin_level（0俱生/2广告/7资质/8运营）✅ 已修正
   - 索引：uk_username, idx_admin_level
   - 备注：只有等级0(俱生)可以创建/管理其他管理员

2. **admin_role**（角色表）✅ 重构为按等级权限
   - 主键：id
   - 字段：role_name, description, admin_level, permissions(权限集合JSON)
   - 索引：uk_role_name
   - 备注：用于快速查询该等级的所有权限

3. **admin_menu**（菜单表）
   - 主键：id
   - 字段：menu_name, parent_id, path, component, order_num, visible（0隐藏/1显示）
   - 索引：idx_parent_id

4. **admin_operatelog**（操作日志表）
   - 主键：id
   - 字段：user_id, operation, method, old_value, new_value, create_time
   - 索引：idx_user_id, idx_create_time
   - 分区：按月分区（保留3年）

## 📝 AOP审计注解

### @OperateLog 注解
```java
@OperateLog(operation = "新增用户", module = "系统管理")
public ResultVo<Void> createUser(@RequestBody AdminUserCreateRequest request) {
    // 自动记录操作日志
}
```

## ⏳ 待开发功能（前端对接需要）

### 🔴 P0 - 系统公告API（1人天）✅ 前端急需

**需求来源**: 前端`channel/channel.vue`和`channel/notice.vue`需要显示系统公告

**API设计**:
```java
@RestController
@RequestMapping("/api/v1.0.1/notice")
public class AppNoticeController {

    @GetMapping("/list")
    // 获取公告列表
    // 参数: page, limit, type(可选)
    // 返回: PageResult<NoticeVO>

    @PostMapping("/read")
    // 标记公告已读
    // 参数: noticeId
    // 返回: CommonResult<Void>

    @GetMapping("/detail/{id}")
    // 公告详情
    // 参数: id
    // 返回: CommonResult<NoticeVO>
}
```

**数据表设计**:
```sql
-- 系统公告表
CREATE TABLE system_notice (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type SMALLINT DEFAULT 1,  -- 1-普通 2-重要
    status SMALLINT DEFAULT 1, -- 1-启用 0-停用
    creator VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64),
    update_time TIMESTAMP
);

-- 公告已读记录表
CREATE TABLE system_notice_read (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(notice_id, user_id)
);
```

**文件位置**:
- Controller: `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/controller/app/AppNoticeController.java`
- Service: `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/service/notice/NoticeService.java`
- DO: `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/notice/NoticeDO.java`

---

### 🔴 P0 - Banner管理API（0.5人天）✅ 前端急需

**需求来源**: 前端`channel/channel.vue`需要显示轮播Banner

**API设计**:
```java
@RestController
@RequestMapping("/api/v1.0.1/banner")
public class AppBannerController {

    @GetMapping("/list")
    // 获取Banner列表
    // 参数: position(位置), limit
    // 返回: CommonResult<List<BannerVO>>

    @GetMapping("/detail/{id}")
    // Banner详情
    // 参数: id
    // 返回: CommonResult<BannerVO>
}
```

**数据表设计**:
```sql
-- Banner表
CREATE TABLE system_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200),
    subtitle VARCHAR(200),
    image VARCHAR(500) NOT NULL,
    link VARCHAR(500),
    position VARCHAR(50) NOT NULL, -- 位置: channel_top, home_top等
    sort INT DEFAULT 0,
    status SMALLINT DEFAULT 1, -- 1-启用 0-停用
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);
CREATE INDEX idx_position_sort ON system_banner(position, sort);
```

**文件位置**:
- Controller: `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/controller/app/AppBannerController.java`
- Service: `yudao-module-system-server/src/main/java/cn/iocoder/yudao/module/system/service/banner/BannerService.java`

---

### 🟡 P1 - 功能调用统计（2人天）
保持原有设计不变

### 🟢 P2 - 平台协议管理（2人天）
保持原有设计不变

## 🔄 更新记录
- 2025-10-16：初始创建，梳理系统管理功能
- 2025-10-16：**🔴 P0修正** - 管理员等级体系修正（俱生/2/7/8级，不是0/1/2级）
- 2025-01-30：**✅ 新增** - 添加系统公告API和Banner管理API（前端对接需要）
