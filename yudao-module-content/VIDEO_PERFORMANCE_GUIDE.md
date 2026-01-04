# 视频上传&播放性能方案（5000 QPS 目标）

本文档说明如何基于现有 MinIO（S3 兼容）实现平滑的视频上传与展示，并给出中间件/网络层配置建议。

## 1. 接口与流程
- **上传**：前端调用 `POST /app-api/infra/file/multipart/init` 获取 `uploadId/path/partSize/url` → 按 `partSize` 并发 PUT 分片（`GET /app-api/infra/file/multipart/part-url` 获取预签名 URL）→ 所有分片完成后 `POST /app-api/infra/file/multipart/complete` 合并 → 调用 `/create` 记录文件。
- **播放**：内容模块保存的 `videoUrl` 即对象路径；对私有桶直接调用 `FileApi.presignGetUrl` 获取短时播放地址；对公有桶使用 CDN 域名直接播放。
- **分片策略**：默认 8MB/片，200MB 视频 ≈ 25 片，可支撑高并发且保持较低 RT。

## 2. MinIO/S3 存储配置
- **桶配置**：`enablePathStyleAccess=true`；分片上传保持默认 5~10MB 区间；开启版本化（便于秒传与回滚）。
- **集群**：最少 4+2 EC（4 数据 2 纠删）节点；每节点 >= 8C16G、NVMe，本地直连 10GbE。
- **网络**：前端与 MinIO 之间走内网或边缘节点；开启 `keep-alive`，`max-concurrent-requests` ≥ 2048。
- **认证**：生产环境 `enablePublicAccess=false`，所有读写通过预签名；将 `accessKey/secret` 配置到 Nacos（application-minio.yaml）。

## 3. 网关/CDN/Nginx
- **CDN**：为视频桶绑定 CDN 域名，缓存命中 2h（短视频），`Range` 请求透传；开启 HTTP/2 + Gzip 仅对文本，视频禁用压缩。
- **边缘缓存**：Nginx 示例：
  ```
  proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=video_cache:512m inactive=2h use_temp_path=off;
  server {
    location /content/video/ {
      proxy_pass https://minio.internal;
      proxy_cache video_cache;
      proxy_cache_valid 200 206 302 2h;
      proxy_ignore_headers "Set-Cookie" "Cache-Control";
      proxy_set_header Range $http_range;
      proxy_set_header If-Range $http_if_range;
    }
  }
  ```
- **断点续传/Range**：播放器开启分段/Range 请求，避免整段重传导致卡顿。

## 4. 应用层优化
- **并发控制**：前端限制单文件并发分片数 4~6，重试上限 3 次，失败分片重传。
- **秒传**：先计算文件 hash；若后端记录已存在则直接返回 URL（可在后续迭代实现）。
- **回源降级**：CDN 回源失败时改用最近的 MinIO 节点或备用 S3。
- **预签名过期**：上传分片 URL 15~20 分钟，播放 URL 10~30 分钟；播放时本地缓存短期 token，避免频繁刷新。

## 5. 观测与告警
- **MinIO**：`s3_requests_inflight`、`s3_requests_errors_total`、`s3_ttfb_seconds`、`s3_network_sent_bytes_total`。
- **Nginx/CDN**：命中率、回源失败率、P95/P99 延迟、206 占比。
- **应用**：`multipart.init/part-url/complete` QPS、失败率、平均耗时；Kafka（如有转码）积压。

## 6. 推荐中间件参数（5000 QPS 基线）
- MinIO：`--max-requests 8192`，`gateway_http_client_transport_max_idle 4096`。
- Nginx：`worker_connections 65536`，`proxy_connect_timeout 2s`，`proxy_read_timeout 120s`，`sendfile on`，`tcp_nodelay on`。
- JVM（上传服务）：`-Xms2g -Xmx2g`，启用 Netty/Tomcat NIO，`server.tomcat.max-threads=500`，`server.tomcat.accept-count=2000`。

## 7. 迭代建议
- 增加 **上传会话存储**（Redis）用于记录分片完成度与秒传状态。
- 上传完成后异步推送 Kafka 事件，触发转码/截图/审核。
- 支持 **HLS/DASH** 切片，播放器可自适应码率，进一步降低卡顿概率。
