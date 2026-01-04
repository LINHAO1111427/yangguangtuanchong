package cn.iocoder.yudao.module.message.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;
import cn.iocoder.yudao.module.message.service.MessageService;
import cn.iocoder.yudao.module.message.service.AppMessageService;
import cn.iocoder.yudao.module.message.service.OfflineMessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WebSocket消息处理器
 *
 * @author xiaolvshu
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "yudao.websocket", name = "enable", havingValue = "true")
public class MessageWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private MessageWebSocketSessionManager sessionManager;

    @Resource
    private MessageService messageService;

    @Resource
    private AppMessageService appMessageService;

    @Resource
    private OfflineMessageQueueService offlineMessageQueueService;

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从attributes中获取userId
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket连接建立失败: userId为空, sessionId={}", session.getId());
            session.close();
            return;
        }

        // 添加到SessionManager
        sessionManager.addSession(userId, session);

        // 发送连接成功消息
        sendMessage(session, createSystemMessage("连接成功", "你已成功连接到消息服务器"));

        // 发送离线缓存的消息（按产生顺序）
        for (String offline : offlineMessageQueueService.drain(userId, 200)) {
            try {
                sendMessage(session, offline);
            } catch (IOException e) {
                log.warn("发送离线消息失败: userId={}, sessionId={}", userId, session.getId(), e);
                break;
            }
        }

        log.info("WebSocket连接已建立: userId={}, sessionId={}, remoteAddress={}",
                userId, session.getId(), session.getRemoteAddress());
    }

    /**
     * 接收到客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);

            // 解析消息
            JSONObject json = JSONUtil.parseObj(payload);
            String type = json.getStr("type", "ping");

            switch (type) {
                case "ping":
                    // 心跳消息
                    handlePing(session);
                    break;

                case "message":
                    // 聊天消息
                    handleChatMessage(session, json);
                    break;

                case "read":
                    // 已读回执
                    handleReadReceipt(session, json);
                    break;

                default:
                    log.warn("未知的消息类型: type={}, sessionId={}", type, session.getId());
                    sendError(session, "未知的消息类型");
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}", session.getId(), e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session);

        Long userId = sessionManager.getUserId(session.getId());
        log.info("WebSocket连接已关闭: userId={}, sessionId={}, status={}",
                userId, session.getId(), status);
    }

    /**
     * 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
        sessionManager.removeSession(session);
    }

    // ==================== 消息处理方法 ====================

    /**
     * 处理心跳消息
     */
    private void handlePing(WebSocketSession session) throws IOException {
        sendMessage(session, createSystemMessage("pong", "心跳响应"));
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, JSONObject json) {
        Long userId = sessionManager.getUserId(session.getId());
        if (userId == null) {
            log.warn("用户ID为空,无法处理聊天消息: sessionId={}", session.getId());
            sendError(session, "用户未登录");
            return;
        }

        try {
            // 1. 从json中提取消息内容
            Long toUserId = json.getLong("toUserId");
            Integer messageType = json.getInt("messageType", 1);
            String content = json.getStr("content");
            String extraData = json.getStr("extraData");
            Long cardContentId = json.getLong("cardContentId");

            List<String> mediaUrls = new ArrayList<>();
            Object mediaObj = json.get("mediaUrls");
            if (mediaObj instanceof List<?>) {
                for (Object item : (List<?>) mediaObj) {
                    if (item != null) {
                        mediaUrls.add(String.valueOf(item));
                    }
                }
            } else if (mediaObj instanceof cn.hutool.json.JSONArray arr) {
                for (Object item : arr) {
                    if (item != null) {
                        mediaUrls.add(String.valueOf(item));
                    }
                }
            }

            // 参数校验
            if (toUserId == null) {
                sendError(session, "接收者ID不能为空");
                return;
            }
            if (messageType == 1 && (content == null || content.trim().isEmpty())) {
                sendError(session, "消息内容不能为空");
                return;
            }
            if (messageType != 1 && mediaUrls.isEmpty() && cardContentId == null
                    && (extraData == null || extraData.trim().isEmpty())) {
                sendError(session, "媒体或卡片内容不能为空");
                return;
            }

            // 2. 构建请求VO并调用MessageService保存消息
            MessageSendReqVO reqVO = new MessageSendReqVO();
            reqVO.setToUserId(toUserId);
            reqVO.setType(messageType);
            reqVO.setContent(content);
            reqVO.setMediaUrls(mediaUrls.isEmpty() ? null : mediaUrls);
            reqVO.setCardContentId(cardContentId);
            reqVO.setExtraData(extraData);

            // 3. 保存消息（会自动通过Kafka发送）并返回发送结果
            AppMessagePackageRespVO respVO = appMessageService.sendPrivateMessage(userId, reqVO);
            Map<String, Object> response = Map.of(
                    "type", "private_message_ack",
                    "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "data", respVO
            );
            sendMessage(session, JsonUtils.toJsonString(response));

            log.info("聊天消息处理成功: userId={}, toUserId={}", userId, toUserId);

        } catch (Exception e) {
            log.error("处理聊天消息失败: userId={}", userId, e);
            sendError(session, "消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理已读回执
     */
    private void handleReadReceipt(WebSocketSession session, JSONObject json) {
        Long userId = sessionManager.getUserId(session.getId());
        if (userId == null) {
            log.warn("用户ID为空,无法处理已读回执: sessionId={}", session.getId());
            sendError(session, "用户未登录");
            return;
        }

        try {
            // 获取消息ID列表
            Object messageIdsObj = json.get("messageIds");
            List<Long> messageIds = new ArrayList<>();

            if (messageIdsObj instanceof List) {
                for (Object id : (List<?>) messageIdsObj) {
                    if (id instanceof Number) {
                        messageIds.add(((Number) id).longValue());
                    }
                }
            } else if (messageIdsObj instanceof Number) {
                // 兼容单个消息ID的情况
                messageIds.add(((Number) messageIdsObj).longValue());
            }

            if (messageIds.isEmpty()) {
                sendError(session, "消息ID不能为空");
                return;
            }

            // 调用MessageService标记消息已读
            messageService.markAsRead(messageIds, userId);

            // 返回成功响应
            JSONObject response = new JSONObject();
            response.put("type", "read_receipt_success");
            response.put("count", messageIds.size());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            sendMessage(session, response.toString());

            log.info("已读回执处理成功: userId={}, count={}", userId, messageIds.size());

        } catch (Exception e) {
            log.error("处理已读回执失败: userId={}", userId, e);
            sendError(session, "已读回执处理失败: " + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 发送消息到指定Session
     */
    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String error) {
        try {
            String message = createErrorMessage(error);
            sendMessage(session, message);
        } catch (IOException e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 创建系统消息JSON
     */
    private String createSystemMessage(String type, String content) {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("content", content);
        json.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return json.toString();
    }

    /**
     * 创建错误消息JSON
     */
    private String createErrorMessage(String error) {
        JSONObject json = new JSONObject();
        json.put("type", "error");
        json.put("error", error);
        json.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return json.toString();
    }

    /**
     * 向指定用户推送消息
     *
     * @param userId  用户ID
     * @param message 消息内容(JSON格式)
     */
    public void pushMessageToUser(Long userId, String message) {
        var sessions = sessionManager.getUserSessions(userId);
        if (sessions.isEmpty()) {
            log.debug("用户不在线,无法推送消息: userId={}", userId);
            offlineMessageQueueService.enqueue(userId, message);
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (WebSocketSession session : sessions) {
            try {
                sendMessage(session, message);
                successCount++;
            } catch (IOException e) {
                log.error("推送消息失败: userId={}, sessionId={}", userId, session.getId(), e);
                failCount++;
            }
        }

        if (successCount == 0 && failCount > 0) {
            offlineMessageQueueService.enqueue(userId, message);
        }

        log.info("消息推送完成: userId={}, 成功={}, 失败={}", userId, successCount, failCount);
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(String message) {
        var onlineUserIds = sessionManager.getOnlineUserIds();
        log.info("开始广播消息: 在线用户数={}", onlineUserIds.size());

        for (Long userId : onlineUserIds) {
            pushMessageToUser(userId, message);
        }
    }
}
