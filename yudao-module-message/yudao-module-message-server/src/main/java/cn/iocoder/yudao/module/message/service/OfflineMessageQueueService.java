package cn.iocoder.yudao.module.message.service;

import java.util.List;

/**
 * WebSocket 离线消息队列（Redis List）
 */
public interface OfflineMessageQueueService {

    void enqueue(Long userId, String payload);

    List<String> drain(Long userId, int maxSize);
}

