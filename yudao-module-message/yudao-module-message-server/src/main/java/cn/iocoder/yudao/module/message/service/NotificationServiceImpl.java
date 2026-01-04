package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import cn.iocoder.yudao.module.message.dal.mapper.NotificationMapper;
import cn.iocoder.yudao.module.message.kafka.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.NOTIFICATION_CONTENT_REQUIRED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.NOTIFICATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.NOTIFICATION_PERMISSION_DENIED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.NOTIFICATION_TYPE_INVALID;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.NOTIFICATION_USER_REQUIRED;

/**
 * 系统通知Service实现
 *
 * @author xiaolvshu
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private MessageProducer messageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(Long userId, Integer type, String title, String content,
                                    String relatedData, String link) {
        // 参数校验
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        if (type == null || type < 1 || type > 5) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_TYPE_INVALID);
        }
        if (StrUtil.isBlank(content)) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_CONTENT_REQUIRED);
        }

        // 创建通知
        NotificationDO notification = new NotificationDO();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedData(relatedData);
        notification.setLink(link);
        notification.setIsRead(0);
        notification.setDeleted(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        notificationMapper.insert(notification);

        log.info("创建系统通知成功: notificationId={}, userId={}, type={}",
                notification.getId(), userId, type);

        // 发送到Kafka(用于推送给在线用户)
        sendToKafka(notification);

        return notification.getId();
    }

    @Override
    public PageResult<NotificationDO> getNotificationPage(Long userId, Integer type, PageParam reqVO) {
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        PageParam pageParam = reqVO != null ? reqVO : new PageParam();
        return notificationMapper.selectNotificationPage(userId, type, pageParam);
    }

    @Override
    public NotificationDO getNotification(Long notificationId, Long userId) {
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        NotificationDO notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_NOT_EXISTS);
        }

        // 验证通知所有权
        if (!notification.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_PERMISSION_DENIED);
        }

        return notification;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }

        notificationMapper.markAsRead(notificationIds, userId);

        log.info("标记通知已读: userId={}, count={}", userId, notificationIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId, Integer type) {
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        notificationMapper.markAllAsRead(userId, type);

        log.info("全部标记为已读: userId={}, type={}", userId, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotifications(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }

        notificationMapper.markAsDeleted(notificationIds, userId);

        log.info("删除通知: userId={}, count={}", userId, notificationIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAll(Long userId, Integer type) {
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        notificationMapper.clearAll(userId, type);

        log.info("清空所有通知: userId={}, type={}", userId, type);
    }

    @Override
    public Long getUnreadCount(Long userId, Integer type) {
        if (userId == null) {
            throw ServiceExceptionUtil.exception(NOTIFICATION_USER_REQUIRED);
        }
        return notificationMapper.selectUnreadCount(userId, type);
    }

    /**
     * 发送通知到Kafka
     */
    private void sendToKafka(NotificationDO notification) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("notificationId", notification.getId());
            data.put("userId", notification.getUserId());
            data.put("type", notification.getType());
            data.put("title", notification.getTitle());
            data.put("content", notification.getContent());
            data.put("relatedData", notification.getRelatedData());
            data.put("link", notification.getLink());
            data.put("createTime", notification.getCreateTime().toString());

            messageProducer.sendNotification(data);
        } catch (Exception e) {
            log.error("发送通知到Kafka失败: notificationId={}", notification.getId(), e);
        }
    }
}
