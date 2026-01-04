package cn.iocoder.yudao.module.message.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Message 模块的配置类
 *
 * @author xiaolvshu
 */
@Configuration
public class SecurityConfiguration {

    /**
     * 提供一个空的 AuthorizeRequestsCustomizer 列表
     * message 模块暂时不需要自定义权限映射
     */
    @Bean
    public List<AuthorizeRequestsCustomizer> authorizeRequestsCustomizers() {
        return Collections.emptyList();
    }

    /**
     * 提供一个Mock的Knife4jProperties，避免框架中的Knife4jOpenApiCustomizer启动失败
     * message模块禁用了swagger，不需要真实的Knife4j配置
     */
    @Bean
    @ConditionalOnMissingBean
    public Knife4jProperties knife4jProperties() {
        return new Knife4jProperties();
    }
}
