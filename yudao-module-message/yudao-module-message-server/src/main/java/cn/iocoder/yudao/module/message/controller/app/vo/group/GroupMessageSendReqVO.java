package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送群消息请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 发送群消息请求")
@Data
public class GroupMessageSendReqVO {

    @Schema(description = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "消息类型 1-文本 2-图片 3-视频 4-语音 5-文件 6-卡片", required = true, example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer type;

    @Schema(description = "消息内容", required = true, example = "大家好")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "扩展数据(JSON格式，如@成员列表)", example = "{\"mentions\":[2,3]}")
    private String extraData;
}

