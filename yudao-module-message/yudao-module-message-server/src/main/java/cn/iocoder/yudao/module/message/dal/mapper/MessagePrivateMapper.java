package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessagePrivateMapper extends BaseMapperX<MessagePrivateDO> {

    default PageResult<MessagePrivateDO> selectConversationPage(Long userId1, Long userId2, PageParam pageParam) {
        return selectPage(pageParam, new QueryWrapperX<MessagePrivateDO>()
                .and(wrapper -> wrapper
                        .nested(w -> w.eq("from_user_id", userId1).eq("to_user_id", userId2))
                        .or(w -> w.eq("from_user_id", userId2).eq("to_user_id", userId1)))
                .ne("deleted", 3)
                .orderByDesc("create_time"));
    }

    default Long selectUnreadCount(Long userId) {
        return selectCount(new QueryWrapperX<MessagePrivateDO>()
                .eq("to_user_id", userId)
                .eq("status", 0)
                .ne("deleted", 2)
                .ne("deleted", 3));
    }

    default void updateStatusToRead(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId) {
        UpdateWrapper<MessagePrivateDO> wrapper = new UpdateWrapper<>();
        wrapper.in("id", messageIds)
                .eq("to_user_id", userId)
                .set("status", 1)
                .set("read_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void updateStatusToRecall(Long messageId, Long userId) {
        UpdateWrapper<MessagePrivateDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId)
                .eq("from_user_id", userId)
                .set("status", 2);
        update(null, wrapper);
    }

    default void markAsDeleted(Long messageId, Long userId) {
        MessagePrivateDO message = selectById(messageId);
        if (message == null) {
            return;
        }
        Integer newDeleted;
        if (userId.equals(message.getFromUserId())) {
            newDeleted = message.getDeleted() != null && message.getDeleted() == 2 ? 3 : 1;
        } else {
            newDeleted = message.getDeleted() != null && message.getDeleted() == 1 ? 3 : 2;
        }
        UpdateWrapper<MessagePrivateDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId)
                .set("deleted", newDeleted);
        update(null, wrapper);
    }
}
