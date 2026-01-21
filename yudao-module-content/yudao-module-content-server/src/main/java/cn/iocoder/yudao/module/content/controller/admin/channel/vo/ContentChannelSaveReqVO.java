package cn.iocoder.yudao.module.content.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - content channel create/update request")
@Data
public class ContentChannelSaveReqVO {

    @Schema(description = "ID for update", example = "1")
    private Long id;

    @Schema(description = "Code", requiredMode = Schema.RequiredMode.REQUIRED, example = "recommend")
    @NotBlank(message = "Code cannot be empty")
    private String code;

    @Schema(description = "Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Recommend")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Icon URL")
    private String icon;

    @Schema(description = "Color")
    private String color;

    @Schema(description = "Sort", example = "0")
    private Integer sort;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Default flag", example = "1")
    private Integer isDefault;

    @Schema(description = "Required flag", example = "1")
    private Integer isRequired;

    @Schema(description = "Keyword hints")
    private List<String> keywordHints;
}
