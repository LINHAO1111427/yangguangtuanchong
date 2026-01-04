package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 分片上传初始化 Response VO")
@Data
public class MultipartUploadInitRespVO {

    @Schema(description = "文件配置编号", example = "1")
    private Long configId;

    @Schema(description = "上传会话 ID", example = "Yk8dM0d2S8hRZb9c")
    private String uploadId;

    @Schema(description = "对象路径（不含域名）", example = "content/video/20250101/video_123.mp4")
    private String path;

    @Schema(description = "推荐分片大小（字节）", example = "8388608")
    private Long partSize;

    @Schema(description = "完成上传后可访问的 URL（公开/预签名）", example = "https://cdn.xiaolvshu.com/content/video/xxx.mp4")
    private String url;
}
