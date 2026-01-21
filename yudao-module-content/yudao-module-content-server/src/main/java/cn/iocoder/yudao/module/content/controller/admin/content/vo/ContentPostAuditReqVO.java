package cn.iocoder.yudao.module.content.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Admin - content post audit request")
@Data
public class ContentPostAuditReqVO {

    @Schema(description = "Content ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ID cannot be null")
    private Long id;

    @Schema(description = "Audit status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Audit status cannot be null")
    private Integer auditStatus;

    @Schema(description = "Audit remark", example = "Approved")
    private String auditRemark;
}
