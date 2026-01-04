package cn.iocoder.yudao.module.message.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import jakarta.annotation.Resource;
import java.util.Arrays;

/**
 * WebSocket 配置
 *
 * @author Lin
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(prefix = "yudao.websocket", name = "enable", havingValue = "true")
public class MessageWebSocketConfig implements WebSocketConfigurer {

    @Value("${message.websocket.path:/message/ws}")
    private String webSocketPath;

    @Resource
    private MessageWebSocketHandler messageWebSocketHandler;

    @Resource
    private WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] paths = Arrays.stream(webSocketPath.split(","))
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .toArray(String[]::new);
        if (paths.length == 0) {
            return;
        }
        registry.addHandler(messageWebSocketHandler, paths)
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
