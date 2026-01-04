package cn.iocoder.yudao.module.pay.dal.dataobject.withdraw;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 提现订单 DO
 *
 * @author xiaolvshu
 */
@TableName("pay_withdraw_order")
@KeySequence("pay_withdraw_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayWithdrawOrderDO extends BaseDO {

    /**
     * 订单ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 钱包ID
     */
    private Long walletId;

    /**
     * 提现金额（分）
     */
    private Integer amount;

    /**
     * 手续费（分）
     */
    private Integer fee;

    /**
     * 实际到账金额（分）
     */
    private Integer realAmount;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 开户名
     */
    private String accountName;

    /**
     * 提现状态
     * 0-待审核 1-审核通过 2-审核拒绝 3-提现中 4-提现成功 5-提现失败
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 提现完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 备注
     */
    private String remark;

}
