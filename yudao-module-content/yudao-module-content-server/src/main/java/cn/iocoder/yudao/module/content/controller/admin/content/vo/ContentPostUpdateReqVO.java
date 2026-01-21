package cn.iocoder.yudao.module.content.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Admin - content post update request")
@Data
public class ContentPostUpdateReqVO {

    @Schema(description = "Content ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ID cannot be null")
    private Long id;

    @Schema(description = "Publish status", example = "1")
    private Integer status;

    @Schema(description = "Public flag", example = "1")
    private Integer isPublic;

    @Schema(description = "Allow comment", example = "1")
    private Integer allowComment;

    @Schema(description = "Allow download", example = "0")
    private Integer allowDownload;

    @Schema(description = "Top flag", example = "0")
    private Integer isTop;

    @Schema(description = "Hot flag", example = "0")
    private Integer isHot;

    @Schema(description = "Recommend flag", example = "0")
    private Integer isRecommend;

    @Schema(description = "Channel ID", example = "1")
    private Long channelId;

    @Schema(description = "Topic ID", example = "1")
    private Long publishTopicId;
}
