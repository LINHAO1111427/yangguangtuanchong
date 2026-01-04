package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Map;

/**
 * Request body for updating an existing piece of content.
 */
@Schema(description = "APP content update request")
public class ContentUpdateReqVO {

    @Schema(description = "Primary key of the content", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Content id is required")
    private Long id;

    @Schema(description = "Owner user id (populated by the backend)", hidden = true)
    private Long userId;

    @Schema(description = "Content type: 1=image+text, 2=video, 3=audio, 4=text only", example = "1")
    private Integer contentType;

    @Schema(description = "Title", example = "Updated title")
    @Length(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Schema(description = "Content body", example = "Updated content text")
    @Length(max = 2000, message = "Content must be at most 2000 characters")
    private String content;

    @Schema(description = "Topic id that the content belongs to", example = "1")
    private Long publishTopicId;

    @Schema(description = "Image URL list")
    private List<String> images;

    @Schema(description = "Video URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Video cover URL")
    private String videoCover;

    @Schema(description = "Video duration (seconds)")
    private Integer videoDuration;

    @Schema(description = "Video width (pixels)")
    private Integer videoWidth;

    @Schema(description = "Video height (pixels)")
    private Integer videoHeight;

    @Schema(description = "Video file size (bytes)")
    private Long videoFileSize;

    @Schema(description = "Video format", example = "mp4")
    private String videoFormat;

    @Schema(description = "Video quality", example = "2")
    private Integer videoQuality;

    @Schema(description = "Audio duration (seconds)")
    private Integer audioDuration;

    @Schema(description = "Whether the content is public: 0=private, 1=public")
    private Integer isPublic;

    @Schema(description = "Publish status: 0=draft, 1=published")
    private Integer status;

    @Schema(description = "Whether comments are allowed: 0=no, 1=yes")
    private Integer allowComment;

    @Schema(description = "Whether the post is anonymous: 0=real name, 1=anonymous")
    private Integer isAnonymous;

    @Schema(description = "Location metadata")
    private Map<String, Object> location;

    @Schema(description = "Associated tag list")
    private List<String> tags;

    @Schema(description = "Extra metadata map")
    private Map<String, Object> extra;

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
}
