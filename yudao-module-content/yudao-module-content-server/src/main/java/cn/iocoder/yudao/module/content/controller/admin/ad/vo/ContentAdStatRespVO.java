package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Admin - content ad stat response")
@Data
public class ContentAdStatRespVO {

    @Schema(description = "Ad ID", example = "1")
    private Long adId;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Advertiser name")
    private String advertiserName;

    @Schema(description = "Display scene")
    private Integer displayScene;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Impression count")
    private Long impressionCount;

    @Schema(description = "Click count")
    private Long clickCount;

    @Schema(description = "Unique impression count")
    private Long uniqueImpressionCount;

    @Schema(description = "Unique click count")
    private Long uniqueClickCount;

    @Schema(description = "Estimated revenue")
    private BigDecimal revenue;

    @Schema(description = "First event time")
    private LocalDateTime firstEventTime;

    @Schema(description = "Last event time")
    private LocalDateTime lastEventTime;
}
