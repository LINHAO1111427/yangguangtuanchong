package cn.iocoder.yudao.module.message.kafka;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationRespVO;
import cn.iocoder.yudao.module.message.convert.AppMessageConvert;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMemberDO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import cn.iocoder.yudao.module.message.dal.mapper.GroupMemberMapper;
import cn.iocoder.yudao.module.message.service.ConversationService;
import cn.iocoder.yudao.module.message.service.MessageService;
import cn.iocoder.yudao.module.message.service.NotificationService;
import cn.iocoder.yudao.module.message.websocket.MessageWebSocketHandler;
import cn.iocoder.yudao.module.message.websocket.MessageWebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kafka 消费者：用于推送消息/通知到 WebSocket
 *
 * @author Lin
 */
@Component
@ConditionalOnProperty(prefix = "yudao.websocket", name = "enable", havingValue = "true")
@Slf4j
public class MessageKafkaConsumer {

    @Resource
    private MessageService messageService;
    @Resource
    private ConversationService conversationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MessageWebSocketSessionManager sessionManager;
    @Resource
    private MessageWebSocketHandler messageWebSocketHandler;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @KafkaListener(topics = "message-private", groupId = "message-private-consumer")
    public void onPrivateMessage(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long messageId = getLong(payload, "messageId");
        if (messageId == null) {
            return;
        }
        try {
            MessagePrivateDO message = messageService.getMessage(messageId);
            if (message == null) {
                log.warn("[onPrivateMessage][messageId({}) 消息不存在]", messageId);
                return;
            }
            Set<Long> memberIds = Set.of(message.getFromUserId(), message.getToUserId());
            Map<Long, MemberUserRespDTO> memberMap = memberUserApi.getUserMap(memberIds);

            pushPrivateMessage(message, message.getFromUserId(), message.getToUserId(), memberMap);
            pushPrivateMessage(message, message.getToUserId(), message.getFromUserId(), memberMap);
        } catch (Exception ex) {
            log.error("[onPrivateMessage][payload={}] 处理异常", payload, ex);
        }
    }

    @KafkaListener(topics = "message-notification", groupId = "message-notification-consumer")
    public void onNotification(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long notificationId = getLong(payload, "notificationId");
        Long userId = getLong(payload, "userId");
        if (notificationId == null || userId == null) {
            return;
        }
        try {
            NotificationDO notification = notificationService.getNotification(notificationId, userId);
            if (notification == null) {
                return;
            }
            AppNotificationRespVO respVO = AppMessageConvert.buildNotification(notification);
            pushToUser(userId, "notification", respVO);
        } catch (Exception ex) {
            log.error("[onNotification][payload={}] 处理异常", payload, ex);
        }
    }

    @KafkaListener(topics = "message-group", groupId = "message-group-consumer")
    public void onGroupMessage(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long groupId = getLong(payload, "groupId");
        Long messageId = getLong(payload, "messageId");
        Long fromUserId = getLong(payload, "fromUserId");
        if (groupId == null || messageId == null) {
            return;
        }

        try {
            List<GroupMemberDO> members = groupMemberMapper.selectListByGroupId(groupId);
            if (members.isEmpty()) {
                return;
            }

            Set<Long> memberIds = members.stream().map(GroupMemberDO::getUserId).collect(Collectors.toSet());

            String senderNickname = payload != null && payload.get("senderNickname") != null
                    ? String.valueOf(payload.get("senderNickname")) : null;
            String senderAvatar = null;
            if (fromUserId != null) {
                try {
                    MemberUserRespDTO sender = memberUserApi.getUser(fromUserId).getCheckedData();
                    if (sender != null) {
                        if (senderNickname == null || senderNickname.isEmpty()) {
                            senderNickname = sender.getNickname();
                        }
                        senderAvatar = sender.getAvatar();
                    }
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> message = Map.of(
                    "id", messageId,
                    "groupId", groupId,
                    "fromUserId", fromUserId,
                    "senderNickname", senderNickname,
                    "senderAvatar", senderAvatar,
                    "type", payload != null ? payload.get("type") : null,
                    "content", payload != null ? payload.get("content") : null,
                    "extraData", payload != null ? payload.get("extraData") : null,
                    "createTime", payload != null ? payload.get("createTime") : null
            );
            Map<String, Object> data = Map.of(
                    "groupId", groupId,
                    "message", message
            );

            for (Long userId : memberIds) {
                pushToUser(userId, "group_message", data);
            }
        } catch (Exception ex) {
            log.error("[onGroupMessage][payload={}] 处理异常", payload, ex);
        }
    }

    private void pushPrivateMessage(MessagePrivateDO message, Long userId, Long targetUserId,
                                    Map<Long, MemberUserRespDTO> memberMap) {
        ConversationDO conversation = conversationService.getConversationByTarget(userId, targetUserId);
        if (conversation == null) {
            log.warn("[pushPrivateMessage][userId={}, targetId={}] 会话不存在", userId, targetUserId);
            return;
        }
        AppConversationRespVO conversationVO = AppMessageConvert.buildConversation(conversation,
                memberMap.get(targetUserId),
                sessionManager.isUserOnline(targetUserId));
        AppMessageRespVO messageVO = AppMessageConvert.buildMessage(message, conversation, memberMap, userId);
        AppMessagePackageRespVO packageRespVO = AppMessageConvert.buildPackage(conversationVO, messageVO);
        pushToUser(userId, "private_message", packageRespVO);
    }

    private void pushToUser(Long userId, String type, Object data) {
        if (userId == null || data == null) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "type", type,
                "data", data
        );
        messageWebSocketHandler.pushMessageToUser(userId, JsonUtils.toJsonString(payload));
    }

    private Long getLong(Map<String, Object> payload, String key) {
        Object value = payload != null ? payload.get(key) : null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String str && !str.isEmpty()) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

}
