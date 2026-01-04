package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;
import cn.iocoder.yudao.module.message.constants.MessageConstants;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import cn.iocoder.yudao.module.message.dal.mapper.ConversationMapper;
import cn.iocoder.yudao.module.message.dal.mapper.MessagePrivateMapper;
import cn.iocoder.yudao.module.message.kafka.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.*;

/**
 * 消息Service实现
 *
 * @author xiaolvshu
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessagePrivateMapper messagePrivateMapper;

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private MessageProducer messageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendPrivateMessage(Long fromUserId, MessageSendReqVO reqVO) {
        Long senderId = fromUserId != null ? fromUserId : SecurityFrameworkUtils.getLoginUserId();
        if (senderId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        validateSendReq(reqVO);

        MessagePrivateDO message = new MessagePrivateDO();
        message.setFromUserId(senderId);
        message.setToUserId(reqVO.getToUserId());
        message.setType(reqVO.getType());
        message.setContent(reqVO.getContent());
        message.setExtraData(buildExtraData(reqVO));
        message.setStatus(0);
        message.setDeleted(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        messagePrivateMapper.insert(message);

        log.info("发送私聊消息成功 messageId={}, from={}, to={}", message.getId(), senderId, reqVO.getToUserId());

        updateConversation(senderId, reqVO.getToUserId(), message, false);
        updateConversation(reqVO.getToUserId(), senderId, message, true);

        sendToKafka(message);

        return message.getId();
    }

    /**
     * 更新会话信息
     */
    private void updateConversation(Long userId, Long targetId, MessagePrivateDO message, boolean incrementUnread) {
        ConversationDO conversation = conversationMapper.selectOrCreate(userId, targetId, 1);

        // 更新最后一条消息
        String content = getMessageSummary(message);
        conversationMapper.updateLastMessage(conversation.getId(), message.getId(), content);

        // 增加未读数(接收者)
        if (incrementUnread) {
            conversationMapper.incrementUnreadCount(conversation.getId());
        }
    }

    /**
     * 获取消息摘要(用于会话列表显示)
     */
    private String getMessageSummary(MessagePrivateDO message) {
        switch (message.getType()) {
            case MessageConstants.ChatMessageType.TEXT:
                return StrUtil.maxLength(message.getContent(), 50);
            case MessageConstants.ChatMessageType.IMAGE:
                return "[图片]";
            case MessageConstants.ChatMessageType.VIDEO:
                return "[视频]";
            case MessageConstants.ChatMessageType.GIF:
                return "[动图]";
            case MessageConstants.ChatMessageType.VOICE:
                return "[语音]";
            case MessageConstants.ChatMessageType.FILE:
                return "[文件]";
            case MessageConstants.ChatMessageType.SHARE_CARD:
                return "[卡片]";
            default:
                return "[消息]";
        }
    }

    private void validateSendReq(MessageSendReqVO reqVO) {
        Integer type = reqVO.getType();
        if (type == null) {
            throw ServiceExceptionUtil.exception(PRIVATE_MESSAGE_TYPE_INVALID);
        }
        if (MessageConstants.ChatMessageType.TEXT == type) {
            if (StrUtil.isBlank(reqVO.getContent())) {
                throw ServiceExceptionUtil.exception(PRIVATE_MESSAGE_CONTENT_REQUIRED);
            }
            return;
        }
        if (MessageConstants.ChatMessageType.SHARE_CARD == type) {
            if (reqVO.getCardContentId() == null && StrUtil.isBlank(reqVO.getExtraData())) {
                throw ServiceExceptionUtil.exception(PRIVATE_MESSAGE_CARD_REQUIRED);
            }
            return;
        }
        if (CollUtil.isEmpty(reqVO.getMediaUrls()) && StrUtil.isBlank(reqVO.getExtraData())) {
            throw ServiceExceptionUtil.exception(PRIVATE_MESSAGE_MEDIA_REQUIRED);
        }
    }

    private String buildExtraData(MessageSendReqVO reqVO) {
        Map<String, Object> merged = new HashMap<>();
        if (StrUtil.isNotBlank(reqVO.getExtraData())) {
            try {
                Map<String, Object> origin = JsonUtils.parseObject(reqVO.getExtraData(), Map.class);
                if (origin != null) {
                    merged.putAll(origin);
                }
            } catch (Exception ignored) {
                // keep raw extraData if invalid json
            }
        }
        if (CollUtil.isNotEmpty(reqVO.getMediaUrls())) {
            merged.put("mediaUrls", reqVO.getMediaUrls());
        }
        if (reqVO.getCardContentId() != null) {
            merged.put("cardContentId", reqVO.getCardContentId());
        }
        if (merged.isEmpty()) {
            return reqVO.getExtraData();
        }
        return JsonUtils.toJsonString(merged);
    }

    /**
     * 发送消息到Kafka
     */
    private void sendToKafka(MessagePrivateDO message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("messageId", message.getId());
            data.put("fromUserId", message.getFromUserId());
            data.put("toUserId", message.getToUserId());
            data.put("type", message.getType());
            data.put("content", message.getContent());
            data.put("extraData", message.getExtraData());
            data.put("createTime", message.getCreateTime().toString());

            messageProducer.sendPrivateMessage(data);
        } catch (Exception e) {
            log.error("发送消息到Kafka失败: messageId={}", message.getId(), e);
        }
    }

    @Override
    public PageResult<MessagePrivateDO> getConversationMessages(Long userId, Long targetId, PageParam reqVO) {
        PageParam pageParam = reqVO != null ? reqVO : new PageParam();
        return messagePrivateMapper.selectConversationPage(userId, targetId, pageParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        // 更新消息状态为已读
        messagePrivateMapper.updateStatusToRead(messageIds, userId);

        // 清空对应会话的未读数
        // 查询第一条消息，确定对方用户ID
        if (!messageIds.isEmpty()) {
            MessagePrivateDO firstMessage = messagePrivateMapper.selectById(messageIds.get(0));
            if (firstMessage != null) {
                // 确定对方用户ID（接收到的消息来自fromUserId）
                Long targetUserId = firstMessage.getFromUserId().equals(userId)
                    ? firstMessage.getToUserId()
                    : firstMessage.getFromUserId();

                // 查找对应的会话并清空未读数
                ConversationDO conversation = conversationMapper.selectOrCreate(userId, targetUserId, 1);
                if (conversation != null && conversation.getUnreadCount() > 0) {
                    conversationMapper.clearUnreadCount(conversation.getId());
                    log.info("清空会话未读数: userId={}, targetUserId={}, conversationId={}",
                            userId, targetUserId, conversation.getId());
                }
            }
        }

        log.info("标记消息已读: userId={}, count={}", userId, messageIds.size());
    }

    @Override
    public void recallMessage(Long messageId, Long userId) {
        MessagePrivateDO message = messagePrivateMapper.selectById(messageId);
        if (message == null) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }

        // 只能撤回自己发送的消息
        if (!message.getFromUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(MESSAGE_RECALL_ONLY_SELF);
        }

        // 只能撤回2分钟内的消息
        if (message.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw ServiceExceptionUtil.exception(MESSAGE_RECALL_TIMEOUT);
        }

        messagePrivateMapper.updateStatusToRecall(messageId, userId);

        log.info("撤回消息: messageId={}, userId={}", messageId, userId);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        MessagePrivateDO message = messagePrivateMapper.selectById(messageId);
        if (message == null) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }
        if (!Objects.equals(message.getFromUserId(), userId)
                && !Objects.equals(message.getToUserId(), userId)) {
            throw ServiceExceptionUtil.exception(CONVERSATION_PERMISSION_DENIED);
        }
        messagePrivateMapper.markAsDeleted(messageId, userId);
        log.info("删除消息: messageId={}, userId={}", messageId, userId);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return messagePrivateMapper.selectUnreadCount(userId);
    }

    @Override
    public MessagePrivateDO getMessage(Long messageId) {
        return messagePrivateMapper.selectById(messageId);
    }
}
