package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Admin - content ad status update request")
@Data
public class ContentAdUpdateStatusReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ID cannot be null")
    private Long id;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status cannot be null")
    private Integer status;
}
