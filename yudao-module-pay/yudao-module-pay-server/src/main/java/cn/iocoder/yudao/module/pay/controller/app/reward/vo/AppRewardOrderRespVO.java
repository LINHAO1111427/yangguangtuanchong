package cn.iocoder.yudao.module.pay.controller.app.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 打赏订单 Response VO")
@Data
public class AppRewardOrderRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "打赏用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567")
    private Long userId;

    @Schema(description = "被打赏用户ID（作者ID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2345678")
    private Long authorId;

    @Schema(description = "作者昵称", example = "张三")
    private String authorNickname;

    @Schema(description = "作者头像", example = "https://xxx.jpg")
    private String authorAvatar;

    @Schema(description = "打赏内容ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long targetId;

    @Schema(description = "打赏类型 1-内容打赏 2-评论打赏", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer rewardType;

    @Schema(description = "打赏金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer amount;

    @Schema(description = "平台抽成金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer commissionAmount;

    @Schema(description = "作者收益金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    private Integer incomeAmount;

    @Schema(description = "支付状态 0-待支付 1-已支付 2-已退款", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer payStatus;

    @Schema(description = "支付时间", example = "2025-10-10 12:00:00")
    private LocalDateTime payTime;

    @Schema(description = "备注", example = "谢谢分享！")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-10-10 12:00:00")
    private LocalDateTime createTime;

}
