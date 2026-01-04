package cn.iocoder.yudao.module.member.controller.app.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 会员任务 Response VO")
@Data
public class AppMemberTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "任务类型 1-观看短视频 2-观看图文 3-观看广告", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer taskType;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "观看短视频")
    private String taskName;

    @Schema(description = "任务描述", example = "每日观看3条短视频")
    private String taskDesc;

    @Schema(description = "当前进度", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer progress;

    @Schema(description = "任务要求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer requiredCount;

    @Schema(description = "任务状态 0-进行中 1-已完成 2-已领取奖励", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "奖励积分数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer rewardPoint;

}
