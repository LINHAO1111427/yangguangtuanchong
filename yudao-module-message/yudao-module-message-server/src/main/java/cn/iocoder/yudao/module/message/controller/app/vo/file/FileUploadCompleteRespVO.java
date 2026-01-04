package cn.iocoder.yudao.module.message.controller.app.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 完成分片上传 Response VO")
@Data
public class FileUploadCompleteRespVO {

    @Schema(description = "文件访问 URL")
    private String url;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long size;
}

