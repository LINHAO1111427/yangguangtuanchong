package cn.iocoder.yudao.module.content.controller.app.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "APP - 创建广告 Request")
public class AppAdCreateReqVO {

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "广告标题不能为空")
    @Length(max = 128, message = "广告标题长度不能超过 128 个字符")
    private String title;

    @Schema(description = "副标题/描述")
    @Length(max = 255, message = "广告描述长度不能超过 255 个字符")
    private String desc;

    @Schema(description = "卡片类型")
    @Length(max = 32, message = "卡片类型长度不能超过 32 个字符")
    private String cardType;

    @Schema(description = "媒体类型(image/video/native)")
    @Length(max = 32, message = "媒体类型长度不能超过 32 个字符")
    private String mediaType;

    @Schema(description = "封面图片(图片广告素材/视频封面)")
    @Length(max = 255, message = "封面地址长度不能超过 255 个字符")
    private String coverImage;

    @Schema(description = "视频URL")
    @Length(max = 255, message = "视频地址长度不能超过 255 个字符")
    private String videoUrl;

    @Schema(description = "跳转链接")
    @Length(max = 255, message = "跳转链接长度不能超过 255 个字符")
    private String link;

    @Schema(description = "行动号召")
    @Length(max = 64, message = "行动号召长度不能超过 64 个字符")
    private String callToAction;

    @Schema(description = "广告主名称")
    @Length(max = 128, message = "广告主名称长度不能超过 128 个字符")
    private String advertiserName;

    @Schema(description = "展示场景(分类)", example = "1")
    private Integer scene;

    @Schema(description = "排序优先级", example = "0")
    private Integer priority;

    @Schema(description = "频控上限(每日)")
    private Integer frequencyCap;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "分类名称(冗余字段，便于前端展示)")
    private String categoryName;

    @Schema(description = "投放区域(冗余字段，便于前端展示)")
    private String targetLocation;

    @Schema(description = "扩展样式信息")
    private Map<String, Object> styleMeta;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public void setAdvertiserName(String advertiserName) {
        this.advertiserName = advertiserName;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getFrequencyCap() {
        return frequencyCap;
    }

    public void setFrequencyCap(Integer frequencyCap) {
        this.frequencyCap = frequencyCap;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Map<String, Object> getStyleMeta() {
        return styleMeta;
    }

    public void setStyleMeta(Map<String, Object> styleMeta) {
        this.styleMeta = styleMeta;
    }
}
