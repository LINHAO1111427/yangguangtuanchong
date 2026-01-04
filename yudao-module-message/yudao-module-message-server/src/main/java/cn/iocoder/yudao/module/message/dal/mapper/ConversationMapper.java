package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话 Mapper
 *
 * @author xiaolvshu
 */
@Mapper
public interface ConversationMapper extends BaseMapperX<ConversationDO> {

    /**
     * 分页查询用户会话
     */
    default PageResult<ConversationDO> selectConversationPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new QueryWrapperX<ConversationDO>()
                .eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("is_top")
                .orderByDesc("update_time"));
    }

    /**
     * 查询用户的会话列�?
     */
    default List<ConversationDO> selectUserConversations(Long userId) {
        return selectList(new QueryWrapperX<ConversationDO>()
                .eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("is_top")
                .orderByDesc("update_time"));
    }

    /**
     * 查询或创建会�?
     */
    default ConversationDO selectOrCreate(Long userId, Long targetId, Integer type) {
        ConversationDO conversation = selectByUserAndTarget(userId, targetId, type);
        if (conversation == null) {
            conversation = new ConversationDO();
            conversation.setUserId(userId);
            conversation.setTargetId(targetId);
            conversation.setType(type);
            conversation.setUnreadCount(0);
            conversation.setIsTop(0);
            conversation.setIsMute(0);
            conversation.setDeleted(0);
            insert(conversation);
        }
        return conversation;
    }

    /**
     * 根据用户+对端查询会话
     */
    default ConversationDO selectByUserAndTarget(Long userId, Long targetId, Integer type) {
        return selectOne(new QueryWrapperX<ConversationDO>()
                .eq("user_id", userId)
                .eq("target_id", targetId)
                .eq("type", type)
                .eq("deleted", 0));
    }

    /**
     * 更新会话的最后一条消�?
     */
    default void updateLastMessage(Long conversationId, Long messageId, String content) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .set("last_message_id", messageId)
                .set("last_message_content", content)
                .set("last_message_time", LocalDateTime.now())
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 增加未读�?
     */
    default void incrementUnreadCount(Long conversationId) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .setSql("unread_count = unread_count + 1")
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 清空未读�?
     */
    default void clearUnreadCount(Long conversationId) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .set("unread_count", 0)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 置顶/取消置顶会话
     */
    default void updateTop(Long conversationId, Integer isTop) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .set("is_top", isTop)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 设置/取消免打�?
     */
    default void updateMute(Long conversationId, Integer isMute) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .set("is_mute", isMute)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 删除会话（软删�?
     */
    default void markAsDeleted(Long conversationId) {
        UpdateWrapper<ConversationDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", conversationId)
                .set("deleted", 1)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

    /**
     * 查询用户的总未读数
     */
    default Long selectTotalUnreadCount(Long userId) {
        return selectList(new QueryWrapperX<ConversationDO>()
                .eq("user_id", userId)
                .eq("deleted", 0)
                .gt("unread_count", 0))
                .stream()
                .mapToLong(ConversationDO::getUnreadCount)
                .sum();
    }

}
