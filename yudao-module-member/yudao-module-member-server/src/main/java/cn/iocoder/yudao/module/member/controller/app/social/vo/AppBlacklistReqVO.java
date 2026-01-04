package cn.iocoder.yudao.module.member.controller.app.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppBlacklistReqVO {

    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long targetUserId;

    @Schema(description = "原因")
    private String reason;
}
