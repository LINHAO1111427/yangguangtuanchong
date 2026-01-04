package cn.iocoder.yudao.module.message.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知分页请求 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 通知分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppNotificationPageReqVO extends PageParam {

    private static final long serialVersionUID = 1L;

    @Schema(description = "通知类型 1-点赞 2-评论 3-关注 4-系统 5-审核", example = "1")
    private Integer type;

}
