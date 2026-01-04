package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Map;

/**
 * Request body used by the app client when creating a piece of content.
 *
 * <p>Fields are intentionally lightweight and only keep the data that the current
 * backend modules consume. The original source text contained unreadable
 * characters, so the class is rewritten from scratch using plain English.</p>
 */
@Schema(description = "APP content creation request")
public class ContentCreateReqVO {

    @Schema(description = "User ID (populated by the backend)", hidden = true)
    private Long userId;

    @Schema(description = "Content type: 1=image+text, 2=video, 3=audio, 4=text only",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Content type is required")
    private Integer contentType;

    @Schema(description = "Title", requiredMode = Schema.RequiredMode.REQUIRED, example = "Share my day")
    @NotBlank(message = "Title is required")
    @Length(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Schema(description = "Content body", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Today I tried a new recipe...")
    @NotBlank(message = "Content body is required")
    @Length(max = 2000, message = "Content must be at most 2000 characters")
    private String content;

    @Schema(description = "Topic ID that the content belongs to", example = "1")
    private Long publishTopicId;

    @Schema(description = "Image URLs attached to the content")
    private List<String> images;

    @Schema(description = "Video playback URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Video cover image URL", example = "https://example.com/cover.jpg")
    private String videoCover;

    @Schema(description = "Video duration in seconds", example = "180")
    private Integer videoDuration;

    @Schema(description = "Video width in pixels", example = "1080")
    private Integer videoWidth;

    @Schema(description = "Video height in pixels", example = "1920")
    private Integer videoHeight;

    @Schema(description = "Video file size in bytes", example = "104857600")
    private Long videoFileSize;

    @Schema(description = "Video format", example = "mp4")
    private String videoFormat;

    @Schema(description = "Video quality: 1=SD, 2=HD, 3=Full HD, 4=4K", example = "2")
    private Integer videoQuality;

    @Schema(description = "Audio duration in seconds", example = "60")
    private Integer audioDuration;

    @Schema(description = "Whether the content is public: 0=private, 1=public", example = "1")
    private Integer isPublic = 1;

    @Schema(description = "Publish status: 0=draft, 1=published", example = "1")
    private Integer status = 1;

    @Schema(description = "Whether comments are allowed: 0=no, 1=yes", example = "1")
    private Integer allowComment = 1;

    @Schema(description = "Whether the post is anonymous: 0=real name, 1=anonymous", example = "0")
    private Integer isAnonymous = 0;

    @Schema(description = "Location metadata (latitude/longitude/address)")
    private Map<String, Object> location;

    @Schema(description = "Tag list associated with the content")
    private List<String> tags;

    @Schema(description = "Additional metadata for the content")
    private Map<String, Object> extra;

    @Schema(description = "Client IP (populated by the backend)", hidden = true)
    private String ipAddress;

    @Schema(description = "User-Agent header (populated by the backend)", hidden = true)
    private String userAgent;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getPublishTopicId() {
        return publishTopicId;
    }

    public void setPublishTopicId(Long publishTopicId) {
        this.publishTopicId = publishTopicId;
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

    public Long getVideoFileSize() {
        return videoFileSize;
    }

    public void setVideoFileSize(Long videoFileSize) {
        this.videoFileSize = videoFileSize;
    }

    public String getVideoFormat() {
        return videoFormat;
    }

    public void setVideoFormat(String videoFormat) {
        this.videoFormat = videoFormat;
    }

    public Integer getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(Integer videoQuality) {
        this.videoQuality = videoQuality;
    }

    public Integer getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(Integer audioDuration) {
        this.audioDuration = audioDuration;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAllowComment() {
        return allowComment;
    }

    public void setAllowComment(Integer allowComment) {
        this.allowComment = allowComment;
    }

    public Integer getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Integer isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
