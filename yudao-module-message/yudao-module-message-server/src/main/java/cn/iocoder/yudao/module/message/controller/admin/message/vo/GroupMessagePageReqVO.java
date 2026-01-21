package cn.iocoder.yudao.module.message.controller.admin.message.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin - group message page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMessagePageReqVO extends PageParam {

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "Sender user ID", example = "1024")
    private Long fromUserId;

    @Schema(description = "Message type", example = "1")
    private Integer type;

    @Schema(description = "Message status", example = "0")
    private Integer status;

    @Schema(description = "Delete status", example = "0")
    private Integer deleted;

    @Schema(description = "Create time range")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
