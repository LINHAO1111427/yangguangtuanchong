package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App - 分片 ETag 信息")
@Data
public class MultipartUploadPartVO {

    @Schema(description = "分片序号，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @Min(value = 1, message = "partNumber 必须从 1 开始")
    private Integer partNumber;

    @Schema(description = "分片 ETag", requiredMode = Schema.RequiredMode.REQUIRED, example = "\"f4f9799c61cd17918cbd6c8817936ae4\"")
    @NotBlank(message = "etag 不能为空")
    private String etag;
}
