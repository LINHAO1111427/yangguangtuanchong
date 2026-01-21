package cn.iocoder.yudao.module.content.controller.admin.ad.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin - content ad page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentAdPageReqVO extends PageParam {

    @Schema(description = "Title keyword", example = "Banner")
    private String title;

    @Schema(description = "Advertiser name", example = "Brand")
    private String advertiserName;

    @Schema(description = "Status", example = "1")
    private Integer status;

    @Schema(description = "Display scene", example = "1")
    private Integer displayScene;

    @Schema(description = "Create time range")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
