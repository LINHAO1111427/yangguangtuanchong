package cn.iocoder.yudao.module.content.controller.admin.topic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - content topic response")
@Data
public class ContentTopicRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Icon")
    private String icon;

    @Schema(description = "Cover")
    private String cover;

    @Schema(description = "Type")
    private Integer type;

    @Schema(description = "Color")
    private String color;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Recommend flag")
    private Integer isRecommend;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Hot score")
    private Double hotScore;

    @Schema(description = "Content count")
    private Integer contentCount;

    @Schema(description = "Participant count")
    private Integer participantCount;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
