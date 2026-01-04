package cn.iocoder.yudao.module.content.controller.app.feed.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "APP - Feed流响应")
public class FeedStreamRespVO {

    @Schema(description = "卡片列表")
    private List<FeedCardRespVO> cards = new ArrayList<>();

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "策略命中摘要")
    private String strategySummary;

    public List<FeedCardRespVO> getCards() {
        return cards;
    }

    public void setCards(List<FeedCardRespVO> cards) {
        this.cards = cards;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getStrategySummary() {
        return strategySummary;
    }

    public void setStrategySummary(String strategySummary) {
        this.strategySummary = strategySummary;
    }
}
