package cn.iocoder.yudao.module.content.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Admin - content comment audit request")
@Data
public class ContentCommentAuditReqVO {

    @Schema(description = "Comment ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ID cannot be null")
    private Long id;

    @Schema(description = "Audit status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Audit status cannot be null")
    private Integer auditStatus;

    @Schema(description = "Audit remark")
    private String auditRemark;
}
