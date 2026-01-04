package cn.iocoder.yudao.module.message.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息 VO
 *
 * @author Lin
 */
@Schema(description = "APP - 用户简要信息 VO")
@Data
public class AppMemberSimpleRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户编号", example = "2048")
    private Long id;

    @Schema(description = "昵称", example = "小绿薯")
    private String nickname;

    @Schema(description = "头像", example = "https://static.xiaolvshu.cn/avatar.png")
    private String avatar;

}
