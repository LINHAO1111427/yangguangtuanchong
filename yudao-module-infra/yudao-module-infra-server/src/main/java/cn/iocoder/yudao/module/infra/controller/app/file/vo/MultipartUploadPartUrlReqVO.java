package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App - 获取分片上传 URL Request VO")
@Data
public class MultipartUploadPartUrlReqVO {

    @Schema(description = "上传会话 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "Yk8dM0d2S8hRZb9c")
    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    @Schema(description = "对象路径（init 返回）", requiredMode = Schema.RequiredMode.REQUIRED, example = "content/video/20250101/video_123.mp4")
    @NotBlank(message = "path 不能为空")
    private String path;

    @Schema(description = "分片序号，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @Min(value = 1, message = "partNumber 必须从 1 开始")
    private Integer partNumber;

    @Schema(description = "URL 有效期（秒），默认 15 分钟", example = "900")
    private Integer expiresInSeconds;
}
