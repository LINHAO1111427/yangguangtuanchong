package cn.iocoder.yudao.module.content.controller.app.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "APP - 收藏记录响应")
public class FavoriteRecordRespVO {

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "内容ID")
    private Long contentId;
    @Schema(description = "分组ID")
    private Long groupId;
    @Schema(description = "标签列表")
    private List<String> tags;
    @Schema(description = "备注")
    private String note;
    @Schema(description = "来源")
    private Integer source;
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
