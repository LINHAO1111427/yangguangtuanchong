package cn.iocoder.yudao.module.message.kafka;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.content.api.ContentApi;
import cn.iocoder.yudao.module.content.api.dto.ContentRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.constants.MessageConstants;
import cn.iocoder.yudao.module.message.service.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 消费行为事件（点赞/收藏/评论/关注），生成站内通知
 *
 * Topic: behavior-event
 *
 * Payload(JSON):
 * - behaviorType: like/collect/comment/follow
 * - action: add/cancel
 * - actorUserId
 * - contentId (like/collect/comment 必填)
 * - targetUserId (follow 必填；comment reply 可选，覆盖 content.owner)
 * - commentId/commentText/parentCommentId (comment 可选)
 */
@Component
@Slf4j
public class BehaviorEventKafkaConsumer {

    @Resource
    private NotificationService notificationService;
    @Resource
    private ContentApi contentApi;
    @Resource
    private MemberUserApi memberUserApi;

    @KafkaListener(topics = "behavior-event", groupId = "message-behavior-consumer",
            containerFactory = "stringKafkaListenerContainerFactory")
    public void onBehaviorEvent(ConsumerRecord<String, String> record) {
        Map<String, Object> payload = parsePayload(record.value());
        if (payload == null || payload.isEmpty()) {
            return;
        }

        String behaviorType = getString(payload, "behaviorType");
        String action = getString(payload, "action");
        Long actorUserId = getLong(payload, "actorUserId");
        if (actorUserId == null) {
            actorUserId = getLong(payload, "userId");
        }
        if (StrUtil.isBlank(behaviorType) || actorUserId == null) {
            return;
        }

        try {
            // 只处理 add（取消不生成通知）
            if (StrUtil.isNotBlank(action) && !"add".equalsIgnoreCase(action)) {
                return;
            }

            Long contentId = getLong(payload, "contentId");
            Long targetUserId = getLong(payload, "targetUserId");
            ContentRespDTO content = null;

            if (MessageConstants.BehaviorType.FOLLOW.equals(behaviorType)) {
                if (targetUserId == null) {
                    return;
                }
            } else {
                if (contentId == null) {
                    return;
                }
                if (targetUserId == null) {
                    try {
                        content = contentApi.getContent(contentId).getCheckedData();
                        if (content == null || content.getUserId() == null) {
                            return;
                        }
                        targetUserId = content.getUserId();
                    } catch (Exception ex) {
                        log.warn("Fetch content failed, skip notification. contentId={}", contentId, ex);
                        return;
                    }
                }
            }

            if (targetUserId == null || targetUserId.equals(actorUserId)) {
                return;
            }

            String actorName = "有人";
            try {
                Map<Long, MemberUserRespDTO> actorMap = memberUserApi.getUserMap(Set.of(actorUserId));
                MemberUserRespDTO actor = actorMap != null ? actorMap.get(actorUserId) : null;
                if (actor != null) {
                    actorName = StrUtil.blankToDefault(actor.getNickname(), actorName);
                }
            } catch (Exception ex) {
                log.warn("Fetch actor failed, fallback to default name. actorUserId={}", actorUserId, ex);
            }

            int notifyType = resolveNotifyType(behaviorType);
            String title = buildTitle(behaviorType);
            String contentText = buildContentText(behaviorType, actorName, getString(payload, "commentText"));

            Map<String, Object> related = new HashMap<>();
            related.put("behaviorType", behaviorType);
            related.put("action", "add");
            related.put("actorUserId", actorUserId);
            related.put("targetUserId", targetUserId);
            related.put("contentId", contentId);
            related.put("commentId", getLong(payload, "commentId"));
            related.put("parentCommentId", getLong(payload, "parentCommentId"));
            related.put("commentText", getString(payload, "commentText"));

            String relatedData = JsonUtils.toJsonString(related);
            String link = MessageConstants.BehaviorType.FOLLOW.equals(behaviorType)
                    ? "/pages/user/profile?userId=" + actorUserId
                    : "/pages/content/news_view?id=" + contentId;

            notificationService.createNotification(targetUserId, notifyType, title, contentText, relatedData, link);
        } catch (Exception ex) {
            log.error("[onBehaviorEvent][payload={}] 处理异常", payload, ex);
        }
    }

    private Map<String, Object> parsePayload(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return JsonUtils.parseObjectQuietly(value, new TypeReference<Map<String, Object>>() {});
    }

    private int resolveNotifyType(String behaviorType) {
        return switch (behaviorType) {
            case MessageConstants.BehaviorType.LIKE, MessageConstants.BehaviorType.COLLECT -> MessageConstants.NotificationType.LIKE;
            case MessageConstants.BehaviorType.COMMENT -> MessageConstants.NotificationType.COMMENT;
            case MessageConstants.BehaviorType.FOLLOW -> MessageConstants.NotificationType.FOLLOW;
            default -> MessageConstants.NotificationType.SYSTEM;
        };
    }

    private String buildTitle(String behaviorType) {
        return switch (behaviorType) {
            case MessageConstants.BehaviorType.COMMENT -> "收到新评论";
            case MessageConstants.BehaviorType.FOLLOW -> "新增关注";
            case MessageConstants.BehaviorType.COLLECT -> "收到收藏";
            case MessageConstants.BehaviorType.LIKE -> "收到点赞";
            default -> "系统通知";
        };
    }

    private String buildContentText(String behaviorType, String actorName, String commentText) {
        if (MessageConstants.BehaviorType.COMMENT.equals(behaviorType)) {
            return StrUtil.maxLength(StrUtil.blankToDefault(commentText, ""), 80);
        }
        return actorName;
    }

    private Long getLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}
