package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建群组请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 创建群组请求")
@Data
public class GroupCreateReqVO {

    @Schema(description = "群名称", required = true, example = "技术交流群")
    @NotBlank(message = "群名称不能为空")
    private String groupName;

    @Schema(description = "群头像", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    @Schema(description = "群简介", example = "这是一个技术交流群")
    private String description;

    @Schema(description = "最大成员数", example = "500")
    private Integer maxMemberCount;

    @Schema(description = "加群验证方式 0-自由加入 1-需要验证 2-禁止加入", example = "0")
    private Integer joinType;

    @Schema(description = "初始成员用户ID列表", example = "[2, 3, 4]")
    private List<Long> memberIds;
}

