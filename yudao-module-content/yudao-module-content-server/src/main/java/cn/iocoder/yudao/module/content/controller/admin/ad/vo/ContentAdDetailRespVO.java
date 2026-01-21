package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Admin - content ad detail response")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentAdDetailRespVO extends ContentAdRespVO {

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

    @Schema(description = "Frequency cap per user", example = "3")
    private Integer frequencyCap;

    @Schema(description = "Style meta")
    private Map<String, Object> styleMeta;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
