package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 发送消息请求VO
 *
 * @author xiaolvshu
 */
@Schema(description = "APP - 发送消息请求")
@Data
public class MessageSendReqVO {

    @Schema(description = "接收者用户ID", required = true, example = "2")
    @NotNull(message = "接收者用户ID不能为空")
    private Long toUserId;

    @Schema(description = "消息类型", required = true, example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer type;

    @Schema(description = "消息内容（文本消息必填）", example = "你好")
    private String content;

    @Schema(description = "媒体 URL 列表（图片/视频/GIF/文件/语音等）",
            example = "[\"https://xx/1.png\",\"https://xx/2.png\"]")
    private List<String> mediaUrls;

    @Schema(description = "分享卡片内容ID（当 type=6 卡片时必填）", example = "1024")
    private Long cardContentId;

    @Schema(description = "扩展数据(JSON格式)", example = "{\"width\":800,\"height\":600}")
    private String extraData;
}
