package cn.iocoder.yudao.module.member.service.session;

import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;

/**
 * 会员用户会话服务
 */
public interface MemberUserSessionService {

    /**
     * 登录成功：记录会话，并按设备类型挤掉旧会话
     */
    void onLoginSuccess(Long userId, AppAuthLoginRespVO loginResp,
                        Integer deviceType, String deviceId, String deviceName,
                        String osName, String osVersion, String appVersion);

    /**
     * 主动登出：标记会话下线
     */
    void onLogout(String accessToken, Long userId);

    /**
     * 被挤下线：标记会话下线（供内部调用）
     */
    void onKicked(String accessToken, Long userId);
}

