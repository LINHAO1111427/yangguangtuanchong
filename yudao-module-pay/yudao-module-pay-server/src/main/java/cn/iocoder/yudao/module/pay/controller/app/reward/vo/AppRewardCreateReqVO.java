package cn.iocoder.yudao.module.pay.controller.app.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Schema(description = "用户 App - 创建打赏订单 Request VO")
@Data
public class AppRewardCreateReqVO {

    @Schema(description = "被打赏用户ID（作者ID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567")
    @NotNull(message = "被打赏用户ID不能为空")
    private Long authorId;

    @Schema(description = "打赏内容ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "打赏内容ID不能为空")
    private Long targetId;

    @Schema(description = "打赏类型 1-内容打赏 2-评论打赏", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打赏类型不能为空")
    private Integer rewardType;

    @Schema(description = "打赏金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "打赏金额不能为空")
    @Min(value = 1, message = "打赏金额必须大于0")
    private Integer amount;

    @Schema(description = "备注", example = "谢谢分享！")
    private String remark;

}

