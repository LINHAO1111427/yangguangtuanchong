package cn.iocoder.yudao.module.content.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comment page request")
public class CommentPageReqVO extends PageParam {

    @Schema(description = "Content id that owns the comments")
    private Long contentId;

    @Schema(description = "Root comment id to query replies for")
    private Long rootId;

    @Schema(description = "Sort type hot/latest", example = "latest")
    private String sort;

    @Schema(description = "Include reply preview when rootId is empty")
    private Boolean withReplies;

    @Schema(description = "Max replies to preview under a root comment")
    private Integer replyPreviewSize;

    @Schema(description = "Query current user's comments")
    private Boolean mine;

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Boolean getWithReplies() {
        return withReplies;
    }

    public void setWithReplies(Boolean withReplies) {
        this.withReplies = withReplies;
    }

    public Integer getReplyPreviewSize() {
        return replyPreviewSize;
    }

    public void setReplyPreviewSize(Integer replyPreviewSize) {
        this.replyPreviewSize = replyPreviewSize;
    }

    public Boolean getMine() {
        return mine;
    }

    public void setMine(Boolean mine) {
        this.mine = mine;
    }
}
