package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - content ad response")
@Data
public class ContentAdRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Title", example = "Ad title")
    private String title;

    @Schema(description = "Sub title", example = "Tagline")
    private String subTitle;

    @Schema(description = "Advertiser name", example = "Brand")
    private String advertiserName;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Priority", example = "10")
    private Integer priority;

    @Schema(description = "Display scene", example = "1")
    private Integer displayScene;

    @Schema(description = "Frequency cap per user", example = "3")
    private Integer frequencyCap;

    @Schema(description = "Start time")
    private LocalDateTime startTime;

    @Schema(description = "End time")
    private LocalDateTime endTime;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
