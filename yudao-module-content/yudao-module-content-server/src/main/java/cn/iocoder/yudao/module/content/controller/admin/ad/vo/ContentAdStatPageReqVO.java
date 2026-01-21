package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin - content ad stat page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentAdStatPageReqVO extends PageParam {

    @Schema(description = "Ad ID", example = "1")
    private Long adId;

    @Schema(description = "Scene", example = "1")
    private Integer scene;

    @Schema(description = "Start time")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "End time")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;
}
