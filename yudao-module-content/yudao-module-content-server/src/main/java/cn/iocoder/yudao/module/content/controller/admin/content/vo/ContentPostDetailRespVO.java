package cn.iocoder.yudao.module.content.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin - content post detail response")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentPostDetailRespVO extends ContentPostRespVO {

    @Schema(description = "Content text")
    private String content;

    @Schema(description = "Summary")
    private String summary;

    @Schema(description = "Images")
    private List<String> images;

    @Schema(description = "Video URL")
    private String videoUrl;

    @Schema(description = "Video cover")
    private String videoCover;

    @Schema(description = "Tags")
    private List<String> tags;

    @Schema(description = "Allow comment")
    private Integer allowComment;

    @Schema(description = "Allow download")
    private Integer allowDownload;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
