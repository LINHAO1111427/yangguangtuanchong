package cn.iocoder.yudao.module.member.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客记录 DO
 *
 * 记录「谁访问了我」的关系，用于数据中心/访客功能。
 *
 * 表结构以 BaseDO 规范为准（create_time/update_time/deleted/tenant_id）。
 */
@TableName("member_visitor_log")
@KeySequence("member_visitor_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberVisitorLogDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 被访问用户 ID
     */
    private Long userId;

    /**
     * 访客用户 ID
     */
    private Long visitorId;

    /**
     * 访问类型：1=主页 2=作品
     */
    private Integer visitType;

    /**
     * 目标 ID（例如作品 ID）
     */
    private Long targetId;

    /**
     * 是否付费解锁（预留）
     */
    private Boolean isPaid;

    /**
     * 付费金额（分，预留）
     */
    private Integer payAmount;
}

