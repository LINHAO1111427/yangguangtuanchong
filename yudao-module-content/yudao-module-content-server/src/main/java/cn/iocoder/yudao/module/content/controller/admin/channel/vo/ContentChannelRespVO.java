package cn.iocoder.yudao.module.content.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin - content channel response")
@Data
public class ContentChannelRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Code")
    private String code;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Icon")
    private String icon;

    @Schema(description = "Color")
    private String color;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Default flag")
    private Integer isDefault;

    @Schema(description = "Required flag")
    private Integer isRequired;

    @Schema(description = "Keyword hints")
    private List<String> keywordHints;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
