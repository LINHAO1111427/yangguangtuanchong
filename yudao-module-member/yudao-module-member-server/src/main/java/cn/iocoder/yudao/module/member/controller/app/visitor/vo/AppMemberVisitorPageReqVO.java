package cn.iocoder.yudao.module.member.controller.app.visitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "App visitor log page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppMemberVisitorPageReqVO extends PageParam {

    @Schema(description = "Query as visitor (true: I visited others)")
    private Boolean asVisitor;

    @Schema(description = "Visit type (1=profile, 2=content)")
    private Integer visitType;
}
