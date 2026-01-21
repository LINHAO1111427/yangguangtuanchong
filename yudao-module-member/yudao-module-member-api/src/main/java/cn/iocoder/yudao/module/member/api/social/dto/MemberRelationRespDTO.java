package cn.iocoder.yudao.module.member.api.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "RPC 服务 - 用户关系概况 Response DTO")
@Data
public class MemberRelationRespDTO {

    @Schema(description = "我是否已关注对方")
    private Boolean following;

    @Schema(description = "对方是否关注我")
    private Boolean follower;

    @Schema(description = "是否互相关注")
    private Boolean mutualFollow;

    @Schema(description = "我是否拉黑对方")
    private Boolean blockedByMe;

    @Schema(description = "对方是否拉黑我")
    private Boolean blockedMe;

    @Schema(description = "是否允许发起沟通")
    private Boolean canMessage;

    @Schema(description = "当前关注状态(0待审核 1通过 2拒绝)")
    private Integer followState;

    @Schema(description = "是否仍处于待审核")
    private Boolean needApproval;
}
