package cn.iocoder.yudao.module.message.controller.app.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "APP - 完成分片上传 Request VO")
@Data
public class FileUploadCompleteReqVO {

    @Schema(description = "上传ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "u_abc123")
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

    @Schema(description = "总分片数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "总分片数不能为空")
    @Min(value = 1, message = "总分片数必须大于0")
    private Integer totalChunks;
}

