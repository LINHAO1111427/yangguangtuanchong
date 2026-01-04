package cn.iocoder.yudao.module.pay.controller.app.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 提现订单 Response VO")
@Data
public class AppWithdrawOrderRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567")
    private Long userId;

    @Schema(description = "提现金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private Integer amount;

    @Schema(description = "手续费（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer fee;

    @Schema(description = "实际到账金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "9990")
    private Integer realAmount;

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6222024100012345678")
    private String bankCardNo;

    @Schema(description = "开户行", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国工商银行")
    private String bankName;

    @Schema(description = "开户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String accountName;

    @Schema(description = "提现状态 0-待审核 1-审核通过 2-审核拒绝 3-提现中 4-提现成功 5-提现失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

    @Schema(description = "审核时间", example = "2025-10-10 12:00:00")
    private LocalDateTime auditTime;

    @Schema(description = "提现完成时间", example = "2025-10-10 15:00:00")
    private LocalDateTime completeTime;

    @Schema(description = "失败原因", example = "银行卡信息错误")
    private String failReason;

    @Schema(description = "备注", example = "提现至工行卡")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-10-10 10:00:00")
    private LocalDateTime createTime;

}
