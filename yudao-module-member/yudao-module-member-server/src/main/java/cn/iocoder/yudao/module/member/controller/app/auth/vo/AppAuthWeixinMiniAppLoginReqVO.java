package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.member.enums.MemberDeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "用户 APP - 微信小程序手机登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniAppLoginReqVO {

    @Schema(description = "手机 code，小程序通过 wx.getPhoneNumber 方法获得", requiredMode = Schema.RequiredMode.REQUIRED, example = "hello")
    @NotEmpty(message = "手机 code 不能为空")
    private String phoneCode;

    @Schema(description = "登录 code，小程序通过 wx.login 方法获得", requiredMode = Schema.RequiredMode.REQUIRED, example = "word")
    @NotEmpty(message = "登录 code 不能为空")
    private String loginCode;

    @Schema(description = "state", requiredMode = Schema.RequiredMode.REQUIRED, example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    @NotEmpty(message = "state 不能为空")
    private String state;

    // ========== 设备信息（用于登录设备限制） ==========

    @Schema(description = "设备类型：1=手机 2=平板", example = "1")
    @InEnum(MemberDeviceTypeEnum.class)
    private Integer deviceType;

    @Schema(description = "设备唯一标识（前端生成并持久化）", example = "d_abc123")
    private String deviceId;

    @Schema(description = "设备名称/型号", example = "iPhone 15 Pro")
    private String deviceName;

    @Schema(description = "操作系统名称", example = "iOS")
    private String osName;

    @Schema(description = "操作系统版本", example = "17.0")
    private String osVersion;

    @Schema(description = "App 版本", example = "1.0.0")
    private String appVersion;

}
