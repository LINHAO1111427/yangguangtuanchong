package cn.iocoder.yudao.module.content.controller.app.feed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "APP - 快速收藏请求")
public class QuickCollectReqVO {

    @Schema(description = "内容ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @Schema(description = "收藏分组ID，可为空")
    private Long groupId;

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
