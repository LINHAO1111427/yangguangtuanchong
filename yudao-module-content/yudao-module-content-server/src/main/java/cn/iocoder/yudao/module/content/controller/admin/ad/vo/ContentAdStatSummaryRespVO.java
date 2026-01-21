package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Admin - content ad stat summary response")
@Data
public class ContentAdStatSummaryRespVO {

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
}
