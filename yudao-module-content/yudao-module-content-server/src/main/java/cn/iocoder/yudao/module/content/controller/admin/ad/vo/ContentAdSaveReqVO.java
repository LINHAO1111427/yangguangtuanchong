package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Admin - content ad create/update request")
@Data
public class ContentAdSaveReqVO {

    @Schema(description = "ID for update", example = "1")
    private Long id;

    @Schema(description = "Title", example = "New ad")
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @Schema(description = "Sub title", example = "Tagline")
    private String subTitle;

    @Schema(description = "Card type", example = "card")
    private String cardType;

    @Schema(description = "Media type", example = "image")
    private String mediaType;

    @Schema(description = "Cover image URL", example = "https://example.com/cover.jpg")
    private String coverImage;

    @Schema(description = "Video URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Jump URL", example = "https://example.com")
    private String jumpUrl;

    @Schema(description = "Call to action", example = "Learn more")
    private String callToAction;

    @Schema(description = "Advertiser name", example = "Brand")
    private String advertiserName;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Priority", example = "10")
    private Integer priority;

    @Schema(description = "Display scene", example = "1")
    private Integer displayScene;

    @Schema(description = "Frequency cap per user", example = "3")
    private Integer frequencyCap;

    @Schema(description = "Start time")
    private LocalDateTime startTime;

    @Schema(description = "End time")
    private LocalDateTime endTime;

    @Schema(description = "Style meta")
    private Map<String, Object> styleMeta;
}
