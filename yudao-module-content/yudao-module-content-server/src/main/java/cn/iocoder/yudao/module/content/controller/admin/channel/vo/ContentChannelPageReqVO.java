package cn.iocoder.yudao.module.content.controller.admin.channel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - content channel page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentChannelPageReqVO extends PageParam {

    @Schema(description = "Code keyword", example = "recommend")
    private String code;

    @Schema(description = "Name keyword", example = "Recommend")
    private String name;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Default flag", example = "1")
    private Integer isDefault;

    @Schema(description = "Required flag", example = "1")
    private Integer isRequired;
}
