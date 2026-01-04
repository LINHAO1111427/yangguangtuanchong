package cn.iocoder.yudao.module.content.service.feed.bo;

import java.util.ArrayList;
import java.util.List;

public class FeedStreamResultBO {

    private List<FeedCardBO> cards = new ArrayList<>();
    private long total;
    private String strategySummary;

    public List<FeedCardBO> getCards() {
        return cards;
    }

    public void setCards(List<FeedCardBO> cards) {
        this.cards = cards;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getStrategySummary() {
        return strategySummary;
    }

    public void setStrategySummary(String strategySummary) {
        this.strategySummary = strategySummary;
    }
}
