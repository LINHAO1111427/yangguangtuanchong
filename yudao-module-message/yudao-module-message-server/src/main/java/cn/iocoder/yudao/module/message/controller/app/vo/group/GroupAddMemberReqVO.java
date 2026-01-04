package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 添加群成员请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 添加群成员请求")
@Data
public class GroupAddMemberReqVO {

    @Schema(description = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "用户ID列表", required = true, example = "[2, 3, 4]")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
}

