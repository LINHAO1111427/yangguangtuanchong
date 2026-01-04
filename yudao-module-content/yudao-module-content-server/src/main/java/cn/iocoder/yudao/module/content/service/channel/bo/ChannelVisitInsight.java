package cn.iocoder.yudao.module.content.service.channel.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户常访问频道的聚合结果.
 *
 * @author Lin
 */
public class ChannelVisitInsight {

    private Long channelId;
    private String channelName;
    private String channelIcon;
    private String channelColor;
    private String channelDescription;
    private Long visitCount;
    private LocalDateTime lastVisitTime;
    private List<ContentPreview> previews = new ArrayList<>();

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

    public String getChannelIcon() {
        return channelIcon;
    }

    public void setChannelIcon(String channelIcon) {
        this.channelIcon = channelIcon;
    }

    public String getChannelColor() {
        return channelColor;
    }

    public void setChannelColor(String channelColor) {
        this.channelColor = channelColor;
    }

    public String getChannelDescription() {
        return channelDescription;
    }

    public void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
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

    public List<ContentPreview> getPreviews() {
        return previews;
    }

    public void setPreviews(List<ContentPreview> previews) {
        this.previews = previews;
    }

    public static class ContentPreview {
        private Long contentId;
        private String title;
        private String coverImage;
        private String videoUrl;
        private Integer likeCount;
        private Integer viewCount;
        private LocalDateTime publishTime;

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
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

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public Integer getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Integer likeCount) {
            this.likeCount = likeCount;
        }

        public Integer getViewCount() {
            return viewCount;
        }

        public void setViewCount(Integer viewCount) {
            this.viewCount = viewCount;
        }

        public LocalDateTime getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(LocalDateTime publishTime) {
            this.publishTime = publishTime;
        }
    }
}
