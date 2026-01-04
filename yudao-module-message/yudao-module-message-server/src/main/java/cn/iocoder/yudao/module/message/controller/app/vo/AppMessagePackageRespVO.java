package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息推送/发送返回 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 会话 + 消息组合返回 VO")
@Data
public class AppMessagePackageRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话信息")
    private AppConversationRespVO conversation;

    @Schema(description = "消息信息")
    private AppMessageRespVO message;

}
