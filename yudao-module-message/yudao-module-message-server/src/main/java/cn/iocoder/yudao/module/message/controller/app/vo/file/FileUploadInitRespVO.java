package cn.iocoder.yudao.module.message.controller.app.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "APP - 初始化分片上传 Response VO")
@Data
public class FileUploadInitRespVO {

    @Schema(description = "上传ID", example = "u_abc123")
    private String uploadId;

    @Schema(description = "分片大小（字节）", example = "2097152")
    private Integer chunkSize;

    @Schema(description = "总分片数", example = "5")
    private Integer totalChunks;

    @Schema(description = "已上传分片索引列表（0-based）", example = "[0,1,3]")
    private List<Integer> uploadedChunks;
}

