package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.StrUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class OfflineMessageQueueServiceImpl implements OfflineMessageQueueService {

    private static final String KEY_PREFIX = "message:offline:";
    private static final int MAX_CACHE_SIZE = 500;
    private static final Duration TTL = Duration.ofDays(3);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void enqueue(Long userId, String payload) {
        if (userId == null || StrUtil.isBlank(payload)) {
            return;
        }
        String key = key(userId);
        stringRedisTemplate.opsForList().rightPush(key, payload);
        stringRedisTemplate.opsForList().trim(key, -MAX_CACHE_SIZE, -1);
        stringRedisTemplate.expire(key, TTL);
    }

    @Override
    public List<String> drain(Long userId, int maxSize) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String key = key(userId);
        Long size = stringRedisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return Collections.emptyList();
        }
        long end = Math.min(size, Math.max(maxSize, 1)) - 1;
        List<String> list = stringRedisTemplate.opsForList().range(key, 0, end);
        stringRedisTemplate.delete(key);
        return list != null ? list : Collections.emptyList();
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}

