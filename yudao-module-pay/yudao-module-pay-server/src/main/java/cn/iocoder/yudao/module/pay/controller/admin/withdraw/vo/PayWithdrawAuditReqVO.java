package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 管理后台 - 提现审核 Request VO
 *
 * @author xiaolvshu
 */
@Schema(description = "管理后台 - 提现审核 Request VO")
@Data
public class PayWithdrawAuditReqVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单ID不能为空")
    private Long id;

    @Schema(description = "是否通过审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @Schema(description = "审核备注", example = "银行卡信息验证通过")
    @NotBlank(message = "审核备注不能为空")
    private String auditRemark;

}

