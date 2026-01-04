package cn.iocoder.yudao.module.member.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备类型（用于登录设备限制）
 */
@AllArgsConstructor
@Getter
public enum MemberDeviceTypeEnum implements ArrayValuable<Integer> {

    MOBILE(1, "手机"),
    TABLET(2, "平板");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MemberDeviceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}

