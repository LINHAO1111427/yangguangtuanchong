package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理后台 - 批量提现审核 Request VO
 *
 * @author xiaolvshu
 */
@Schema(description = "管理后台 - 批量提现审核 Request VO")
@Data
public class PayWithdrawBatchAuditReqVO {

    @Schema(description = "订单ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3]")
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "是否通过审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @Schema(description = "审核备注", example = "批量审核通过")
    private String auditRemark;

}

