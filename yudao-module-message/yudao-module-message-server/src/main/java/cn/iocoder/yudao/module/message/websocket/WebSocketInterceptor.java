package cn.iocoder.yudao.module.message.websocket;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket握手拦截器
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "yudao.websocket", name = "enable", havingValue = "true")
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;
    @Resource
    private SecurityProperties securityProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            if (!(request instanceof ServletServerHttpRequest servletRequest)) {
                log.warn("WebSocket握手失败: 不支持的请求类型, type={}", request.getClass().getName());
                return false;
            }
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = SecurityFrameworkUtils.obtainAuthorization(httpRequest,
                    securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
            if (StrUtil.isBlank(token)) {
                log.warn("WebSocket握手失败: 未提供token, URI={}", request.getURI());
                return false;
            }
            Long userId = authenticateToken(token);
            if (userId == null) {
                log.warn("WebSocket握手失败: token校验失败, URI={}", request.getURI());
                return false;
            }
            attributes.put("userId", userId);
            attributes.put("token", token);
            return true;
        } catch (Exception ex) {
            log.error("WebSocket握手异常", ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常", exception);
        }
    }

    private Long authenticateToken(String token) {
        try {
            OAuth2AccessTokenCheckRespDTO respDTO = oauth2TokenApi.checkAccessToken(token).getCheckedData();
            if (respDTO != null) {
                return respDTO.getUserId();
            }
        } catch (Exception ex) {
            log.warn("WebSocket token 验证异常", ex);
        }
        if (securityProperties.getMockEnable()
                && token.startsWith(securityProperties.getMockSecret())) {
            return Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        }
        return null;
    }
}
