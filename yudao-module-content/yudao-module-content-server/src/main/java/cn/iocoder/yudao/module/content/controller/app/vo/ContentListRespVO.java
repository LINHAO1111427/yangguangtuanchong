package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Simplified content list view object returned to the app client.
 */
@Schema(description = "APP content list response VO")
public class ContentListRespVO {

    @Schema(description = "Content id", example = "1024")
    private Long id;

    @Schema(description = "Author user id", example = "1001")
    private Long userId;

    @Schema(description = "Title", example = "Share my day")
    private String title;

    @Schema(description = "Short summary or excerpt")
    private String summary;

    @Schema(description = "Plain text content, trimmed")
    private String contentText;

    @Schema(description = "Content type：1=图文 2=视频")
    private Integer contentType;

    @Schema(description = "Topic id")
    private Long publishTopicId;

    @Schema(description = "Topic name")
    private String publishTopicName;

    @Schema(description = "Channel id")
    private Long channelId;

    @Schema(description = "Channel name")
    private String channelName;

    @Schema(description = "Main cover image URL")
    private String coverImage;

    @Schema(description = "Image list")
    private List<String> images;

    @Schema(description = "Video URL")
    private String videoUrl;

    @Schema(description = "Video cover image")
    private String videoCover;

    @Schema(description = "Video duration (seconds)")
    private Integer videoDuration;
    @Schema(description = "Video width")
    private Integer videoWidth;
    @Schema(description = "Video height")
    private Integer videoHeight;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Comment count")
    private Integer commentCount;

    @Schema(description = "Share count")
    private Integer shareCount;

    @Schema(description = "Collect/favorite count")
    private Integer collectCount;

    @Schema(description = "View count")
    private Integer viewCount;

    @Schema(description = "Whether the current user owns the content")
    private Boolean isMine;

    @Schema(description = "Whether the current user liked the content")
    private Boolean isLiked;

    @Schema(description = "Whether the current user collected the content")
    private Boolean isCollected;

    @Schema(description = "Whether the current user follows the author or topic")
    private Boolean isFollowed;

    @Schema(description = "Author nickname")
    private String authorNickname;

    @Schema(description = "Author avatar")
    private String authorAvatar;

    @Schema(description = "Additional metadata map")
    private Map<String, Object> extra;

    @Schema(description = "Creation time")
    private LocalDateTime createTime;

    @Schema(description = "Last update time")
    private LocalDateTime updateTime;

    @Schema(description = "Publish time")
    private LocalDateTime publishTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public Long getPublishTopicId() {
        return publishTopicId;
    }

    public void setPublishTopicId(Long publishTopicId) {
        this.publishTopicId = publishTopicId;
    }

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

    public String getPublishTopicName() {
        return publishTopicName;
    }

    public void setPublishTopicName(String publishTopicName) {
        this.publishTopicName = publishTopicName;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }

    public Integer getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(Integer videoWidth) {
        this.videoWidth = videoWidth;
    }

    public Integer getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(Integer videoHeight) {
        this.videoHeight = videoHeight;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsMine() {
        return isMine;
    }

    public void setIsMine(Boolean mine) {
        isMine = mine;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean liked) {
        isLiked = liked;
    }

    public Boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(Boolean collected) {
        isCollected = collected;
    }

    public Boolean getIsFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(Boolean followed) {
        isFollowed = followed;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }
}
