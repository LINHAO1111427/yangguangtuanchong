package cn.iocoder.yudao.module.pay.enums.withdraw;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 提现状态枚举
 *
 * @author xiaolvshu
 */
@AllArgsConstructor
@Getter
public enum PayWithdrawStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    PROCESSING(3, "提现中"),
    SUCCESS(4, "提现成功"),
    FAILED(5, "提现失败");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 说明
     */
    private final String description;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PayWithdrawStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayWithdrawStatusEnum valueOf(Integer status) {
        return Arrays.stream(values()).filter(item -> item.getStatus().equals(status)).findFirst().orElse(null);
    }

}
