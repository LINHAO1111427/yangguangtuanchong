package cn.iocoder.yudao.module.infra.framework.config;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback Web 配置，确保 WebProperties 始终可以注册成功。
 * <p>
 * 在个别环境下，Spring Cloud 的配置导入流程被提前终止时，
 * yudao-spring-boot-starter-web 的自动配置可能无法生效，导致上下文缺少 WebProperties Bean。
 * 通过在模块内显式开启 {@link EnableConfigurationProperties} 保证 WebProperties 兜底可用。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebProperties.class)
public class InfraWebPropertiesConfiguration {
}
