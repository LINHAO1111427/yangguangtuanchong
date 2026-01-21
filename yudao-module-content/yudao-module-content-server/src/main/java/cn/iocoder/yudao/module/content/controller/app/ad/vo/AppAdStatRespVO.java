package cn.iocoder.yudao.module.content.controller.app.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "APP - ad statistics")
public class AppAdStatRespVO {

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

    public Long getImpressionCount() {
        return impressionCount;
    }

    public void setImpressionCount(Long impressionCount) {
        this.impressionCount = impressionCount;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public Long getUniqueImpressionCount() {
        return uniqueImpressionCount;
    }

    public void setUniqueImpressionCount(Long uniqueImpressionCount) {
        this.uniqueImpressionCount = uniqueImpressionCount;
    }

    public Long getUniqueClickCount() {
        return uniqueClickCount;
    }

    public void setUniqueClickCount(Long uniqueClickCount) {
        this.uniqueClickCount = uniqueClickCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
