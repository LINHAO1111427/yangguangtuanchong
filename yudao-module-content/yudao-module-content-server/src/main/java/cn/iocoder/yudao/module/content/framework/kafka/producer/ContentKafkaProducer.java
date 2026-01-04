package cn.iocoder.yudao.module.content.framework.kafka.producer;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.content.framework.kafka.message.ContentInteractionMessage;
import cn.iocoder.yudao.module.content.framework.kafka.message.UserBehaviorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.module.content.constants.ContentConstants.KafkaTopic.*;

/**
 * 内容模块 Kafka 生产者
 *
 * @author 阳光团宠
 */
@Slf4j
@Service
public class ContentKafkaProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送用户行为消息
     *
     * @param message 用户行为消息
     */
    public void sendUserBehavior(UserBehaviorMessage message) {
        String jsonMessage = JSONUtil.toJsonStr(message);
        String key = message.getUserId() + "_" + message.getPostId();

        CompletableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(USER_BEHAVIOR, key, jsonMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Send user behavior message success: topic={}, key={}, offset={}",
                    USER_BEHAVIOR, key, result.getRecordMetadata().offset());
            } else {
                log.error("Send user behavior message failed: topic={}, key={}, message={}",
                    USER_BEHAVIOR, key, jsonMessage, ex);
            }
        });
    }

    /**
     * 发送内容互动消息
     *
     * @param message 内容互动消息
     */
    public void sendContentInteraction(ContentInteractionMessage message) {
        String jsonMessage = JSONUtil.toJsonStr(message);
        String key = message.getPostId() + "_" + message.getUserId();

        CompletableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(CONTENT_INTERACTION, key, jsonMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Send content interaction message success: topic={}, key={}, offset={}",
                    CONTENT_INTERACTION, key, result.getRecordMetadata().offset());
            } else {
                log.error("Send content interaction message failed: topic={}, key={}, message={}",
                    CONTENT_INTERACTION, key, jsonMessage, ex);
            }
        });
    }

    /**
     * 发送内容举报消息
     *
     * @param postId 内容ID
     * @param userId 用户ID
     * @param reason 举报原因
     */
    public void sendContentReport(Long postId, Long userId, String reason) {
        String jsonMessage = String.format("{\"postId\":%d,\"userId\":%d,\"reason\":\"%s\",\"eventTime\":\"%s\"}",
            postId, userId, reason, java.time.LocalDateTime.now());
        String key = postId + "_" + userId;

        CompletableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(CONTENT_REPORT, key, jsonMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Send content report message success: postId={}, userId={}, offset={}",
                    postId, userId, result.getRecordMetadata().offset());
            } else {
                log.error("Send content report message failed: postId={}, userId={}, reason={}",
                    postId, userId, reason, ex);
            }
        });
    }

    /**
     * 发送消息模块用的行为事件（用于生成通知）
     *
     * Topic: behavior-event（由 message-server 消费）
     */
    public void sendBehaviorEvent(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return;
        }
        String jsonMessage = JSONUtil.toJsonStr(payload);
        String key = String.valueOf(payload.getOrDefault("actorUserId", payload.getOrDefault("userId", "")));
        kafkaTemplate.send("behavior-event", key, jsonMessage);
    }
}
