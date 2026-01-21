package cn.iocoder.yudao.module.member.controller.app.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "App visitor log response")
@Data
public class AppMemberVisitorLogRespVO {

    @Schema(description = "Log id")
    private Long id;

    @Schema(description = "Owner user id")
    private Long userId;

    @Schema(description = "Visitor user id")
    private Long visitorId;

    @Schema(description = "Visit type (1=profile, 2=content)")
    private Integer visitType;

    @Schema(description = "Target id")
    private Long targetId;

    @Schema(description = "Paid flag")
    private Boolean isPaid;

    @Schema(description = "Pay amount (cents)")
    private Integer payAmount;

    @Schema(description = "Visit time")
    private LocalDateTime createTime;

    @Schema(description = "Counterpart user id")
    private Long counterpartUserId;

    @Schema(description = "Counterpart nickname")
    private String counterpartNickname;

    @Schema(description = "Counterpart avatar")
    private String counterpartAvatar;
}
