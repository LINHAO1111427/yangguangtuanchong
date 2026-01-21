package cn.iocoder.yudao.module.message.controller.admin.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - notification response")
@Data
public class NotificationRespVO {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "1024")
    private Long userId;

    @Schema(description = "User nickname")
    private String userName;

    @Schema(description = "Notification type", example = "1")
    private Integer type;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Link")
    private String link;

    @Schema(description = "Read status", example = "0")
    private Integer isRead;

    @Schema(description = "Delete status", example = "0")
    private Integer deleted;

    @Schema(description = "Read time")
    private LocalDateTime readTime;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
