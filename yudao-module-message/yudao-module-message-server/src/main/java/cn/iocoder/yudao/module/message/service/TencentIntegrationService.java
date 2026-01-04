package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.message.framework.tencent.TencentIntegrationProperties;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * 腾讯语音 / 视频集成 Service
 *
 * @author Lin
 */
@Slf4j
@Service
public class TencentIntegrationService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Resource
    private TencentIntegrationProperties properties;

    /**
     * 腾讯实时音视频是否可用
     */
    public boolean isRtcEnabled() {
        TencentIntegrationProperties.Rtc rtc = properties.getRtc();
        return rtc.isEnabled()
                && StrUtil.isNotBlank(rtc.getSdkAppId())
                && StrUtil.isNotBlank(rtc.getSecretKey());
    }

    /**
     * 腾讯语音转写是否可用
     */
    public boolean isVoiceEnabled() {
        TencentIntegrationProperties.Voice voice = properties.getVoice();
        return voice.isEnabled()
                && StrUtil.isNotBlank(voice.getAppId())
                && StrUtil.isNotBlank(voice.getSecretKey())
                && StrUtil.isNotBlank(voice.getSecretId());
    }

    /**
     * 获取语音识别所属区域
     */
    public String getVoiceRegion() {
        return properties.getVoice().getRegion();
    }

    /**
     * 生成腾讯实时音视频 UserSig
     *
     * @param userId 当前用户
     * @param roomId 房间号
     * @return 签名信息
     */
    public Optional<RtcSignature> generateRtcSignature(Long userId, String roomId) {
        if (userId == null || StrUtil.isBlank(roomId)) {
            log.warn("[TencentRTC] 无法生成签名，userId={} roomId={}", userId, roomId);
            return Optional.empty();
        }
        if (!isRtcEnabled()) {
            log.info("[TencentRTC] 功能未启用，跳过签名生成");
            return Optional.empty();
        }
        try {
            long nowSeconds = System.currentTimeMillis() / 1000;
            long expireSeconds = nowSeconds + resolveSignatureTtlSeconds();
            String nonce = IdUtil.fastSimpleUUID();
            String payload = userId + ":" + roomId + ":" + expireSeconds + ":" + nonce;
            String signature = sign(payload, properties.getRtc().getSecretKey());
            return Optional.of(new RtcSignature(
                    properties.getRtc().getSdkAppId(),
                    userId,
                    roomId,
                    nonce,
                    expireSeconds,
                    signature
            ));
        } catch (GeneralSecurityException ex) {
            log.error("[TencentRTC] 生成签名失败 userId={} roomId={}", userId, roomId, ex);
            return Optional.empty();
        }
    }

    private long resolveSignatureTtlSeconds() {
        Duration ttl = properties.getRtc().getSignatureTtl();
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return Duration.ofHours(1).getSeconds();
        }
        return ttl.getSeconds();
    }

    private String sign(String payload, String secretKey) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    /**
     * 签名结果
     */
    @Data
    @AllArgsConstructor
    public static class RtcSignature {
        private String sdkAppId;
        private Long userId;
        private String roomId;
        private String nonce;
        private Long expireTimestamp;
        private String signature;
    }
}

