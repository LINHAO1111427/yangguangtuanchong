package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Comment create request")
public class CommentCreateReqVO {

    @NotNull(message = "Content id is required")
    private Long contentId;

    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment length can not exceed 500 characters")
    private String content;

    @Schema(description = "Parent comment id, empty means top level")
    private Long parentId;

    @Schema(description = "User id being replied to")
    private Long replyUserId;

    @Schema(description = "Attachment list (image/video/gif urls)")
    @Size(max = 9, message = "Attachments can not exceed 9")
    private List<String> images;

    @Schema(description = "Whether comment is anonymous")
    private Boolean anonymous;

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(Long replyUserId) {
        this.replyUserId = replyUserId;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }
}
