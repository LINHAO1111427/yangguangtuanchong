package cn.iocoder.yudao.module.member.controller.app.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "用户 APP - 我的访客统计 Response VO")
@Data
public class AppMemberVisitorStatsRespVO {

    @Schema(description = "统计天数", example = "7")
    private Integer days;

    @Schema(description = "当前周期去重访客数", example = "120")
    private Long total;

    @Schema(description = "对比周期去重访客数", example = "90")
    private Long compareTotal;

    @Schema(description = "涨跌幅（百分比，可能为 null）", example = "33")
    private Integer changePercent;

    @Schema(description = "每日去重访客数")
    private List<Day> daily;

    @Schema(description = "每日统计")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Day {

        @Schema(description = "日期（yyyy-MM-dd）", example = "2025-12-16")
        private String day;

        @Schema(description = "去重访客数", example = "10")
        private Long count;
    }
}

