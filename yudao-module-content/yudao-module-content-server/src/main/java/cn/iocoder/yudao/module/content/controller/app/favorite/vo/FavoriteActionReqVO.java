package cn.iocoder.yudao.module.content.controller.app.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "APP - 收藏操作请求")
public class FavoriteActionReqVO {

    @Schema(description = "内容ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @Schema(description = "分组ID，空则落入默认分组")
    private Long groupId;

    @Schema(description = "自定义标签")
    private List<String> tags;

    @Schema(description = "备注")
    private String note;

    @Schema(description = "来源标识")
    private Integer source;

    @Schema(description = "扩展信息")
    private Map<String, Object> extra;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
