package cn.iocoder.yudao.module.member.framework.kafka;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MemberBehaviorEventProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendFollowEvent(Long actorUserId, Long targetUserId) {
        if (actorUserId == null || targetUserId == null || actorUserId.equals(targetUserId)) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("behaviorType", "follow");
        payload.put("action", "add");
        payload.put("actorUserId", actorUserId);
        payload.put("targetUserId", targetUserId);
        payload.put("eventTime", LocalDateTime.now().toString());
        kafkaTemplate.send("behavior-event", actorUserId + "_" + targetUserId, JsonUtils.toJsonString(payload));
    }
}

