# 🚀 视频上传功能测试 - 快速运行指南

## 📋 目录
- [单元测试运行](#单元测试运行)
- [端到端测试](#端到端测试)
- [测试结果验证](#测试结果验证)

---

## 🧪 单元测试运行

### 方法1: 使用IntelliJ IDEA (推荐)

1. **打开项目**
   ```
   在IDEA中打开: C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17
   ```

2. **找到测试文件**
   ```
   路径: yudao-module-content/yudao-module-content-server/src/test/java/
         cn/iocoder/yudao/module/content/controller/app/
         AppContentMediaControllerTest.java
   ```

3. **运行测试**
   - 方式A: 右键测试类 → Run 'AppContentMediaControllerTest'
   - 方式B: 点击类名旁的绿色运行按钮
   - 方式C: Ctrl+Shift+F10 (快捷键)

4. **查看结果**
   - 绿色勾: 测试通过 ✅
   - 红色叉: 测试失败 ❌
   - 查看详细日志

---

### 方法2: 使用Maven命令行

#### 前置条件
确保Maven已安装并配置环境变量：
```cmd
mvn -version
```

#### 运行命令

**Windows CMD:**
```cmd
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17\yudao-module-content\yudao-module-content-server

REM 运行所有测试
mvn clean test

REM 只运行视频上传测试
mvn test -Dtest=AppContentMediaControllerTest

REM 运行特定测试方法
mvn test -Dtest=AppContentMediaControllerTest#testUploadValidMp4Video
```

**Windows PowerShell:**
```powershell
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17\yudao-module-content\yudao-module-content-server

# 运行所有测试
mvn clean test

# 只运行视频上传测试
mvn test "-Dtest=AppContentMediaControllerTest"
```

---

### 方法3: 使用Gradle (如果项目用Gradle)

```bash
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17
gradle test --tests AppContentMediaControllerTest
```

---

## 🌐 端到端测试

### 准备工作

#### 1. 启动MINIO服务

**检查MINIO是否运行:**
```cmd
# 访问MINIO控制台
http://localhost:9001

# 登录信息
用户名: minioadmin
密码: minioadmin
```

**如果未运行，启动MINIO:**
```cmd
# Docker方式
docker run -p 9000:9000 -p 9001:9001 ^
  -e MINIO_ROOT_USER=minioadmin ^
  -e MINIO_ROOT_PASSWORD=minioadmin ^
  minio/minio server /data --console-address ":9001"

# 或直接运行MINIO可执行文件
minio.exe server C:\minio-data --console-address ":9001"
```

#### 2. 启动后端服务

```cmd
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17

# 启动content模块
# (根据你的启动方式)
```

#### 3. 启动前端小程序

```cmd
cd C:\WorkSpace\xiaolvshu\interface-new

# 编译微信小程序
npm run dev:mp-weixin

# 打开微信开发者工具，导入目录:
# C:\WorkSpace\xiaolvshu\interface-new\dist\dev\mp-weixin
```

---

### 测试步骤

#### 测试1: 上传图文内容
1. 在小程序中进入"发布内容"页面
2. 选择"图文"类型
3. 输入标题和内容
4. 上传1-9张图片
5. 点击"发布"
6. ✅ 验证上传成功

#### 测试2: 上传视频内容
1. 在小程序中进入"发布内容"页面
2. 选择"视频"类型
3. 输入标题和内容
4. 点击"上传视频"
5. 选择本地视频文件（建议20MB以内，快速测试）
6. 等待上传完成
7. 查看视频预览
8. 点击"发布"
9. ✅ 验证发布成功

#### 测试3: 视频大小限制
1. 尝试上传超过600MB的视频
2. ✅ 应该显示错误提示

#### 测试4: 不支持的格式
1. 尝试上传AVI格式视频
2. ✅ 应该显示格式不支持

---

## 📊 测试结果验证

### 验证1: MINIO中查看文件

1. 打开 http://localhost:9001
2. 登录MINIO控制台
3. 进入 `xiaolvshu-dev` bucket
4. 检查 `content/video/` 目录
5. ✅ 应该看到上传的视频文件

### 验证2: 数据库验证

连接数据库查询发布内容表:
```sql
-- 查看最新发布的视频内容
SELECT * FROM publish_content
WHERE content_type = 2
ORDER BY create_time DESC
LIMIT 10;

-- 验证视频URL是否正确
SELECT id, title, video_url, images
FROM publish_content
WHERE content_type = 2;
```

### 验证3: 后端日志

查看后端日志，应该有:
```
Upload content video success, name=xxx, size=xxx, url=http://localhost:9001/xiaolvshu-dev/content/video/xxx
```

---

## 📈 性能测试 (可选)

### 测试大文件上传

```cmd
# 准备测试文件
# 100MB视频
# 300MB视频
# 500MB视频
# 600MB视频 (边界)
```

### 并发上传测试

使用JMeter或Postman进行并发测试:
- 10个并发用户
- 每个上传50MB视频
- 观察服务器性能

---

## 🐛 常见问题

### 问题1: 测试编译失败
**错误**: Cannot find symbol FileApi

**解决**:
```cmd
cd C:\WorkSpace\xiaolvshu\yudao-cloud-jdk17
mvn clean install -DskipTests
```

### 问题2: MINIO连接失败
**错误**: Connection refused: localhost:9001

**解决**:
1. 检查MINIO是否运行: `docker ps | grep minio`
2. 检查端口是否被占用: `netstat -ano | findstr 9001`
3. 重启MINIO服务

### 问题3: 视频上传超时
**错误**: SocketTimeoutException

**解决**:
1. 检查网络连接
2. 增大超时配置
3. 减小测试视频文件大小

### 问题4: 权限问题
**错误**: 403 Forbidden

**解决**:
1. 检查JWT Token是否有效
2. 确认用户已登录
3. 检查后端认证配置

---

## ✅ 测试检查清单

- [ ] 单元测试全部通过
- [ ] MINIO服务正常运行
- [ ] 后端服务启动成功
- [ ] 前端小程序编译成功
- [ ] 可以成功上传图片
- [ ] 可以成功上传视频
- [ ] 视频预览功能正常
- [ ] 文件大小限制生效
- [ ] 格式验证生效
- [ ] MINIO中可以查看上传的文件
- [ ] 数据库中有正确的记录

---

## 📞 技术支持

遇到问题？
1. 查看测试文档: [VIDEO_UPLOAD_TEST_DOCUMENTATION.md](./VIDEO_UPLOAD_TEST_DOCUMENTATION.md)
2. 查看后端日志
3. 查看浏览器控制台
4. 联系开发团队

---

## 🎉 测试成功标志

如果以下全部通过，说明视频上传功能已完美实现：

✅ 10个单元测试全部通过
✅ 可以通过小程序上传视频到MINIO
✅ 视频可以正常预览和播放
✅ 所有限制和验证正常工作
✅ 性能表现良好

**恭喜！视频上传功能开发完成！🎊**
