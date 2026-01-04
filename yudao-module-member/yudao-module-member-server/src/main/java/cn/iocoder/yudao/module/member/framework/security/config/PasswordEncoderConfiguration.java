package cn.iocoder.yudao.module.member.framework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 补充密码加密器配置,避免在未引入统一安全自动配置时缺失 {@link PasswordEncoder} Bean。
 *
 * @author Codex
 */
@Configuration
public class PasswordEncoderConfiguration {

    /**
     * 密码加密器 - 备用方案
     * 使用默认强度4,与框架配置保持一致
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

}
