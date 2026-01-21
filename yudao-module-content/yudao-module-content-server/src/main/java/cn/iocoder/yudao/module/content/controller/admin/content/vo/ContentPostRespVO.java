package cn.iocoder.yudao.module.content.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - content post response")
@Data
public class ContentPostRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "1024")
    private Long userId;

    @Schema(description = "Author nickname")
    private String authorNickname;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Content type")
    private Integer contentType;

    @Schema(description = "Publish status")
    private Integer status;

    @Schema(description = "Audit status")
    private Integer auditStatus;

    @Schema(description = "Public flag")
    private Integer isPublic;

    @Schema(description = "Top flag")
    private Integer isTop;

    @Schema(description = "Hot flag")
    private Integer isHot;

    @Schema(description = "Recommend flag")
    private Integer isRecommend;

    @Schema(description = "Channel ID")
    private Long channelId;

    @Schema(description = "Channel name")
    private String channelName;

    @Schema(description = "Topic ID")
    private Long publishTopicId;

    @Schema(description = "Cover image")
    private String coverImage;

    @Schema(description = "View count")
    private Integer viewCount;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Comment count")
    private Integer commentCount;

    @Schema(description = "Share count")
    private Integer shareCount;

    @Schema(description = "Collect count")
    private Integer collectCount;

    @Schema(description = "Publish time")
    private LocalDateTime publishTime;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
