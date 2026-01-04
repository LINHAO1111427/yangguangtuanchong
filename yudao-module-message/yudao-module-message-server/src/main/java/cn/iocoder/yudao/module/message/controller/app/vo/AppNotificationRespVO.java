package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知返回 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 通知返回 VO")
@Data
public class AppNotificationRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "通知编号", example = "10086")
    private Long id;

    @Schema(description = "通知类型 1-点赞 2-评论 3-关注 4-系统 5-审核", example = "1")
    private Integer type;

    @Schema(description = "触发用户ID（点赞/评论/关注的发起者）", example = "100")
    private Long senderUserId;

    @Schema(description = "触发用户昵称", example = "小绿薯")
    private String senderNickname;

    @Schema(description = "触发用户头像")
    private String senderAvatar;

    @Schema(description = "行为类型：like/collect/comment/follow", example = "like")
    private String behaviorType;

    @Schema(description = "关联内容ID（笔记）", example = "123")
    private Long contentId;

    @Schema(description = "兼容字段：relationId = contentId", example = "123")
    private Long relationId;

    @Schema(description = "内容封面（视频封面或首图）")
    private String relationCover;

    @Schema(description = "评论ID（评论通知）", example = "456")
    private Long commentId;

    @Schema(description = "父评论ID（回复通知）", example = "123")
    private Long parentCommentId;

    @Schema(description = "评论内容片段（评论通知）")
    private String commentText;

    @Schema(description = "标题", example = "有人赞了你")
    private String title;

    @Schema(description = "内容", example = "小绿薯赞了你的作品《夏日海边》")
    private String content;

    @Schema(description = "关联数据(JSON)", example = "{\"postId\":123}")
    private String relatedData;

    @Schema(description = "跳转链接", example = "/pages/post/detail?id=1")
    private String link;

    @Schema(description = "是否已读 0/1", example = "0")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

}
