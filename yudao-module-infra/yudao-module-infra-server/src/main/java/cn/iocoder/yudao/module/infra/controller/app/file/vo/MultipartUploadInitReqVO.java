package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "App - 分片上传初始化 Request VO")
@Data
public class MultipartUploadInitReqVO {

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "video.mp4")
    @NotBlank(message = "文件名不能为空")
    private String name;

    @Schema(description = "目录前缀", example = "content/video")
    private String directory;

    @Schema(description = "MIME 类型", example = "video/mp4")
    private String contentType;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED, example = "104857600")
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于 0")
    private Long fileSize;
}
