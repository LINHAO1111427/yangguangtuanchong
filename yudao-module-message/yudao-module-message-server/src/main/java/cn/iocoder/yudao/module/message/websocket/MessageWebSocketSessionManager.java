package cn.iocoder.yudao.module.message.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket Session 管理组件
 *
 * <p>注意：命名为 {@code MessageWebSocketSessionManager}，避免覆盖
 * 框架层 {@code webSocketSessionManager} Bean。</p>
 *
 * @author xiaolvshu
 */
@Slf4j
@Component
public class MessageWebSocketSessionManager {

    /**
     * userId -> Sessions
     */
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> userId
     */
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionUserMap.put(session.getId(), userId);
        log.info("用户 WebSocket 连接建立: userId={}, sessionId={}, 在线设备数={}",
                userId, session.getId(), userSessions.get(userId).size());
    }

    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("用户 WebSocket 连接断开: userId={}, sessionId={}, 剩余在线设备数={}",
                    userId, sessionId, sessions != null ? sessions.size() : 0);
        }
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            log.error("关闭 WebSocket Session 失败: sessionId={}", sessionId, e);
        }
    }

    public Set<WebSocketSession> getUserSessions(Long userId) {
        return userSessions.getOrDefault(userId, ConcurrentHashMap.newKeySet());
    }

    public Long getUserId(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    public boolean isUserOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public int getOnlineUserCount() {
        return userSessions.size();
    }

    public Set<Long> getOnlineUserIds() {
        return userSessions.keySet();
    }

    public int getUserDeviceCount(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    public void cleanClosedSessions() {
        int cleanedCount = 0;
        for (Map.Entry<Long, Set<WebSocketSession>> entry : userSessions.entrySet()) {
            Set<WebSocketSession> sessions = entry.getValue();
            Set<WebSocketSession> closedSessions = sessions.stream()
                    .filter(session -> !session.isOpen())
                    .collect(Collectors.toSet());
            for (WebSocketSession session : closedSessions) {
                sessions.remove(session);
                sessionUserMap.remove(session.getId());
                cleanedCount++;
            }
            if (sessions.isEmpty()) {
                userSessions.remove(entry.getKey());
            }
        }
        if (cleanedCount > 0) {
            log.info("清理已关闭 WebSocket Session: 数量={}, 当前在线用户数={}",
                    cleanedCount, getOnlineUserCount());
        }
    }

    public void closeUserSessions(Long userId) {
        Set<WebSocketSession> sessions = userSessions.remove(userId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                sessionUserMap.remove(session.getId());
                try {
                    if (session.isOpen()) {
                        session.close();
                    }
                } catch (IOException e) {
                    log.error("关闭用户 WebSocket Session 失败: userId={}, sessionId={}",
                            userId, session.getId(), e);
                }
            }
            log.info("用户所有 WebSocket 连接已关闭: userId={}, 数量={}", userId, sessions.size());
        }
    }

    public String getStatistics() {
        return String.format("在线用户数: %d, 总连接数: %d",
                getOnlineUserCount(),
                sessionUserMap.size());
    }
}
