package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 搜索结果中的广告卡片展示数据。
 */
@Schema(description = "APP - 搜索结果广告响应")
public class SearchAdRespVO {

    @Schema(description = "广告ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    @Schema(description = "卡片类型")
    private String cardType;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "按钮文案")
    private String callToAction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
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

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }
}
