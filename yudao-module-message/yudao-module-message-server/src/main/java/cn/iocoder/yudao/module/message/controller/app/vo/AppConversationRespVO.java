package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话返回 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 会话返回 VO")
@Data
public class AppConversationRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话编号", example = "1024")
    private Long id;

    @Schema(description = "当前用户编号", example = "2048")
    private Long userId;

    @Schema(description = "对端用户编号", example = "4096")
    private Long targetUserId;

    @Schema(description = "对端昵称", example = "小绿薯")
    private String targetNickname;

    @Schema(description = "对端头像", example = "https://static.xiaolvshu.cn/avatar.png")
    private String targetAvatar;

    @Schema(description = "对端是否在线", example = "true")
    private Boolean targetOnline;

    @Schema(description = "最新消息内容", example = "你好呀～")
    private String lastMessageContent;

    @Schema(description = "最新消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读数量", example = "5")
    private Integer unreadCount;

    @Schema(description = "是否置顶(0/1)", example = "1")
    private Integer isTop;

    @Schema(description = "是否免打扰(0/1)", example = "0")
    private Integer isMute;

}
