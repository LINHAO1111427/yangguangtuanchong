package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import cn.iocoder.yudao.module.message.dal.mapper.ConversationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.CONVERSATION_MUTE_INVALID;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.CONVERSATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.CONVERSATION_PERMISSION_DENIED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.CONVERSATION_TOP_INVALID;

/**
 * 会话Service实现
 *
 * @author xiaolvshu
 */
@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public PageResult<ConversationDO> getConversationPage(Long userId, PageParam reqVO) {
        return conversationMapper.selectConversationPage(userId, reqVO);
    }

    @Override
    public List<ConversationDO> getConversationList(Long userId) {
        return conversationMapper.selectUserConversations(userId);
    }

    @Override
    public ConversationDO getConversation(Long conversationId, Long userId) {
        ConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw ServiceExceptionUtil.exception(CONVERSATION_NOT_EXISTS);
        }

        // 验证会话所有权
        if (!conversation.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(CONVERSATION_PERMISSION_DENIED);
        }

        return conversation;
    }

    @Override
    public ConversationDO getConversationByTarget(Long userId, Long targetId) {
        return conversationMapper.selectByUserAndTarget(userId, targetId, 1);
    }

    @Override
    public void clearUnreadCount(Long conversationId, Long userId) {
        // 验证会话所有权
        ConversationDO conversation = getConversation(conversationId, userId);

        conversationMapper.clearUnreadCount(conversationId);

        log.info("清空会话未读数: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    public void toggleTop(Long conversationId, Long userId, Integer isTop) {
        // 验证会话所有权
        ConversationDO conversation = getConversation(conversationId, userId);

        // 验证参数
        if (isTop != 0 && isTop != 1) {
            throw ServiceExceptionUtil.exception(CONVERSATION_TOP_INVALID);
        }

        conversationMapper.updateTop(conversationId, isTop);

        log.info("{}会话: conversationId={}, userId={}",
                isTop == 1 ? "置顶" : "取消置顶", conversationId, userId);
    }

    @Override
    public void toggleMute(Long conversationId, Long userId, Integer isMute) {
        // 验证会话所有权
        ConversationDO conversation = getConversation(conversationId, userId);

        // 验证参数
        if (isMute != 0 && isMute != 1) {
            throw ServiceExceptionUtil.exception(CONVERSATION_MUTE_INVALID);
        }

        conversationMapper.updateMute(conversationId, isMute);

        log.info("{}会话免打扰: conversationId={}, userId={}",
                isMute == 1 ? "开启" : "关闭", conversationId, userId);
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        // 验证会话所有权
        ConversationDO conversation = getConversation(conversationId, userId);

        conversationMapper.markAsDeleted(conversationId);

        log.info("删除会话: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    public Long getTotalUnreadCount(Long userId) {
        return conversationMapper.selectTotalUnreadCount(userId);
    }
}

