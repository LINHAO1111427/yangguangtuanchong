package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;

import java.util.List;

/**
 * 消息Service接口
 *
 * @author xiaolvshu
 */
public interface MessageService {

    /**
     * 发送私聊消息
     *
     * @param reqVO 发送消息请求
     * @return 消息ID
     */
    Long sendPrivateMessage(Long fromUserId, MessageSendReqVO reqVO);
    /**
     * 发送私聊消息（默认使用当前登录用户作为发送者）
     *
     * @param reqVO 发送消息请求
     * @return 消息ID
     */
    default Long sendPrivateMessage(MessageSendReqVO reqVO) {
        return sendPrivateMessage(null, reqVO);
    }

    /**
     * 获取会话消息列表
     *
     * @param userId   当前用户ID
     * @param targetId 对方用户ID
     * @param reqVO    分页参数
     * @return 消息列表
     */
    PageResult<MessagePrivateDO> getConversationMessages(Long userId, Long targetId, PageParam reqVO);

    /**
     * 标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @param userId     用户ID
     */
    void markAsRead(List<Long> messageIds, Long userId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * 获取用户的未读消息数
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 根据ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息DO
     */
    MessagePrivateDO getMessage(Long messageId);
}
