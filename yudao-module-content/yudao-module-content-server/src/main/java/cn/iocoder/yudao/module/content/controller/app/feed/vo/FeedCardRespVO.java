package cn.iocoder.yudao.module.content.controller.app.feed.vo;

import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP - Feed卡片")
public class FeedCardRespVO {

    @Schema(description = "卡片类型：content/ad")
    private String cardType;

    @Schema(description = "布局样式标识")
    private String layout;

    @Schema(description = "命中的策略")
    private String strategy;

    @Schema(description = "命中的内容卡片")
    private ContentListRespVO content;

    @Schema(description = "广告信息，当 card_type=ad 时返回")
    private FeedAdRespVO ad;

    @Schema(description = "内容对应的打赏金额（分）")
    private Integer rewardAmount;

    @Schema(description = "推荐分数")
    private Double score;

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public ContentListRespVO getContent() {
        return content;
    }

    public void setContent(ContentListRespVO content) {
        this.content = content;
    }

    public FeedAdRespVO getAd() {
        return ad;
    }

    public void setAd(FeedAdRespVO ad) {
        this.ad = ad;
    }

    public Integer getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Integer rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
