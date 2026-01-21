package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePermissionRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;

/**
 * APP 消息聚合 Service
 *
 * @author Lin
 */
public interface AppMessageService {

    /**
     * 分页查询会话列表
     *
     * @param userId 当前用户
     * @param reqVO  请求
     * @return 会话分页
     */
    PageResult<AppConversationRespVO> getConversationPage(Long userId, AppConversationPageReqVO reqVO);

    /**
     * 查询单个会话
     *
     * @param conversationId 会话编号
     * @param userId         当前用户
     * @return 会话
     */
    AppConversationRespVO getConversation(Long conversationId, Long userId);

    /**
     * 分页查询会话消息
     *
     * @param userId 当前用户
     * @param reqVO  请求
     * @return 消息分页
     */
    PageResult<AppMessageRespVO> getConversationMessages(Long userId, AppMessagePageReqVO reqVO);

    /**
     * 发送私信
     *
     * @param userId 当前用户
     * @param reqVO  请求
     * @return 会话+消息
     */
    AppMessagePackageRespVO sendPrivateMessage(Long userId, MessageSendReqVO reqVO);

    /**
     * 鑾峰彇绉佷俊鍙戦€佹潈闄?     *
     * @param userId       褰撳墠鐢ㄦ埛
     * @param targetUserId 瀵规柟鐢ㄦ埛
     * @return 鍙戦€佹潈闄?
     */
    AppMessagePermissionRespVO getPrivateMessagePermission(Long userId, Long targetUserId);

    /**
     * 查询单条消息
     *
     * @param userId    当前用户
     * @param messageId 消息编号
     * @return 消息
     */
    AppMessageRespVO getMessage(Long userId, Long messageId);

    /**
     * 分页查询通知
     *
     * @param userId 当前用户
     * @param reqVO  请求
     * @return 通知分页
     */
    PageResult<AppNotificationRespVO> getNotificationPage(Long userId, AppNotificationPageReqVO reqVO);

    /**
     * 查询通知详情
     *
     * @param userId         当前用户
     * @param notificationId 通知编号
     * @return 通知
     */
    AppNotificationRespVO getNotification(Long userId, Long notificationId);

}
