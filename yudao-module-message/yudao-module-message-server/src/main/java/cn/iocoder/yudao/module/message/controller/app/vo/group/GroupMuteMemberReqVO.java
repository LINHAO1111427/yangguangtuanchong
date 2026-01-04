package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 禁言群成员请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 禁言群成员请求")
@Data
public class GroupMuteMemberReqVO {

    @Schema(description = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "用户ID", required = true, example = "2")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "禁言时长(分钟)", required = true, example = "60")
    @NotNull(message = "禁言时长不能为空")
    private Integer muteDuration;
}

