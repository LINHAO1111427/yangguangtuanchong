package cn.iocoder.yudao.module.content.controller.app.feed.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "APP - Feed广告卡片")
public class FeedAdRespVO {

    @Schema(description = "广告ID")
    private Long adId;

    @Schema(description = "广告标题")
    private String title;

    @Schema(description = "广告副标题")
    private String subTitle;

    @Schema(description = "卡片类型")
    private String cardType;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    @Schema(description = "行动号召")
    private String callToAction;

    @Schema(description = "广告主名称")
    private String advertiserName;

    @Schema(description = "扩展样式信息")
    private Map<String, Object> styleMeta;

    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
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

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
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

    public Map<String, Object> getStyleMeta() {
        return styleMeta;
    }

    public void setStyleMeta(Map<String, Object> styleMeta) {
        this.styleMeta = styleMeta;
    }
}
