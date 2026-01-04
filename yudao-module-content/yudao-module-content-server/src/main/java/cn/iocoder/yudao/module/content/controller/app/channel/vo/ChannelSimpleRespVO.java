package cn.iocoder.yudao.module.content.controller.app.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP - 频道信息")
public class ChannelSimpleRespVO {

    @Schema(description = "频道ID", example = "1")
    private Long id;

    @Schema(description = "频道编码", example = "travel")
    private String code;

    @Schema(description = "频道名称", example = "旅行")
    private String name;

    @Schema(description = "频道描述")
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "主题色")
    private String color;

    @Schema(description = "是否默认频道：0-否 1-是")
    private Integer isDefault;

    @Schema(description = "是否必选频道：0-否 1-是")
    private Integer isRequired;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }
}
