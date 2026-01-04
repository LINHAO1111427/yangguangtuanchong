package cn.iocoder.yudao.module.content.controller.app;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentMediaUploadRespVO;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 内容媒体")
@Validated
@RestController
@RequestMapping("/content/media")
public class AppContentMediaController {

    private static final Logger log = LoggerFactory.getLogger(AppContentMediaController.class);

    private static final long MAX_VIDEO_SIZE = 600L * 1024 * 1024; // 600MB
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",
            "video/x-matroska",
            "video/mpeg",
            "video/webm"
    );

    @Resource
    private FileApi fileApi;

    @PostMapping(value = "/upload_image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传内容图片", description = "使用存储服务保存图片，返回可访问地址")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ContentMediaUploadRespVO> uploadImage(@NotNull @RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "directory", required = false) String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw exception(ErrorCodeConstants.FILE_UPLOAD_FAILED);
        }
        String storeDirectory = StrUtil.blankToDefault(directory, "content/image");
        String fileName = StrUtil.blankToDefault(file.getOriginalFilename(), "image_" + System.currentTimeMillis());
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String url = fileApi.createFile(bytes, fileName, storeDirectory, file.getContentType());
        ContentMediaUploadRespVO respVO = new ContentMediaUploadRespVO();
        respVO.setUrl(url);
        respVO.setFileName(fileName);
        respVO.setContentType(file.getContentType());
        respVO.setSize(file.getSize());
        return success(respVO);
    }

    @PostMapping(value = "/upload_video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传内容视频", description = "使用 MinIO 存储视频文件，返回可访问地址")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ContentMediaUploadRespVO> uploadVideo(@NotNull @RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "directory", required = false) String directory) throws IOException {
        validateVideoFile(file);
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String storeDirectory = StrUtil.blankToDefault(directory, "content/video");
        String fileName = StrUtil.blankToDefault(file.getOriginalFilename(), "video_" + System.currentTimeMillis());
        String url = fileApi.createFile(bytes, fileName, storeDirectory, file.getContentType());
        ContentMediaUploadRespVO respVO = new ContentMediaUploadRespVO();
        respVO.setUrl(url);
        respVO.setFileName(fileName);
        respVO.setContentType(file.getContentType());
        respVO.setSize(file.getSize());
        log.info("Upload content video success, name={}, size={}, url={}", fileName, file.getSize(), url);
        return success(respVO);
    }

    @Operation(summary = "获取视频播放预签名地址")
    @GetMapping("/play_url")
    @PermitAll
    public CommonResult<String> getPlayUrl(
            @Parameter(description = "视频原始 URL（存储路径）") @RequestParam("url") String url,
            @Parameter(description = "有效期(秒)，默认 600") @RequestParam(value = "expires", required = false) Integer expires) {
        int expiration = expires == null ? 600 : expires;
        // 直接透传文件服务的预签名 URL，避免套一层 CommonResult
        return fileApi.presignGetUrl(url, expiration);
    }

    @GetMapping("/proxy")
    @PermitAll
    @Operation(summary = "媒体代理（用于小程序域名/HTTP限制）", description = "仅允许代理本地 MinIO(127.0.0.1/localhost) 资源，避免 SSRF 风险")
    public void proxy(@RequestParam("url") String url,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            response.sendError(400, "invalid url");
            return;
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            response.sendError(400, "invalid scheme");
            return;
        }
        String host = uri.getHost();
        if (!"127.0.0.1".equals(host) && !"localhost".equalsIgnoreCase(host)) {
            response.sendError(403, "host not allowed");
            return;
        }

        String range = request.getHeader(HttpHeaders.RANGE);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .GET();
        if (range != null) {
            builder.header(HttpHeaders.RANGE, range);
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<InputStream> upstream;
        try {
            upstream = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(502, "proxy interrupted");
            return;
        } catch (Exception e) {
            log.warn("Proxy media failed url={}", url, e);
            response.sendError(502, "proxy failed");
            return;
        }

        int status = upstream.statusCode();
        response.setStatus(status);
        upstream.headers().firstValue(HttpHeaders.CONTENT_TYPE).ifPresent(response::setContentType);
        upstream.headers().firstValue(HttpHeaders.CONTENT_LENGTH).ifPresent(v -> response.setHeader(HttpHeaders.CONTENT_LENGTH, v));
        upstream.headers().firstValue(HttpHeaders.ACCEPT_RANGES).ifPresent(v -> response.setHeader(HttpHeaders.ACCEPT_RANGES, v));
        upstream.headers().firstValue(HttpHeaders.CONTENT_RANGE).ifPresent(v -> response.setHeader(HttpHeaders.CONTENT_RANGE, v));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=600");

        try (InputStream body = upstream.body()) {
            IoUtil.copy(body, response.getOutputStream());
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw exception(ErrorCodeConstants.FILE_UPLOAD_FAILED);
        }
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw exception(ErrorCodeConstants.FILE_SIZE_TOO_LARGE);
        }
        String contentType = StrUtil.blankToDefault(file.getContentType(), "");
        if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
            log.warn("Unsupported video type: {}", contentType);
            throw exception(ErrorCodeConstants.VIDEO_FORMAT_ERROR);
        }
    }
}
