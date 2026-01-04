package cn.iocoder.yudao.module.pay.controller.app.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Schema(description = "用户 App - 申请提现 Request VO")
@Data
public class AppWithdrawApplyReqVO {

    @Schema(description = "提现金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    @NotNull(message = "提现金额不能为空")
    @Min(value = 1000, message = "提现金额不能小于10元")
    private Integer amount;

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6222024100012345678")
    @NotBlank(message = "银行卡号不能为空")
    private String bankCardNo;

    @Schema(description = "开户行", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国工商银行")
    @NotBlank(message = "开户行不能为空")
    private String bankName;

    @Schema(description = "开户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "开户名不能为空")
    private String accountName;

    @Schema(description = "备注", example = "提现至工行卡")
    private String remark;

}

