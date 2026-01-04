package cn.iocoder.yudao.module.message.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话消息分页请求 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 会话消息分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppMessagePageReqVO extends PageParam {

    private static final long serialVersionUID = 1L;

    @Schema(description = "对端用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "对端用户编号不能为空")
    private Long targetUserId;

}
