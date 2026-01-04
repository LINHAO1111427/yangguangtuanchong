# 阳光团宠微服务部署与启动指�?
> **🔥 重要提示�?025-10-13架构升级�?*�?>
> 1. **本项目已完全微服务化** - 每个模块独立启动，独立部�?> 2. **🚫 yudao-server已废�?* - 不再聚合业务模块，已改为空壳项目
> 3. **�?必须独立启动各微服务** - 通过网关访问，不能单体启�?> 4. **📊 8个独立服�?* - gateway + 7个业务服�?>
> **如果你还在使用yudao-server启动，请立即停止！这是错误的架构�?*

---

## 📋 目录

1. [环境准备](#环境准备)
2. [Nacos注册中心启动](#nacos注册中心启动)
3. [项目编译](#项目编译)
4. [微服务启动](#微服务启�?
5. [验证服务状态](#验证服务状�?
6. [常见问题排查](#常见问题排查)
7. [端口分配表](#端口分配�?

---

## 环境准备

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 必须，Spring Boot 3要求 |
| Maven | 3.8+ | 必须，项目构�?|
| Nacos | 2.2.0+ | 必须，注册中�?配置中心 |
| PostgreSQL | 14+ | 必须，业务数据库 |
| Redis | 7+ | 必须，缓�?消息队列 |
| Kafka | 3.0+ | 必须，消息队�?|
| Elasticsearch | 8.0+ | 可选，全文搜索 |

### 环境变量配置

```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:MAVEN_HOME = "C:\Program Files\Maven\apache-maven-3.8.6"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export MAVEN_HOME=/opt/maven
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
```

### 数据库初始化

```bash
# 1. 创建数据库（在PostgreSQL中执行）
CREATE DATABASE xiaolvshu_user ENCODING 'UTF8';
CREATE DATABASE xiaolvshu_message ENCODING 'UTF8';
CREATE DATABASE xiaolvshu_content ENCODING 'UTF8';
CREATE DATABASE xiaolvshu_product ENCODING 'UTF8';
CREATE DATABASE xiaolvshu_marketing ENCODING 'UTF8';
CREATE DATABASE xiaolvshu_system ENCODING 'UTF8';

# 2. 执行初始化SQL
psql -U postgres -d xiaolvshu_system -f sql/system-schema.sql
psql -U postgres -d xiaolvshu_user -f sql/member-schema.sql
# ... 依次执行其他SQL文件
```

---

## Nacos注册中心启动

### 方式1：Docker启动（推荐）

```bash
# 单机模式启动Nacos
docker run -d \
  --name nacos-server \
  -e MODE=standalone \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  nacos/nacos-server:v2.2.0

# 验证Nacos是否启动成功
docker logs -f nacos-server
```

**访问Nacos控制�?*：`http://localhost:8848/nacos`
- 默认账号：`nacos`
- 默认密码：`nacos`

### 方式2：本地启�?
```bash
# Windows
cd nacos/bin
startup.cmd -m standalone

# Linux/Mac
cd nacos/bin
sh startup.sh -m standalone
```

### Nacos配置检�?
登录Nacos控制台后，确认以下配置：

1. **命名空间**：创建`dev`命名空间（命名空间ID：dev�?2. **配置分组**：使用默认的`DEFAULT_GROUP`
3. **配置文件**：可选，如需统一配置可创建以下配置：
   - `gateway-server-dev.yaml`
   - `system-server-dev.yaml`
   - `member-server-dev.yaml`
   - `content-server-dev.yaml`
   - `message-server-dev.yaml`
   - `infra-server-dev.yaml`
   - `pay-server-dev.yaml`

---

## 项目编译

### 完整编译（首次启动）

```bash
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17

# 清理+编译+打包（跳过测试）
mvn clean package -DskipTests

# 或者仅编译（不打包jar�?mvn clean compile -DskipTests
```

**预期输出**�?```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for yudao 2025.09-SNAPSHOT:
[INFO]
[INFO] yudao-dependencies ........................ SUCCESS [  2.145 s]
[INFO] yudao-framework ........................... SUCCESS [ 15.324 s]
[INFO] yudao-gateway ............................. SUCCESS [  8.234 s]
[INFO] yudao-module-system ....................... SUCCESS [ 12.456 s]
[INFO] yudao-module-infra ........................ SUCCESS [ 10.123 s]
[INFO] yudao-module-member ....................... SUCCESS [ 11.789 s]
[INFO] yudao-module-content ...................... SUCCESS [  9.876 s]
[INFO] yudao-module-message ...................... SUCCESS [  8.543 s]
[INFO] yudao-module-pay .......................... SUCCESS [  7.234 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 单模块编译（开发时�?
```bash
# 仅编译gateway模块
mvn clean package -pl yudao-gateway -am -DskipTests

# 仅编译content-server模块
mvn clean package -pl yudao-module-content/yudao-module-content-server -am -DskipTests
```

---

## 微服务启�?
> **⚠️ 重要警告**�?> - �?**禁止使用** `yudao-server` 启动（已废弃�?> - �?**禁止执行** `java -jar yudao-server.jar`（错误命令）
> - �?**必须分别启动** 各个独立微服�?> - �?**推荐使用** `start-all-microservices.bat` 批量启动脚本

### 启动顺序（严格按顺序！）

#### 1️⃣ 先启动基础服务

```bash
# 确保以下服务已启动：
�?Nacos (8848)
�?PostgreSQL (5432)
�?Redis (6379)
�?Kafka (9092)
```

#### 2️⃣ 启动网关服务

```bash
cd yudao-gateway
mvn spring-boot:run

# 或使用jar包启�?java -jar target/yudao-gateway.jar

# 指定环境（默认local�?java -jar target/yudao-gateway.jar --spring.profiles.active=dev
```

**启动成功标志**�?```
🎉 Nacos注册成功
🌐 Gateway服务启动：http://localhost:48080
📚 Swagger文档：http://localhost:48080/doc.html
```

#### 3️⃣ 启动业务微服务（可并行启动）

**方式1：使用批量启动脚本（强烈推荐�?*

项目根目录已提供 `start-all-microservices.bat` 脚本�?
```bash
# Windows
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17
.\start-all-microservices.bat

# 脚本会自动：
# 1. 检查Nacos是否启动
# 2. 编译整个项目
# 3. 按顺序启动Gateway + 8个微服务
# 4. 每个服务启动后等�?0-15�?# 5. 打开Nacos控制台检查注册状�?```

**启动后会看到10个命令行窗口**�?- Gateway-Server（绿色标题）
- System-Server
- Infra-Server
- Member-Server
- Pay-Server
- Content-Server
- Message-Server
- MP-Server

**方式2：自定义批处理脚�?*

如需自定义启动顺序，可创�?`start-custom-services.bat`:
```batch
@echo off
echo Starting Xiaolvshu Microservices...

start "Gateway-Server" cmd /k "cd yudao-gateway && mvn spring-boot:run"
timeout /t 15

start "System-Server" cmd /k "cd yudao-module-system\yudao-module-system-server && mvn spring-boot:run"
timeout /t 10

start "Infra-Server" cmd /k "cd yudao-module-infra\yudao-module-infra-server && mvn spring-boot:run"
timeout /t 10

start "Member-Server" cmd /k "cd yudao-module-member\yudao-module-member-server && mvn spring-boot:run"
timeout /t 10

start "Content-Server" cmd /k "cd yudao-module-content\yudao-module-content-server && mvn spring-boot:run"
timeout /t 10

start "Message-Server" cmd /k "cd yudao-module-message\yudao-module-message-server && mvn spring-boot:run"
timeout /t 10

start "Pay-Server" cmd /k "cd yudao-module-pay\yudao-module-pay-server && mvn spring-boot:run"
timeout /t 10

start "MP-Server" cmd /k "cd yudao-module-mp\yudao-module-mp-server && mvn spring-boot:run"

echo All services started!
pause
```

**手动逐个启动**�?
```bash
# system-server
cd yudao-module-system/yudao-module-system-server
mvn spring-boot:run

# infra-server
cd yudao-module-infra/yudao-module-infra-server
mvn spring-boot:run

# member-server
cd yudao-module-member/yudao-module-member-server
mvn spring-boot:run

# content-server
cd yudao-module-content/yudao-module-content-server
mvn spring-boot:run

# message-server
cd yudao-module-message/yudao-module-message-server
mvn spring-boot:run

# pay-server
cd yudao-module-pay/yudao-module-pay-server
mvn spring-boot:run
```

---

## 验证服务状�?
### 1. 检查Nacos服务列表

访问：`http://localhost:8848/nacos/` �?服务管理 �?服务列表

**预期结果**�?```
�?gateway-server (1个实例，健康)
�?system-server (1个实例，健康)
�?infra-server (1个实例，健康)
�?member-server (1个实例，健康)
�?content-server (1个实例，健康)
�?message-server (1个实例，健康)
�?pay-server (1个实例，健康)
```

### 2. 测试网关路由

```bash
# 测试网关健康检�?curl http://localhost:48080/actuator/health

# 测试member服务路由（通过网关�?curl -X POST http://localhost:48080/api/v1.0.1/member/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "test123"
  }'

# 测试content服务路由（通过网关�?curl http://localhost:48080/api/v1.0.1/publish_content/index?pageNo=1&pageSize=10
```

**预期返回**�?```json
{
  "code": 200,    // �?成功码已修复�?00
  "msg": "",
  "data": {...}
}
```

### 3. 访问Swagger文档

访问：`http://localhost:48080/doc.html`

**预期内容**�?- gateway-server文档
- system-server文档
- infra-server文档
- member-server文档
- **content-server文档** �?- **message-server文档** �?- pay-server文档

### 4. 查看服务日志

```bash
# 网关日志
tail -f ~/logs/gateway-server.log

# 业务服务日志
tail -f ~/logs/system-server.log
tail -f ~/logs/member-server.log
tail -f ~/logs/content-server.log
# ...
```

**正常日志标志**�?```
�?Nacos注册成功：[content-server] registered
�?Redis连接成功
�?PostgreSQL连接成功
�?Kafka连接成功
�?无ERROR或WARN日志
```

---

## 常见问题排查

### 问题1：服务无法注册到Nacos

**现象**�?```
ERROR [nacos] failed to req api:/nacos/v1/ns/instance after all servers
```

**排查步骤**�?1. 检查Nacos是否启动：`http://localhost:8848/nacos`
2. 检查配置文件中Nacos地址是否正确�?   ```yaml
   spring:
     cloud:
       nacos:
         server-addr: 127.0.0.1:8848  # 确认端口正确
         discovery:
           namespace: dev  # 确认命名空间存在
   ```
3. 检查防火墙是否开�?848/9848/9849端口

### 问题2：网关路�?04

**现象**�?```
{
  "timestamp": "2025-10-13T10:00:00.000+00:00",
  "path": "/api/v1.0.1/member/auth/login",
  "status": 404,
  "error": "Not Found"
}
```

**排查步骤**�?1. 确认目标服务已在Nacos注册：访问Nacos服务列表
2. 检查网关路由配置：`yudao-gateway/src/main/resources/application.yaml`
3. 检查路径前缀是否正确�?   - �?正确：`/api/v1.0.1/member/auth/login`
   - �?错误：`/member/auth/login`（缺少版本前缀�?
### 问题3：返回码仍然�?而不�?00

**现象**�?```json
{
  "code": 0,  // �?应该�?00
  "msg": "成功"
}
```

**原因**：代码未重新编译

**解决**�?```bash
# 1. 停止所有服�?# 2. 清理并重新编�?mvn clean compile -DskipTests
# 3. 重新启动服务
```

### 问题4：OpenFeign调用失败

**现象**�?```
feign.FeignException$ServiceUnavailable: [503] during [POST] to [http://member-server/...]
```

**排查步骤**�?1. 确认目标服务已启动并注册到Nacos
2. 检查Feign接口的`@FeignClient`配置�?   ```java
   @FeignClient(name = "member-server", path = "/member")
   public interface MemberApi {
       // ...
   }
   ```
3. 确认OpenFeign依赖未被排除（已修复�?
### 问题5：数据库连接失败

**现象**�?```
com.zaxxer.hikari.pool.HikariPool$PoolInitializationException:
  Failed to initialize pool: Connection refused
```

**排查步骤**�?1. 检查PostgreSQL是否启动�?   ```bash
   # Windows
   services.msc �?查找 PostgreSQL

   # Linux
   systemctl status postgresql
   ```
2. 检查数据库配置�?   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:55432/xiaolvshu_system
       username: postgres
       password: your_password
   ```
3. 测试数据库连接：
   ```bash
   psql -U postgres -h localhost -p 5432 -d xiaolvshu_system
   ```

---

## 端口分配�?
| 服务 | 端口 | 说明 | 访问地址 |
|------|------|------|----------|
| **Nacos** | 8848 | 注册中心+配置中心 | http://localhost:8848/nacos |
| **yudao-gateway** | 48080 | API网关（统一入口�?| http://localhost:48080 |
| **system-server** | 48081 | 系统管理服务 | http://localhost:48081 |
| **infra-server** | 48082 | 基础设施服务 | http://localhost:48082 |
| **member-server** | 48083 | 会员管理服务 | http://localhost:48083 |
| **content-server** | 48084 | 内容管理服务 | http://localhost:48084 |
| **message-server** | 48085 | 消息通讯服务 | http://localhost:48085 |
| **pay-server** | 48086 | 支付服务 | http://localhost:48086 |
| **PostgreSQL** | 5432 | 数据�?| - |
| **Redis** | 6379 | 缓存 | - |
| **Kafka** | 9092 | 消息队列 | - |
| **Elasticsearch** | 9200 | 搜索引擎 | http://localhost:9200 |

---

## 生产环境部署建议

### Docker Compose部署

创建 `docker-compose.yml`�?```yaml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:v2.2.0
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"
    networks:
      - xiaolvshu-net

  gateway:
    image: xiaolvshu/gateway:latest
    ports:
      - "48080:48080"
    depends_on:
      - nacos
    networks:
      - xiaolvshu-net

  system-server:
    image: xiaolvshu/system-server:latest
    ports:
      - "48081:48081"
    depends_on:
      - nacos
    networks:
      - xiaolvshu-net

  # ... 其他服务

networks:
  xiaolvshu-net:
    driver: bridge
```

启动�?```bash
docker-compose up -d
```

### Kubernetes部署

参考配置文件：`k8s/deployment.yaml`

```bash
# 部署到K8s集群
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

---

## 监控与运�?
### 健康检�?
所有服务都暴露了Spring Boot Actuator端点�?
```bash
# 健康检�?curl http://localhost:48080/actuator/health

# 查看所有端�?curl http://localhost:48080/actuator

# 查看metrics
curl http://localhost:48080/actuator/metrics
```

### 日志收集

推荐使用ELK Stack�?- Elasticsearch：日志存�?- Logstash：日志收�?- Kibana：日志可视化

### 链路追踪

集成SkyWalking（已在框架中预留）：
```bash
# 启动SkyWalking OAP
java -jar skywalking-oap-server.jar

# 启动SkyWalking UI
java -jar skywalking-webapp.jar
```

---

## 附录

### 快速启动命令汇�?
```bash
# 1. 启动Nacos
docker run -d --name nacos -e MODE=standalone -p 8848:8848 nacos/nacos-server:v2.2.0

# 2. 编译项目
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17
mvn clean package -DskipTests

# 3. 启动网关
cd yudao-gateway
mvn spring-boot:run

# 4. 启动所有业务服务（新开终端窗口�?.\start-all-services.bat

# 5. 验证服务
curl http://localhost:48080/actuator/health
```

### 停止所有服�?
```bash
# Windows：关闭所有cmd窗口

# Linux：使用pkill
pkill -f "yudao-gateway"
pkill -f "system-server"
pkill -f "member-server"
pkill -f "content-server"
pkill -f "message-server"
pkill -f "infra-server"
pkill -f "pay-server"
```

---

## 技术支�?
如遇到问题，请参考：
1. **项目文档**：`PROJECT_MEMORY.md`
2. **架构分析**：`架构分析.md`
3. **需求文�?*：`需求文�?md`
4. **各模块MEMORY**：`yudao-module-{name}/MODULE_MEMORY.md`

---

## 版本历史

### v2.0 - 2025-10-13 14:30（架构重大升级）
- 🚫 **废弃yudao-server** - 不再聚合业务模块，改为空壳项�?- �?**真正微服务化** - 每个模块独立启动，独立部�?- �?**创建MessageServerApplication** - 补齐message模块启动�?- �?**提供批量启动脚本** - `start-all-microservices.bat`一键启�?个微服务
- 📝 **强制规则更新** - 添加禁止使用yudao-server的规�?- 🎯 **服务数量** - Gateway + 8个业务微服务（system/infra/member/pay/content/message/mp�?
### v1.0 - 2025-10-13（架构初步修复）
- �?启用Spring Cloud Gateway
- �?修复OpenFeign被排除的问题
- �?配置网关路由规则
- �?统一API版本前缀
- �?修复返回码标准（0 �?200�?- �?Swagger文档聚合

---

**最后更�?*�?025-10-13 14:30
**文档版本**：v2.0
**架构状�?*：✅ 完整微服务架构（真·微服务�?
