package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;

import java.util.List;

/**
 * 系统通知Service接口
 *
 * @author xiaolvshu
 */
public interface NotificationService {

    /**
     * 创建系统通知
     *
     * @param userId      用户ID
     * @param type        通知类型(1-点赞 2-评论 3-关注 4-系统公告 5-审核通知)
     * @param title       标题
     * @param content     内容
     * @param relatedData 关联数据(JSON)
     * @param link        跳转链接
     * @return 通知ID
     */
    Long createNotification(Long userId, Integer type, String title, String content, String relatedData, String link);

    /**
     * 获取用户的通知列表
     *
     * @param userId 用户ID
     * @param type   通知类型(可�?
     * @param reqVO  分页参数
     * @return 通知列表
     */
    PageResult<NotificationDO> getNotificationPage(Long userId, Integer type, PageParam reqVO);

    /**
     * 获取通知详情
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 通知DO
     */
    NotificationDO getNotification(Long notificationId, Long userId);

    /**
     * 标记通知为已�?
     *
     * @param notificationIds 通知ID列表
     * @param userId          用户ID
     */
    void markAsRead(List<Long> notificationIds, Long userId);

    /**
     * 全部标记为已�?
     *
     * @param userId 用户ID
     * @param type   通知类型(可�?
     */
    void markAllAsRead(Long userId, Integer type);

    /**
     * 删除通知
     *
     * @param notificationIds 通知ID列表
     * @param userId          用户ID
     */
    void deleteNotifications(List<Long> notificationIds, Long userId);

    /**
     * 清空所有通知
     *
     * @param userId 用户ID
     * @param type   通知类型(可�?
     */
    void clearAll(Long userId, Integer type);

    /**
     * 获取未读通知�?
     *
     * @param userId 用户ID
     * @param type   通知类型(可�?
     * @return 未读数量
     */
    Long getUnreadCount(Long userId, Integer type);

}
