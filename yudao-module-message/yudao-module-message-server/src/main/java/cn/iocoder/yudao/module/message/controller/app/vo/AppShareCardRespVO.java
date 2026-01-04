package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "APP - 分享作品卡片信息")
@Data
public class AppShareCardRespVO {

    @Schema(description = "内容ID", example = "1024")
    private Long contentId;

    @Schema(description = "内容类型 1图文 2视频", example = "2")
    private Integer contentType;

    @Schema(description = "标题", example = "夏日海边")
    private String title;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "视频 URL")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "作者ID", example = "1001")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
}

