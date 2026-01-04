package cn.iocoder.yudao.module.system.framework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 补充密码加密器配置，防止由于精简安全依赖导致缺少 {@link PasswordEncoder} Bean。
 */
@Configuration
public class PasswordEncoderConfiguration {

    /**
     * 兜底 PasswordEncoder，使用默认强度 4，与框架默认行为保持一致。
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

}
