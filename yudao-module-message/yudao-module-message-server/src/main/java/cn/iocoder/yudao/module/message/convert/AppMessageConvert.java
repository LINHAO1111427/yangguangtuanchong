package cn.iocoder.yudao.module.message.convert;

import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMemberSimpleRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationRespVO;
import cn.iocoder.yudao.module.message.dal.dataobject.ConversationDO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Objects;

/**
 * APP 消息相关 VO 转换
 *
 * @author Lin
 */
@UtilityClass
public class AppMessageConvert {

    public AppConversationRespVO buildConversation(ConversationDO conversation, MemberUserRespDTO targetUser,
                                                   boolean targetOnline) {
        if (conversation == null) {
            return null;
        }
        AppConversationRespVO vo = new AppConversationRespVO();
        vo.setId(conversation.getId());
        vo.setUserId(conversation.getUserId());
        vo.setTargetUserId(conversation.getTargetId());
        vo.setUnreadCount(conversation.getUnreadCount());
        vo.setIsTop(conversation.getIsTop());
        vo.setIsMute(conversation.getIsMute());
        vo.setLastMessageContent(conversation.getLastMessageContent());
        vo.setLastMessageTime(conversation.getLastMessageTime());
        vo.setTargetNickname(targetUser != null ? targetUser.getNickname() : null);
        vo.setTargetAvatar(targetUser != null ? targetUser.getAvatar() : null);
        vo.setTargetOnline(targetOnline);
        return vo;
    }

    public AppMessageRespVO buildMessage(MessagePrivateDO message, ConversationDO conversation,
                                         Map<Long, MemberUserRespDTO> memberMap, Long currentUserId) {
        if (message == null) {
            return null;
        }
        AppMessageRespVO vo = new AppMessageRespVO();
        vo.setId(message.getId());
        vo.setConversationId(conversation != null ? conversation.getId() : null);
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setSelf(Objects.equals(message.getFromUserId(), currentUserId));
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setExtraData(message.getExtraData());
        vo.setStatus(message.getStatus());
        vo.setCreateTime(message.getCreateTime());
        vo.setReadTime(message.getReadTime());
        vo.setSender(buildMember(memberMap != null ? memberMap.get(message.getFromUserId()) : null));
        return vo;
    }

    public AppMemberSimpleRespVO buildMember(MemberUserRespDTO user) {
        if (user == null) {
            return null;
        }
        AppMemberSimpleRespVO vo = new AppMemberSimpleRespVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    public AppNotificationRespVO buildNotification(NotificationDO notification) {
        if (notification == null) {
            return null;
        }
        AppNotificationRespVO vo = new AppNotificationRespVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedData(notification.getRelatedData());
        vo.setLink(notification.getLink());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        vo.setReadTime(notification.getReadTime());
        return vo;
    }

    public AppMessagePackageRespVO buildPackage(AppConversationRespVO conversation, AppMessageRespVO message) {
        if (conversation == null && message == null) {
            return null;
        }
        AppMessagePackageRespVO vo = new AppMessagePackageRespVO();
        vo.setConversation(conversation);
        vo.setMessage(message);
        return vo;
    }

}
