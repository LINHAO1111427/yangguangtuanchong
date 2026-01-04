package cn.iocoder.yudao.module.pay.dal.dataobject.reward;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 打赏订单 DO
 *
 * @author xiaolvshu
 */
@TableName("pay_reward_order")
@KeySequence("pay_reward_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRewardOrderDO extends BaseDO {

    /**
     * 订单ID
     */
    @TableId
    private Long id;

    /**
     * 打赏用户ID
     */
    private Long userId;

    /**
     * 被打赏用户ID（作者ID）
     */
    private Long authorId;

    /**
     * 钱包ID
     */
    private Long walletId;

    /**
     * 打赏内容ID（内容ID或评论ID）
     */
    private Long targetId;

    /**
     * 打赏类型
     * 1-内容打赏 2-评论打赏
     */
    private Integer rewardType;

    /**
     * 打赏金额（分）
     */
    private Integer amount;

    /**
     * 平台抽成金额（分）
     */
    private Integer commissionAmount;

    /**
     * 作者收益金额（分）
     */
    private Integer incomeAmount;

    /**
     * 支付状态
     * 0-待支付 1-已支付 2-已退款
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注
     */
    private String remark;

}
