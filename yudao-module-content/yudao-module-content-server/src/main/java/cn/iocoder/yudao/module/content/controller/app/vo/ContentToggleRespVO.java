package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Content like/collect toggle response")
public class ContentToggleRespVO {

    @Schema(description = "Whether liked after toggle")
    private Boolean liked;

    @Schema(description = "Latest like count")
    private Integer likeCount;

    @Schema(description = "Whether collected after toggle")
    private Boolean collected;

    @Schema(description = "Latest collect count")
    private Integer collectCount;

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }
}
