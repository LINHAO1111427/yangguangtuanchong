package cn.iocoder.yudao.module.content.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Paging request used by multiple content queries.
 */
@Schema(description = "APP content paging request")
public class ContentPageReqVO extends PageParam {

    @Schema(description = "Keyword for fuzzy search")
    private String keyword;

    @Schema(description = "Topic id filter")
    private Long publishTopicId;

    @Schema(description = "Channel id filter")
    private Long channelId;

    @Schema(description = "User id filter (filled when querying self data)", hidden = true)
    private Long userId;

    @Schema(description = "Content type filter")
    private Integer contentType;

    @Schema(description = "Audit status filter")
    private Integer auditStatus;

    @Schema(description = "Publish status filter")
    private Integer status;

    @Schema(description = "Public flag filter")
    private Integer isPublic;

    @Schema(description = "Title fuzzy filter")
    private String title;

    @Schema(description = "Content fuzzy filter")
    private String content;

    @Schema(description = "Creation time start")
    private LocalDateTime createTimeStart;

    @Schema(description = "Creation time end")
    private LocalDateTime createTimeEnd;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
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

    public LocalDateTime getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(LocalDateTime createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public LocalDateTime getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(LocalDateTime createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }
}
