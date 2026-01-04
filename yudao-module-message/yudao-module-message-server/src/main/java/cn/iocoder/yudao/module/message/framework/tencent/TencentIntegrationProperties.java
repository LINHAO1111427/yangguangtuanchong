package cn.iocoder.yudao.module.message.framework.tencent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 腾讯语音 / 视频集成配置
 *
 * <p>可通过 thirdparty-config.*.yaml 进行配置，提供统一的开关与密钥管理。</p>
 *
 * @author Lin
 */
@Data
@Component
@ConfigurationProperties(prefix = "thirdparty.tencent")
public class TencentIntegrationProperties {

    /**
     * 实时音视频配置
     */
    private final Rtc rtc = new Rtc();

    /**
     * 语音转文字配置
     */
    private final Voice voice = new Voice();

    @Data
    public static class Rtc {

        /**
         * 是否启用腾讯实时音视频
         */
        private boolean enabled = false;

        /**
         * 腾讯云 SDKAppId
         */
        private String sdkAppId;

        /**
         * 用户签名生成密钥
         */
        private String secretKey;

        /**
         * 签名有效期，默认 2 小时
         */
        private Duration signatureTtl = Duration.ofHours(2);
    }

    @Data
    public static class Voice {

        /**
         * 是否启用腾讯语音转写
         */
        private boolean enabled = false;

        /**
         * 腾讯语音识别 AppId
         */
        private String appId;

        /**
         * 腾讯语音识别 SecretId
         */
        private String secretId;

        /**
         * 腾讯语音识别 SecretKey
         */
        private String secretKey;

        /**
         * 语音识别区域，默认 ap-shanghai
         */
        private String region = "ap-shanghai";
    }
}

