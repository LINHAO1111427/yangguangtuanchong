package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.module.member.service.social.bo.MemberFriendRequestCreateBO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppRelationApplyReqVO {

    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long targetUserId;

    @Schema(description = "关系类型：0关注 1好友")
    private Integer relationType = 0;

    @Schema(description = "来源：0搜索 1推荐 2扫码")
    private Integer source = 0;

    @Schema(description = "附言")
    private String message;

    public MemberFriendRequestCreateBO toBO(Long userId) {
        MemberFriendRequestCreateBO bo = new MemberFriendRequestCreateBO();
        bo.setUserId(userId);
        bo.setTargetUserId(targetUserId);
        bo.setRelationType(relationType);
        bo.setSource(source);
        bo.setRequestMessage(message);
        return bo;
    }
}
