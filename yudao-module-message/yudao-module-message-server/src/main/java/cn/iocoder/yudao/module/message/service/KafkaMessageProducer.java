package cn.iocoder.yudao.module.message.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Kafka producer wrapper with graceful degradation.
 */
@Component
@Slf4j
public class KafkaMessageProducer {

    @Getter
    @RequiredArgsConstructor
    public enum Topic {
        USER_BEHAVIOR("xiaolvshu-user-behavior"),
        CONTENT_INTERACTION("xiaolvshu-content-interaction"),
        CONTENT_REPORT("xiaolvshu-content-report");

        private final String topicName;
    }

    @Resource
    @Nullable
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAsync(Topic topic, String key, Object payload) {
        if (kafkaTemplate == null) {
            log.debug("[KafkaMock] topic={}, key={}, payload={}", topic.getTopicName(), key, payload);
            return;
        }
        kafkaTemplate.send(topic.getTopicName(), key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Send kafka message failed: topic={}, key={}", topic.getTopicName(), key, ex);
                    } else if (log.isDebugEnabled()) {
                        log.debug("Kafka message sent: topic={}, key={}, partition={}, offset={}",
                                topic.getTopicName(),
                                key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void sendContentInteraction(Long userId, Long contentId, String behaviorType, String action) {
        Map<String, Object> message = new HashMap<>(8);
        message.put("userId", userId);
        message.put("contentId", contentId);
        message.put("behaviorType", behaviorType);
        message.put("action", action);
        message.put("timestamp", System.currentTimeMillis());
        sendAsync(Topic.CONTENT_INTERACTION, buildKey(contentId, userId), message);
    }

    private String buildKey(Long contentId, Long userId) {
        if (contentId != null) {
            return String.valueOf(contentId);
        }
        if (userId != null) {
            return "user:" + userId;
        }
        return "anonymous";
    }
}

