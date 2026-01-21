package cn.iocoder.yudao.module.content.controller.admin.topic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Admin - content topic create/update request")
@Data
public class ContentTopicSaveReqVO {

    @Schema(description = "ID for update", example = "1")
    private Long id;

    @Schema(description = "Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Topic")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Icon URL")
    private String icon;

    @Schema(description = "Cover URL")
    private String cover;

    @Schema(description = "Type", example = "0")
    private Integer type;

    @Schema(description = "Color", example = "#FF9900")
    private String color;

    @Schema(description = "Sort", example = "0")
    private Integer sort;

    @Schema(description = "Recommend flag", example = "0")
    private Integer isRecommend;

    @Schema(description = "Status", example = "1")
    private Integer status;
}
