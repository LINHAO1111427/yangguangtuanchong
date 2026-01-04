package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 提现审核统计 Response VO
 *
 * @author xiaolvshu
 */
@Schema(description = "管理后台 - 提现审核统计 Response VO")
@Data
public class PayWithdrawStatisticsRespVO {

    @Schema(description = "今日待审核数量", example = "10")
    private Long todayPendingCount;

    @Schema(description = "今日已审核数量", example = "20")
    private Long todayAuditedCount;

    @Schema(description = "今日审核通过数量", example = "18")
    private Long todayApprovedCount;

    @Schema(description = "今日审核拒绝数量", example = "2")
    private Long todayRejectedCount;

    @Schema(description = "今日审核通过金额（分）", example = "1000000")
    private Long todayApprovedAmount;

    @Schema(description = "历史待审核数量", example = "5")
    private Long totalPendingCount;

    @Schema(description = "历史待审核金额（分）", example = "500000")
    private Long totalPendingAmount;

}
