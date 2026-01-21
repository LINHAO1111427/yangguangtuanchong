package cn.iocoder.yudao.module.message.controller.admin.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - group message response")
@Data
public class GroupMessageRespVO {

    @Schema(description = "Message ID", example = "1")
    private Long id;

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "Group name")
    private String groupName;

    @Schema(description = "Sender user ID", example = "1024")
    private Long fromUserId;

    @Schema(description = "Sender nickname")
    private String fromUserName;

    @Schema(description = "Message type", example = "1")
    private Integer type;

    @Schema(description = "Message content")
    private String content;

    @Schema(description = "Extra data (JSON)")
    private String extraData;

    @Schema(description = "Message status", example = "0")
    private Integer status;

    @Schema(description = "Delete status", example = "0")
    private Integer deleted;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
