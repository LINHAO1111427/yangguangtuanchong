package cn.iocoder.yudao.module.message.controller.app.vo.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 腾讯语音 / 视频集成状态返回
 *
 * @author Lin
 */
@Data
@Schema(description = "APP - 腾讯语音/视频集成状态响应")
public class AppTencentIntegrationStatusRespVO {

    @Schema(description = "Tencent RTC 是否启用")
    private boolean rtcEnabled;

    @Schema(description = "Tencent 语音转写是否启用")
    private boolean voiceEnabled;

    @Schema(description = "语音识别 Region")
    private String voiceRegion;
}

