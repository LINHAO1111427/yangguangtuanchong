package cn.iocoder.yudao.module.pay.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 平台抽成配置 DO
 *
 * <p>
 * 为新的打赏、提现等功能预留的佣金配置表。当前业务中如未启用数据库配置，
 * 默认值可以由业务层自行处理。
 * </p>
 *
 * @author
 */
@TableName("pay_commission_config")
@KeySequence("pay_commission_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayCommissionConfigDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 抽成比例，百分比形式（0-100）
     */
    private BigDecimal commissionRate;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;

}
