package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 更新群信息请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 更新群信息请求")
@Data
public class GroupUpdateReqVO {

    @Schema(description = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "群名称", example = "新的群名称")
    private String groupName;

    @Schema(description = "群头像", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    @Schema(description = "群公告", example = "欢迎新成员")
    private String announcement;

    @Schema(description = "群简介", example = "这是一个技术交流群")
    private String description;

    @Schema(description = "加群验证方式 0-自由加入 1-需要验证 2-禁止加入", example = "1")
    private Integer joinType;
}

