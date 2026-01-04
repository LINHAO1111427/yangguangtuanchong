package cn.iocoder.yudao.module.pay.enums.reward;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 打赏类型枚举
 *
 * @author xiaolvshu
 */
@AllArgsConstructor
@Getter
public enum PayRewardTypeEnum implements ArrayValuable<Integer> {

    CONTENT(1, "内容打赏"),
    COMMENT(2, "评论打赏");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PayRewardTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayRewardTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(PayRewardTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}
