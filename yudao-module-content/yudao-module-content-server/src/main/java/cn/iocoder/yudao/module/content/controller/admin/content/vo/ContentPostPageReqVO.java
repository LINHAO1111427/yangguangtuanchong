package cn.iocoder.yudao.module.content.controller.admin.content.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin - content post page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentPostPageReqVO extends PageParam {

    @Schema(description = "Keyword", example = "travel")
    private String keyword;

    @Schema(description = "Title keyword", example = "note")
    private String title;

    @Schema(description = "User ID", example = "1024")
    private Long userId;

    @Schema(description = "Content type", example = "1")
    private Integer contentType;

    @Schema(description = "Audit status", example = "1")
    private Integer auditStatus;

    @Schema(description = "Publish status", example = "1")
    private Integer status;

    @Schema(description = "Public flag", example = "1")
    private Integer isPublic;

    @Schema(description = "Top flag", example = "0")
    private Integer isTop;

    @Schema(description = "Hot flag", example = "0")
    private Integer isHot;

    @Schema(description = "Recommend flag", example = "0")
    private Integer isRecommend;

    @Schema(description = "Channel ID", example = "1")
    private Long channelId;

    @Schema(description = "Topic ID", example = "1")
    private Long publishTopicId;

    @Schema(description = "Create time range")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
