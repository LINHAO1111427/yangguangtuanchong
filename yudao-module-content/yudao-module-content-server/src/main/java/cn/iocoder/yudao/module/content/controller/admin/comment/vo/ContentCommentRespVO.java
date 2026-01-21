package cn.iocoder.yudao.module.content.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - content comment response")
@Data
public class ContentCommentRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Content ID", example = "1")
    private Long contentId;

    @Schema(description = "User ID", example = "1024")
    private Long userId;

    @Schema(description = "User name")
    private String userName;

    @Schema(description = "Reply user ID", example = "2048")
    private Long replyUserId;

    @Schema(description = "Reply user name")
    private String replyUserName;

    @Schema(description = "Parent ID", example = "0")
    private Long parentId;

    @Schema(description = "Root ID", example = "0")
    private Long rootId;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Audit status", example = "1")
    private Integer auditStatus;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Reply count")
    private Integer replyCount;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
