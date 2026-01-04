package cn.iocoder.yudao.module.message.kafka;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Resource;

/**
 * Kafka 消息生产者
 *
 * <p>统一封装消息发送逻辑，提供私信、群聊、通知等多种 Topic 的发送入口，便于后续切换 MQ 实现。</p>
 *
 * @author xiaolvshu
 */
@Slf4j
@Component
public class MessageProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送私聊消息
     *
     * @param message 消息体
     */
    public void sendPrivateMessage(Object message) {
        sendMessage("message-private", message);
    }

    /**
     * 发送群聊消息
     *
     * @param message 消息体
     */
    public void sendGroupMessage(Object message) {
        sendMessage("message-group", message);
    }

    /**
     * 发送系统通知
     *
     * @param notification 通知内容
     */
    public void sendNotification(Object notification) {
        sendMessage("message-notification", notification);
    }

    /**
     * 发送已读回执
     *
     * @param receipt 回执数据
     */
    public void sendReadReceipt(Object receipt) {
        sendMessage("message-read-receipt", receipt);
    }

    /**
     * 发送消息到指定分区
     *
     * @param topic     Topic 名称
     * @param partition 分区
     * @param key       消息 Key
     * @param message   消息体
     */
    public void sendMessageToPartition(String topic, Integer partition, String key, Object message) {
        try {
            String messageJson = JSONUtil.toJsonStr(message);
            log.info("发送 Kafka 消息到指定分区: topic={}, partition={}, key={}, message={}",
                    topic, partition, key, messageJson);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, partition, key, message);
            addLoggingCallback(topic, messageJson, future);
        } catch (Exception ex) {
            log.error("发送 Kafka 消息到指定分区失败: topic={}", topic, ex);
            throw new RuntimeException("Kafka 消息发送失败", ex);
        }
    }

    /**
     * 通用发送逻辑
     */
    private void sendMessage(String topic, Object message) {
        try {
            String messageJson = JSONUtil.toJsonStr(message);
            log.info("发送 Kafka 消息: topic={}, message={}", topic, messageJson);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);
            addLoggingCallback(topic, messageJson, future);
        } catch (Exception ex) {
            log.error("发送 Kafka 消息失败: topic={}", topic, ex);
            throw new RuntimeException("Kafka 消息发送失败", ex);
        }
    }

    private void addLoggingCallback(String topic, String payload, CompletableFuture<SendResult<String, Object>> future) {
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka 消息发送失败: topic={}, message={}", topic, payload, ex);
                return;
            }
            if (result != null && result.getRecordMetadata() != null) {
                log.info("Kafka 消息发送成功: topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.info("Kafka 消息发送成功: topic={}, payload={}", topic, payload);
            }
        });
    }
}
