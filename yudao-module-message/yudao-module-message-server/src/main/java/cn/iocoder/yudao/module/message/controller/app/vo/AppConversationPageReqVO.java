package cn.iocoder.yudao.module.message.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话分页请求 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 会话分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConversationPageReqVO extends PageParam {

    private static final long serialVersionUID = 1L;

}
