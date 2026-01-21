package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "APP - 私信发送权限")
public class AppMessagePermissionRespVO {

    @Schema(description = "是否允许发送", example = "true")
    private Boolean canSend;

    @Schema(description = "是否仅允许文字", example = "true")
    private Boolean textOnly;

    @Schema(description = "是否触发陌生人限制提示", example = "true")
    private Boolean limitActive;

    @Schema(description = "剩余可发送文字条数", example = "1")
    private Integer remainingTextCount;

    @Schema(description = "限制时间窗口(小时)", example = "24")
    private Integer limitHours;

    @Schema(description = "提示文案")
    private String tip;
}
