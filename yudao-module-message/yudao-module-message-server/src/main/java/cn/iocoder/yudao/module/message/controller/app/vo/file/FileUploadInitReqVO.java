package cn.iocoder.yudao.module.message.controller.app.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "APP - 初始化分片上传 Request VO")
@Data
public class FileUploadInitReqVO {

    @Schema(description = "已有上传ID（断点续传时传入）", example = "u_abc123")
    private String uploadId;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "video.mp4")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10485760")
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;

    @Schema(description = "分片大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2097152")
    @NotNull(message = "分片大小不能为空")
    @Min(value = 1024, message = "分片大小过小")
    private Integer chunkSize;

    @Schema(description = "文件类型(MIME)", example = "video/mp4")
    private String contentType;

    @Schema(description = "文件 MD5（可选，用于未来秒传/去重）", example = "5d41402abc4b2a76b9719d911017c592")
    private String md5;
}

