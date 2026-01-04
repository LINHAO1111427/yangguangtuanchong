package cn.iocoder.yudao.module.content.controller.app.bbs.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP - 论坛话题")
public class AppBbsTopicRespVO {

    @Schema(description = "话题ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "封面图")
    private String image;

    @Schema(description = "参与人数")
    private Integer participants;

    @Schema(description = "帖子数量")
    private Integer posts;

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

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }
}
