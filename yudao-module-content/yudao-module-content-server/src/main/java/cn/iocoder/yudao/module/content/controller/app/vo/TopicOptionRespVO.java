package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Topic option response")
public class TopicOptionRespVO {

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
