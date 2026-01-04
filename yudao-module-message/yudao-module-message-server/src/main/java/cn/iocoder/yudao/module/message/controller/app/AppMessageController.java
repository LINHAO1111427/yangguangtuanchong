package cn.iocoder.yudao.module.message.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppConversationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessagePackageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppMessageRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationPageReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppNotificationRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppShareCardRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppSuggestedFriendRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.MessageSendReqVO;
import cn.iocoder.yudao.module.message.controller.app.vo.integration.AppTencentIntegrationStatusRespVO;
import cn.iocoder.yudao.module.message.controller.app.vo.integration.AppTencentRtcSignatureRespVO;
import cn.iocoder.yudao.module.message.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.message.service.AppMessageService;
import cn.iocoder.yudao.module.message.service.ConversationService;
import cn.iocoder.yudao.module.message.service.MessageService;
import cn.iocoder.yudao.module.message.service.NotificationService;
import cn.iocoder.yudao.module.message.service.ShareCardService;
import cn.iocoder.yudao.module.message.service.SuggestedFriendService;
import cn.iocoder.yudao.module.message.service.TencentIntegrationService;
import cn.iocoder.yudao.module.message.service.TencentIntegrationService.RtcSignature;
import cn.iocoder.yudao.module.message.websocket.MessageWebSocketSessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息API - APP 端
 */
@Tag(name = "APP - 消息管理")
@RestController
// app-api 前缀由 YudaoWebAutoConfiguration 基于包名自动添加，这里只保留业务路径
@RequestMapping("/message")
@Validated
@Slf4j
public class AppMessageController {

    @Resource
    private MessageWebSocketSessionManager sessionManager;
    @Resource
    private AppMessageService appMessageService;
    @Resource
    private MessageService messageService;
    @Resource
    private ConversationService conversationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private ShareCardService shareCardService;
    @Resource
    private SuggestedFriendService suggestedFriendService;
    @Resource
    private TencentIntegrationService tencentIntegrationService;

    // ========== 系统相关接口 ==========

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public CommonResult<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("service", "message-server");
        data.put("timestamp", LocalDateTime.now());
        data.put("onlineUsers", sessionManager.getOnlineUserCount());
        data.put("statistics", sessionManager.getStatistics());
        return CommonResult.success(data);
    }

    @GetMapping("/online-count")
    @Operation(summary = "获取在线用户数")
    public CommonResult<Integer> getOnlineCount() {
        return CommonResult.success(sessionManager.getOnlineUserCount());
    }

    @GetMapping("/is-online")
    @Operation(summary = "检查用户是否在线")
    public CommonResult<Boolean> isUserOnline(@RequestParam("userId") Long userId) {
        return CommonResult.success(sessionManager.isUserOnline(userId));
    }

    // ========== 私聊消息接口 ==========

    @PostMapping("/private/send")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "发送私聊消息")
    public CommonResult<AppMessagePackageRespVO> sendPrivateMessage(@Valid @RequestBody MessageSendReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        try {
            AppMessagePackageRespVO respVO = appMessageService.sendPrivateMessage(userId, reqVO);
            return CommonResult.success(respVO);
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException ex) {
            if (ErrorCodeConstants.CONVERSATION_PERMISSION_DENIED.getCode().equals(ex.getCode())) {
                return CommonResult.error(ErrorCodeConstants.CONVERSATION_PERMISSION_DENIED.getCode(), "请先互相关注再发送私信");
            }
            throw ex;
        }
    }

    @GetMapping("/private/conversation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取会话消息列表")
    public CommonResult<PageResult<AppMessageRespVO>> getConversationMessages(@Valid AppMessagePageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<AppMessageRespVO> messages = appMessageService.getConversationMessages(userId, reqVO);
        return CommonResult.success(messages);
    }

    @PostMapping("/private/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记消息为已读")
    public CommonResult<Boolean> markAsRead(@RequestBody List<Long> messageIds) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.markAsRead(messageIds, userId);
        return CommonResult.success(true);
    }

    @PostMapping("/private/recall")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "撤回消息")
    public CommonResult<Boolean> recallMessage(@RequestParam("messageId") Long messageId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.recallMessage(messageId, userId);
        return CommonResult.success(true);
    }

    @DeleteMapping("/private/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除消息")
    public CommonResult<Boolean> deleteMessage(@PathVariable("messageId") Long messageId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.deleteMessage(messageId, userId);
        return CommonResult.success(true);
    }

    @GetMapping("/private/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取未读消息数")
    public CommonResult<Long> getUnreadCount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(messageService.getUnreadCount(userId));
    }

    @GetMapping("/private/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取消息详情")
    public CommonResult<AppMessageRespVO> getMessage(@PathVariable("messageId") Long messageId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(appMessageService.getMessage(userId, messageId));
    }

    // ========== 会话管理接口 ==========

    @GetMapping("/conversation/list")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取会话列表")
    public CommonResult<PageResult<AppConversationRespVO>> getConversationList(@Valid AppConversationPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(appMessageService.getConversationPage(userId, reqVO));
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取会话详情")
    public CommonResult<AppConversationRespVO> getConversation(@PathVariable("conversationId") Long conversationId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(appMessageService.getConversation(conversationId, userId));
    }

    @PostMapping("/conversation/{conversationId}/clear-unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "清空会话未读数")
    public CommonResult<Boolean> clearUnreadCount(@PathVariable("conversationId") Long conversationId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        conversationService.clearUnreadCount(conversationId, userId);
        return CommonResult.success(true);
    }

    @PostMapping("/conversation/{conversationId}/toggle-top")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "置顶/取消置顶会话")
    public CommonResult<Boolean> toggleTop(@PathVariable("conversationId") Long conversationId,
                                           @RequestParam("isTop") Integer isTop) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        conversationService.toggleTop(conversationId, userId, isTop);
        return CommonResult.success(true);
    }

    @PostMapping("/conversation/{conversationId}/toggle-mute")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "设置/取消免打扰")
    public CommonResult<Boolean> toggleMute(@PathVariable("conversationId") Long conversationId,
                                            @RequestParam("isMute") Integer isMute) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        conversationService.toggleMute(conversationId, userId, isMute);
        return CommonResult.success(true);
    }

    @DeleteMapping("/conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除会话")
    public CommonResult<Boolean> deleteConversation(@PathVariable("conversationId") Long conversationId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        conversationService.deleteConversation(conversationId, userId);
        return CommonResult.success(true);
    }

    @GetMapping("/conversation/total-unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取总未读数")
    public CommonResult<Long> getTotalUnreadCount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(conversationService.getTotalUnreadCount(userId));
    }

    // ========== 系统通知接口 ==========

    @GetMapping("/notification/list")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取通知列表")
    public CommonResult<PageResult<AppNotificationRespVO>> getNotificationList(@Valid AppNotificationPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(appMessageService.getNotificationPage(userId, reqVO));
    }

    @GetMapping("/notification/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取通知详情")
    public CommonResult<AppNotificationRespVO> getNotification(@PathVariable("notificationId") Long notificationId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(appMessageService.getNotification(userId, notificationId));
    }

    @PostMapping("/notification/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记通知为已读")
    public CommonResult<Boolean> markNotificationAsRead(@RequestBody List<Long> notificationIds) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        notificationService.markAsRead(notificationIds, userId);
        return CommonResult.success(true);
    }

    @PostMapping("/notification/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "全部标记为已读")
    public CommonResult<Boolean> markAllAsRead(@RequestParam(value = "type", required = false) Integer type) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        notificationService.markAllAsRead(userId, type);
        return CommonResult.success(true);
    }

    @DeleteMapping("/notification")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除通知")
    public CommonResult<Boolean> deleteNotifications(@RequestBody List<Long> notificationIds) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        notificationService.deleteNotifications(notificationIds, userId);
        return CommonResult.success(true);
    }

    @DeleteMapping("/notification/clear-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "清空所有通知")
    public CommonResult<Boolean> clearAllNotifications(@RequestParam(value = "type", required = false) Integer type) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        notificationService.clearAll(userId, type);
        return CommonResult.success(true);
    }

    @GetMapping("/notification/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取未读通知数")
    public CommonResult<Long> getNotificationUnreadCount(@RequestParam(value = "type", required = false) Integer type) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(notificationService.getUnreadCount(userId, type));
    }

    // ========== 分享卡片接口 ==========

    @GetMapping("/share/card")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取分享作品卡片信息")
    public CommonResult<AppShareCardRespVO> getShareCard(@RequestParam("content_id") Long contentId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(shareCardService.getShareCard(userId, contentId));
    }

    // ========== 可能认识的人（占位） ==========

    @GetMapping("/suggested-friends")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "可能认识的人（占位，算法由后续服务提供）")
    public CommonResult<List<AppSuggestedFriendRespVO>> getSuggestedFriends(
            @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit,
            @RequestParam(value = "city_code", required = false) String cityCode,
            @RequestParam(value = "district_code", required = false) String districtCode,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "wifi_hash", required = false) String wifiHash,
            @RequestParam(value = "ip_hash", required = false) String ipHash,
            @RequestParam(value = "school_id", required = false) Long schoolId,
            @RequestParam(value = "grade", required = false) String grade) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return CommonResult.success(suggestedFriendService.getSuggestedFriends(
                userId, limit, cityCode, districtCode, lat, lng, wifiHash, ipHash, schoolId, grade));
    }

    // ========== 第三方集成 ==========

    @GetMapping("/integration/tencent/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取腾讯语音/视频接入状态")
    public CommonResult<AppTencentIntegrationStatusRespVO> getTencentIntegrationStatus() {
        AppTencentIntegrationStatusRespVO respVO = new AppTencentIntegrationStatusRespVO();
        respVO.setRtcEnabled(tencentIntegrationService.isRtcEnabled());
        respVO.setVoiceEnabled(tencentIntegrationService.isVoiceEnabled());
        respVO.setVoiceRegion(tencentIntegrationService.getVoiceRegion());
        return CommonResult.success(respVO);
    }

    @GetMapping("/integration/tencent/rtc-signature")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取腾讯 RTC 用户签名")
    public CommonResult<AppTencentRtcSignatureRespVO> getTencentRtcSignature(
            @RequestParam("roomId") String roomId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        AppTencentRtcSignatureRespVO respVO = new AppTencentRtcSignatureRespVO();
        respVO.setEnabled(false);
        tencentIntegrationService.generateRtcSignature(userId, roomId)
                .ifPresent(signature -> fillSignature(respVO, signature));
        return CommonResult.success(respVO);
    }

    private void fillSignature(AppTencentRtcSignatureRespVO respVO, RtcSignature signature) {
        respVO.setEnabled(true);
        respVO.setSdkAppId(signature.getSdkAppId());
        respVO.setUserId(signature.getUserId());
        respVO.setRoomId(signature.getRoomId());
        respVO.setNonce(signature.getNonce());
        respVO.setExpireTimestamp(signature.getExpireTimestamp());
        respVO.setUserSig(signature.getSignature());
    }

}
