package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 完成分片上传 Request VO")
@Data
public class MultipartUploadCompleteReqVO {

    @Schema(description = "上传会话 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "Yk8dM0d2S8hRZb9c")
    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    @Schema(description = "对象路径（init 返回）", requiredMode = Schema.RequiredMode.REQUIRED, example = "content/video/20250101/video_123.mp4")
    @NotBlank(message = "path 不能为空")
    private String path;

    @Schema(description = "已上传分片列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "parts 不能为空")
    @Valid
    private List<MultipartUploadPartVO> parts;
}
