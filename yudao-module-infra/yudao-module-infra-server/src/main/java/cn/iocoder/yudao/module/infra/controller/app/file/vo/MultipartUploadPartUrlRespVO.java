package cn.iocoder.yudao.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 获取分片上传 URL Response VO")
@Data
public class MultipartUploadPartUrlRespVO {

    @Schema(description = "预签名上传地址", example = "https://minio.xiaolvshu.com/bucket/key?X-Amz-Signature=xxx")
    private String uploadUrl;
}
