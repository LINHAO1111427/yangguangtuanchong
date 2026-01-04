package cn.iocoder.yudao.module.member.controller.app.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppRelationHandleReqVO {

    @Schema(description = "请求ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long requestId;

    @Schema(description = "拒绝原因")
    private String reason;
}
