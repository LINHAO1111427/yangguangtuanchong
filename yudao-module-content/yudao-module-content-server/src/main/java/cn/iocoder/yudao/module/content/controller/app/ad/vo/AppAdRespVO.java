package cn.iocoder.yudao.module.content.controller.app.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP - 广告信息")
public class AppAdRespVO {

    @Schema(description = "广告ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题/描述")
    private String desc;

    @Schema(description = "封面图片")
    private String image;

    @Schema(description = "跳转链接")
    private String link;

    @Schema(description = "展示场景")
    private Integer scene;

    @Schema(description = "排序优先级")
    private Integer priority;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
