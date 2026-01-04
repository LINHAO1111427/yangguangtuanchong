package cn.iocoder.yudao.module.content.controller.app.vo.boost;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Boost start request")
public class ContentBoostStartReqVO {

    @NotNull(message = "Post id is required")
    private Long postId;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
