package cn.iocoder.yudao.module.content.dal.dataobject;

import java.time.LocalDateTime;

/**
 * 用户频道浏览统计的临时结果映射.
 *
 * @author Lin
 */
public class ChannelVisitStatsDO {

    private Long channelId;
    private String channelName;
    private Long visitCount;
    private LocalDateTime lastVisitTime;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Long getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
    }

    public LocalDateTime getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(LocalDateTime lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }
}
