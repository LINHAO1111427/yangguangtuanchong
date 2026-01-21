package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapperX<NotificationDO> {

    default PageResult<NotificationDO> selectNotificationPage(Long userId, Integer type, PageParam pageParam) {
        return selectPage(pageParam, new QueryWrapperX<NotificationDO>()
                .eq("user_id", userId)
                .eqIfPresent("type", type)
                .eq("deleted", 0)
                .orderByDesc("create_time"));
    }

    default void markAsRead(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        UpdateWrapper<NotificationDO> wrapper = new UpdateWrapper<>();
        wrapper.in("id", notificationIds)
                .eq("user_id", userId)
                .set("is_read", 1)
                .set("read_time", LocalDateTime.now())
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void markAllAsRead(Long userId, Integer type) {
        UpdateWrapper<NotificationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0);
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.set("is_read", 1)
                .set("read_time", LocalDateTime.now())
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void markAsDeleted(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        UpdateWrapper<NotificationDO> wrapper = new UpdateWrapper<>();
        wrapper.in("id", notificationIds)
                .eq("user_id", userId)
                .set("deleted", 1)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void markAsDeletedByAdmin(Long notificationId) {
        UpdateWrapper<NotificationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", notificationId)
                .set("deleted", 1)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void clearAll(Long userId, Integer type) {
        UpdateWrapper<NotificationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.set("deleted", 1)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default Long selectUnreadCount(Long userId, Integer type) {
        return selectCount(new QueryWrapperX<NotificationDO>()
                .eq("user_id", userId)
                .eq("is_read", 0)
                .eq("deleted", 0)
                .eqIfPresent("type", type));
    }
}
