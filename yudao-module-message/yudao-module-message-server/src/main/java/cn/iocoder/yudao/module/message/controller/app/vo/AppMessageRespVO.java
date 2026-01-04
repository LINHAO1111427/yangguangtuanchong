package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息返回 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 私信消息返回 VO")
@Data
public class AppMessageRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息编号", example = "512")
    private Long id;

    @Schema(description = "会话编号", example = "100")
    private Long conversationId;

    @Schema(description = "发送者用户编号", example = "2048")
    private Long fromUserId;

    @Schema(description = "接收者用户编号", example = "4096")
    private Long toUserId;

    @Schema(description = "是否为本人消息", example = "true")
    private Boolean self;

    @Schema(description = "消息类型 1-文本 2-图片 ...", example = "1")
    private Integer type;

    @Schema(description = "消息内容", example = "下午有空吗？")
    private String content;

    @Schema(description = "扩展内容(JSON)", example = "{\"width\":300}")
    private String extraData;

    @Schema(description = "消息状态 0-未读 1-已读 2-撤回", example = "0")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;

    @Schema(description = "发送者信息")
    private AppMemberSimpleRespVO sender;

}
