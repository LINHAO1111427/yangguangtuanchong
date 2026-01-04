package cn.iocoder.yudao.module.member.controller.app.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 访客记录 Request VO")
@Data
public class AppMemberVisitorRecordReqVO {

    @Schema(description = "被访问用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "被访问用户 ID 不能为空")
    private Long ownerId;

    @Schema(description = "访问类型：1=主页 2=作品", example = "1")
    private Integer visitType;

    @Schema(description = "目标 ID（例如作品 ID）", example = "123")
    private Long targetId;
}

