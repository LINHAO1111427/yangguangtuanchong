package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.api.ContentApi;
import cn.iocoder.yudao.module.content.api.dto.ContentRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;
import cn.iocoder.yudao.module.message.convert.AppMessageConvert;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import cn.iocoder.yudao.module.content.api.follow.FollowApi;
import cn.iocoder.yudao.module.message.websocket.MessageWebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.CONVERSATION_PERMISSION_DENIED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.MESSAGE_NOT_EXISTS;
import static java.util.function.Function.identity;

/**
 * APP 消息聚合 Service 实现
 *
 * @author Lin
 */
@Service
@Slf4j
public class AppMessageServiceImpl implements AppMessageService {

    @Resource
    private ConversationService conversationService;
    @Resource
    private MessageService messageService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ContentApi contentApi;
    @Resource
    private MessageWebSocketSessionManager sessionManager;
    @Resource
    private FollowApi followApi;

    @Override
    public PageResult<AppConversationRespVO> getConversationPage(Long userId, AppConversationPageReqVO reqVO) {
        PageResult<ConversationDO> page = conversationService.getConversationPage(userId, reqVO);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty();
        }
        Set<Long> targetIds = page.getList().stream()
                .map(ConversationDO::getTargetId)
                .collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberMap = loadMemberUserMap(targetIds);

        List<AppConversationRespVO> list = page.getList().stream()
                .map(conversation -> AppMessageConvert.buildConversation(conversation,
                        memberMap.get(conversation.getTargetId()),
                        sessionManager.isUserOnline(conversation.getTargetId())))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public AppConversationRespVO getConversation(Long conversationId, Long userId) {
        ConversationDO conversation = conversationService.getConversation(conversationId, userId);
        Map<Long, MemberUserRespDTO> memberMap = loadMemberUserMap(
                Collections.singleton(conversation.getTargetId()));
        return AppMessageConvert.buildConversation(conversation,
                memberMap.get(conversation.getTargetId()),
                sessionManager.isUserOnline(conversation.getTargetId()));
    }

    @Override
    public PageResult<AppMessageRespVO> getConversationMessages(Long userId, AppMessagePageReqVO reqVO) {
        ConversationDO conversation = conversationService.getConversationByTarget(userId, reqVO.getTargetUserId());
        if (conversation == null) {
            return PageResult.empty();
        }
        PageResult<MessagePrivateDO> pageResult =
                messageService.getConversationMessages(userId, reqVO.getTargetUserId(), reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Set<Long> memberIds = new HashSet<>();
        memberIds.add(reqVO.getTargetUserId());
        memberIds.add(userId);
        pageResult.getList().forEach(message -> {
            memberIds.add(message.getFromUserId());
            memberIds.add(message.getToUserId());
        });
        Map<Long, MemberUserRespDTO> memberMap = loadMemberUserMap(memberIds);

        List<AppMessageRespVO> list = pageResult.getList().stream()
                .map(message -> AppMessageConvert.buildMessage(message, conversation, memberMap, userId))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public AppMessagePackageRespVO sendPrivateMessage(Long userId, MessageSendReqVO reqVO) {
        Long targetUserId = reqVO.getToUserId();
        if (targetUserId != null && !Boolean.TRUE.equals(followApi.isFollowingUser(targetUserId, userId).getCheckedData())) {
            throw ServiceExceptionUtil.exception(CONVERSATION_PERMISSION_DENIED);
        }
        Long messageId = messageService.sendPrivateMessage(userId, reqVO);
        MessagePrivateDO message = messageService.getMessage(messageId);
        ConversationDO conversation = conversationService.getConversationByTarget(userId, reqVO.getToUserId());
        Map<Long, MemberUserRespDTO> memberMap = loadMemberUserMap(
                CollUtil.newHashSet(message.getFromUserId(), message.getToUserId()));

        AppConversationRespVO conversationVO = AppMessageConvert.buildConversation(conversation,
                memberMap.get(reqVO.getToUserId()),
                sessionManager.isUserOnline(reqVO.getToUserId()));
        AppMessageRespVO messageVO = AppMessageConvert.buildMessage(message, conversation, memberMap, userId);
        return AppMessageConvert.buildPackage(conversationVO, messageVO);
    }

    @Override
    public AppMessageRespVO getMessage(Long userId, Long messageId) {
        MessagePrivateDO message = messageService.getMessage(messageId);
        if (message == null) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }
        if (!Objects.equals(message.getFromUserId(), userId)
                && !Objects.equals(message.getToUserId(), userId)) {
            throw ServiceExceptionUtil.exception(CONVERSATION_PERMISSION_DENIED);
        }
        Long targetUserId = Objects.equals(message.getFromUserId(), userId)
                ? message.getToUserId()
                : message.getFromUserId();
        ConversationDO conversation = conversationService.getConversationByTarget(userId, targetUserId);
        Map<Long, MemberUserRespDTO> memberMap =
                loadMemberUserMap(CollUtil.newHashSet(message.getFromUserId(), message.getToUserId()));
        return AppMessageConvert.buildMessage(message, conversation, memberMap, userId);
    }

    @Override
    public PageResult<AppNotificationRespVO> getNotificationPage(Long userId, AppNotificationPageReqVO reqVO) {
        PageResult<NotificationDO> page = notificationService.getNotificationPage(userId, reqVO.getType(), reqVO);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty();
        }
        List<AppNotificationRespVO> list = buildNotificationList(page.getList());
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public AppNotificationRespVO getNotification(Long userId, Long notificationId) {
        NotificationDO notification = notificationService.getNotification(notificationId, userId);
        if (notification == null) {
            return null;
        }
        List<AppNotificationRespVO> list = buildNotificationList(List.of(notification));
        return CollUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    private List<AppNotificationRespVO> buildNotificationList(List<NotificationDO> notifications) {
        if (CollUtil.isEmpty(notifications)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> relatedList = new ArrayList<>(notifications.size());
        Set<Long> senderIds = new HashSet<>();
        Set<Long> contentIds = new HashSet<>();

        for (NotificationDO notification : notifications) {
            Map<String, Object> related = NotificationRelatedDataHelper.parse(notification.getRelatedData());
            relatedList.add(related);
            Long senderId = NotificationRelatedDataHelper.getLong(related, "actorUserId");
            if (senderId != null) {
                senderIds.add(senderId);
            }
            Long contentId = NotificationRelatedDataHelper.getLong(related, "contentId");
            if (contentId != null) {
                contentIds.add(contentId);
            }
        }

        Map<Long, MemberUserRespDTO> senderMap = loadMemberUserMap(senderIds);
        Map<Long, ContentRespDTO> contentMap = loadContentMap(contentIds);

        List<AppNotificationRespVO> result = new ArrayList<>(notifications.size());
        for (int index = 0; index < notifications.size(); index++) {
            NotificationDO notification = notifications.get(index);
            Map<String, Object> related = relatedList.get(index);
            AppNotificationRespVO vo = AppMessageConvert.buildNotification(notification);

            String behaviorType = NotificationRelatedDataHelper.getString(related, "behaviorType");
            Long senderId = NotificationRelatedDataHelper.getLong(related, "actorUserId");
            Long contentId = NotificationRelatedDataHelper.getLong(related, "contentId");
            Long commentId = NotificationRelatedDataHelper.getLong(related, "commentId");
            Long parentCommentId = NotificationRelatedDataHelper.getLong(related, "parentCommentId");
            String commentText = NotificationRelatedDataHelper.getString(related, "commentText");

            vo.setBehaviorType(behaviorType);
            vo.setSenderUserId(senderId);
            if (senderId != null) {
                MemberUserRespDTO sender = senderMap.get(senderId);
                if (sender != null) {
                    vo.setSenderNickname(sender.getNickname());
                    vo.setSenderAvatar(sender.getAvatar());
                }
            }

            vo.setContentId(contentId);
            vo.setRelationId(contentId);
            vo.setCommentId(commentId);
            vo.setParentCommentId(parentCommentId);
            vo.setCommentText(commentText);

            if (contentId != null) {
                ContentRespDTO content = contentMap.get(contentId);
                if (content != null) {
                    vo.setRelationCover(resolveCover(content));
                }
            }

            result.add(vo);
        }
        return result;
    }

    private Map<Long, ContentRespDTO> loadContentMap(Set<Long> contentIds) {
        if (CollUtil.isEmpty(contentIds)) {
            return Collections.emptyMap();
        }
        try {
            List<ContentRespDTO> list = contentApi.getContentList(contentIds).getCheckedData();
            if (CollUtil.isEmpty(list)) {
                return Collections.emptyMap();
            }
            return list.stream().filter(item -> item != null && item.getId() != null)
                    .collect(Collectors.toMap(ContentRespDTO::getId, identity(), (a, b) -> a));
        } catch (Exception e) {
            log.warn("Load contents failed, ids={}", contentIds, e);
            return Collections.emptyMap();
        }
    }

    private String resolveCover(ContentRespDTO content) {
        if (content == null) {
            return null;
        }
        if (CollUtil.isNotEmpty(content.getImages())) {
            return content.getImages().get(0);
        }
        return content.getVideoCover();
    }

    private Map<Long, MemberUserRespDTO> loadMemberUserMap(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, MemberUserRespDTO> map = memberUserApi.getUserMap(userIds);
            return map != null ? map : Collections.emptyMap();
        } catch (Exception e) {
            log.warn("Load member users failed, ids={}", userIds, e);
            return Collections.emptyMap();
        }
    }

}
