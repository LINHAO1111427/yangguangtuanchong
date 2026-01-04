package cn.iocoder.yudao.module.content.controller.app.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 APP - 常访问频道 Response VO")
@Data
public class ChannelVisitRespVO {

    @Schema(description = "频道编号", example = "12")
    private Long channelId;
    @Schema(description = "频道名称", example = "热点")
    private String channelName;
    @Schema(description = "频道图标")
    private String icon;
    @Schema(description = "频道主题色")
    private String color;
    @Schema(description = "频道描述")
    private String description;
    @Schema(description = "访问次数", example = "32")
    private Long visitCount;
    @Schema(description = "最近访问时间")
    private LocalDateTime lastVisitTime;
    @Schema(description = "内容预览")
    private List<ContentPreview> previews;

    @Data
    public static class ContentPreview {
        private Long contentId;
        private String title;
        private String coverImage;
        private String videoUrl;
        private Integer likeCount;
        private Integer viewCount;
        private LocalDateTime publishTime;
    }
}
