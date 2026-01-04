package cn.iocoder.yudao.module.content.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import cn.iocoder.yudao.module.content.enums.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Content module Security configuration
 */
@Configuration("contentSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("contentAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // Swagger API documentation
                registry.requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll();
                // Spring Boot Actuator endpoints
                registry.requestMatchers("/actuator").permitAll()
                        .requestMatchers("/actuator/**").permitAll();
                // Druid monitoring
                registry.requestMatchers("/druid/**").permitAll();
                // RPC internal security endpoints
                registry.requestMatchers(ApiConstants.PREFIX + "/**").permitAll();

                // App API - public content browsing (index, detail, search, etc.)
                String[] versionedPublicPaths = {
                        "/api/v1.0.1/content/publish/index",
                        "/api/v1.0.1/content/publish/detail",
                        "/api/v1.0.1/content/feed/stream",
                        "/api/v1.0.1/content/topic/**",
                        "/api/v1.0.1/content/search/**",
                        "/api/v1.0.1/content/media/**",
                        "/api/v1.0.1/content/comment/list",
                        "/api/v1.0.1/content/author/**",
                        "/api/v1.0.1/content/channel/board",
                        "/api/v1.0.1/bbs/**",
                        "/api/v1.0.1/ad/**"
                };
                registry.requestMatchers(versionedPublicPaths).permitAll();

                String[] appApiPublicPaths = {
                        "/app-api/content/publish/index",
                        "/app-api/content/publish/detail",
                        "/app-api/content/feed/stream",
                        "/app-api/content/topic/**",
                        "/app-api/content/search/**",
                        "/app-api/content/media/**",
                        "/app-api/content/comment/list",
                        "/app-api/content/author/**",
                        "/app-api/content/channel/board",
                        "/app-api/bbs/**",
                        "/app-api/ad/**"
                };
                registry.requestMatchers(appApiPublicPaths).permitAll();

                String[] directPublicPaths = {
                        "/content/publish/index",
                        "/content/publish/detail",
                        "/content/feed/stream",
                        "/content/topic/**",
                        "/content/search/**",
                        "/content/media/**",
                        "/content/comment/list",
                        "/content/author/**",
                        "/content/channel/board",
                        "/bbs/**",
                        "/ad/**"
                };
                registry.requestMatchers(directPublicPaths).permitAll();
            }

        };
    }

}
