package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;

import java.util.List;

/**
 * 会话Service接口
 *
 * @author xiaolvshu
 */
public interface ConversationService {

    /**
     * 分页获取用户的会话列表
     *
     * @param userId 用户ID
     * @param reqVO  分页参数
     * @return 会话分页
     */
    PageResult<ConversationDO> getConversationPage(Long userId, PageParam reqVO);

    /**
     * 获取用户的会话列�?
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationDO> getConversationList(Long userId);

    /**
     * 获取会话详情
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话DO
     */
    ConversationDO getConversation(Long conversationId, Long userId);

    /**
     * 根据用户与对端查询会话
     *
     * @param userId   用户ID
     * @param targetId 对端用户ID
     * @return 会话
     */
    ConversationDO getConversationByTarget(Long userId, Long targetId);

    /**
     * 清空会话未读�?
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    void clearUnreadCount(Long conversationId, Long userId);

    /**
     * 置顶/取消置顶会话
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @param isTop          是否置顶(0-�?1-�?
     */
    void toggleTop(Long conversationId, Long userId, Integer isTop);

    /**
     * 设置/取消免打�?
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @param isMute         是否免打�?0-�?1-�?
     */
    void toggleMute(Long conversationId, Long userId, Integer isMute);

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    void deleteConversation(Long conversationId, Long userId);

    /**
     * 获取用户的总未读数
     *
     * @param userId 用户ID
     * @return 总未读数
     */
    Long getTotalUnreadCount(Long userId);

}
