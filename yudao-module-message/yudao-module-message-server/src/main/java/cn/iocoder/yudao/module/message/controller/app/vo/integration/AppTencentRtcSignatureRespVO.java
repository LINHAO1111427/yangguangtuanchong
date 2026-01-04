package cn.iocoder.yudao.module.message.controller.app.vo.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 腾讯实时音视频签名响应
 *
 * @author Lin
 */
@Data
@Schema(description = "APP - 腾讯 RTC 签名响应 VO")
public class AppTencentRtcSignatureRespVO {

    @Schema(description = "是否已启用腾讯 RTC 集成")
    private boolean enabled;

    @Schema(description = "Tencent SDKAppId")
    private String sdkAppId;

    @Schema(description = "签名对应的用户 ID")
    private Long userId;

    @Schema(description = "使用的房间号")
    private String roomId;

    @Schema(description = "随机串")
    private String nonce;

    @Schema(description = "签名内容(userSig)")
    private String userSig;

    @Schema(description = "签名过期时间戳（秒）")
    private Long expireTimestamp;
}

