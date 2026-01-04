package cn.iocoder.yudao.module.content.controller.app.vo.boost;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Boost configuration response")
public class ContentBoostConfigRespVO {

    @Schema(description = "Configuration id")
    private Long id;

    @Schema(description = "Display name")
    private String name;

    @Schema(description = "Description")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
