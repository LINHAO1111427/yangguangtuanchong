package cn.iocoder.yudao.module.content.service.feed.bo;

import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;

/**
 * 统一的 Feed 卡片结构，既可以承载内容，也可以承载广告。
 */
public class FeedCardBO {

    public enum CardType {
        CONTENT,
        AD
    }

    private CardType cardType;
    private String layout;
    private String strategy;
    private ContentListRespVO content;
    private ContentAdDO ad;
    private Integer rewardAmount;
    private Double score;

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
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

    public ContentAdDO getAd() {
        return ad;
    }

    public void setAd(ContentAdDO ad) {
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
