package cn.iocoder.yudao.module.member.service.session;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.ip.core.utils.IPUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.session.MemberUserSessionDO;
import cn.iocoder.yudao.module.member.dal.mysql.session.MemberUserSessionMapper;
import cn.iocoder.yudao.module.member.enums.MemberDeviceTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;

@Service
@Slf4j
public class MemberUserSessionServiceImpl implements MemberUserSessionService {

    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_LOGOUT = 1;
    public static final int STATUS_KICKED = 2;

    @Resource
    private MemberUserSessionMapper sessionMapper;
    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onLoginSuccess(Long userId, AppAuthLoginRespVO loginResp,
                               Integer deviceType, String deviceId, String deviceName,
                               String osName, String osVersion, String appVersion) {
        if (userId == null || loginResp == null || StrUtil.isBlank(loginResp.getAccessToken())) {
            return;
        }

        Integer normalizedDeviceType = normalizeDeviceType(deviceType);
        String ip = getClientIP();
        String ipArea = resolveIpProvince(ip);
        Integer terminal = WebFrameworkUtils.getTerminal();

        // 1) 先挤掉同设备类型的在线会话（保留：1台手机 + 1台平板）
        List<MemberUserSessionDO> onlineList = sessionMapper.selectList(new LambdaQueryWrapperX<MemberUserSessionDO>()
                .eq(MemberUserSessionDO::getUserId, userId)
                .eq(MemberUserSessionDO::getDeviceType, normalizedDeviceType)
                .eq(MemberUserSessionDO::getStatus, STATUS_ONLINE)
                .orderByDesc(MemberUserSessionDO::getCreateTime));
        if (onlineList != null) {
            for (MemberUserSessionDO old : onlineList) {
                if (old == null || StrUtil.isBlank(old.getAccessToken())) continue;
                if (StrUtil.equals(old.getAccessToken(), loginResp.getAccessToken())) continue;
                kickSession(old);
            }
        }

        // 2) 记录新会话
        MemberUserSessionDO session = new MemberUserSessionDO();
        session.setUserId(userId);
        session.setAccessToken(loginResp.getAccessToken());
        session.setRefreshToken(loginResp.getRefreshToken());
        session.setDeviceType(normalizedDeviceType);
        session.setDeviceId(StrUtil.blankToDefault(deviceId, null));
        session.setDeviceName(StrUtil.blankToDefault(deviceName, null));
        session.setOsName(StrUtil.blankToDefault(osName, null));
        session.setOsVersion(StrUtil.blankToDefault(osVersion, null));
        session.setAppVersion(StrUtil.blankToDefault(appVersion, null));
        session.setTerminal(terminal);
        session.setLoginIp(ip);
        session.setLoginIpArea(ipArea);
        session.setUserAgent(getUserAgent());
        session.setStatus(STATUS_ONLINE);
        session.setLogoutTime(null);
        sessionMapper.insert(session);
    }

    @Override
    public void onLogout(String accessToken, Long userId) {
        markOffline(accessToken, userId, STATUS_LOGOUT);
    }

    @Override
    public void onKicked(String accessToken, Long userId) {
        markOffline(accessToken, userId, STATUS_KICKED);
    }

    private void kickSession(MemberUserSessionDO old) {
        try {
            // 先移除系统访问令牌（全局生效）
            oauth2TokenApi.removeAccessToken(old.getAccessToken()).checkError();
        } catch (Exception ex) {
            log.warn("[kickSession] remove access token failed, token={}", old.getAccessToken(), ex);
        } finally {
            // 再标记会话为被挤下线
            old.setStatus(STATUS_KICKED);
            old.setLogoutTime(LocalDateTime.now());
            sessionMapper.updateById(old);
        }
    }

    private void markOffline(String accessToken, Long userId, int status) {
        if (StrUtil.isBlank(accessToken)) return;
        MemberUserSessionDO session = sessionMapper.selectOne(new LambdaQueryWrapperX<MemberUserSessionDO>()
                .eq(MemberUserSessionDO::getAccessToken, accessToken)
                .eqIfPresent(MemberUserSessionDO::getUserId, userId)
                .orderByDesc(MemberUserSessionDO::getCreateTime)
                .last("LIMIT 1"));
        if (session == null) return;
        if (!Integer.valueOf(STATUS_ONLINE).equals(session.getStatus())) return;
        session.setStatus(status);
        session.setLogoutTime(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    private Integer normalizeDeviceType(Integer deviceType) {
        if (deviceType == null) {
            return MemberDeviceTypeEnum.MOBILE.getType();
        }
        if (MemberDeviceTypeEnum.TABLET.getType().equals(deviceType)) {
            return deviceType;
        }
        return MemberDeviceTypeEnum.MOBILE.getType();
    }

    /**
     * 解析 IP 属地：优先省/直辖市，其次城市/地区。
     */
    private String resolveIpProvince(String ip) {
        if (StrUtil.isBlank(ip)) return null;
        try {
            Integer areaId = IPUtils.getAreaId(ip);
            if (areaId == null) return null;
            Integer provinceId = AreaUtils.getParentIdByType(areaId, AreaTypeEnum.PROVINCE);
            Integer useId = provinceId != null ? provinceId : areaId;
            Area area = AreaUtils.getArea(useId);
            return area != null ? area.getName() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
