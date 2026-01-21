package cn.iocoder.yudao.module.content.controller.admin.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin - content comment page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentCommentPageReqVO extends PageParam {

    @Schema(description = "Content ID", example = "1")
    private Long contentId;

    @Schema(description = "User ID", example = "1024")
    private Long userId;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Audit status", example = "1")
    private Integer auditStatus;

    @Schema(description = "Create time range")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
