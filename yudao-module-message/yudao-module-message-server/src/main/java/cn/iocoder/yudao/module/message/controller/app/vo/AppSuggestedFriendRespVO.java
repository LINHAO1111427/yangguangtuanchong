package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 可能认识的人返回 VO")
@Data
public class AppSuggestedFriendRespVO {

    @Schema(description = "用户信息")
    private AppMemberSimpleRespVO user;

    @Schema(description = "推荐原因", example = "同城附近")
    private String reason;

    @Schema(description = "推荐得分", example = "0.82")
    private Double score;
}

