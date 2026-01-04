package cn.iocoder.yudao.module.member.dal.dataobject.session;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会员用户会话 DO
 *
 * 用于实现「1台平板 + 1台手机」登录限制，以及记录 IP 属地等审计信息。
 */
@TableName("member_user_session")
@KeySequence("member_user_session_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberUserSessionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌（可选）
     */
    private String refreshToken;

    /**
     * 设备类型：1=手机 2=平板
     */
    private Integer deviceType;

    /**
     * 设备唯一标识
     */
    private String deviceId;

    /**
     * 设备名称/型号
     */
    private String deviceName;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * App 版本
     */
    private String appVersion;

    /**
     * 终端类型（TerminalEnum）
     */
    private Integer terminal;

    /**
     * 登录 IP
     */
    private String loginIp;

    /**
     * IP 属地（省/直辖市）
     */
    private String loginIpArea;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 状态：0=在线 1=主动登出 2=被挤下线
     */
    private Integer status;

    /**
     * 下线时间
     */
    private LocalDateTime logoutTime;
}

