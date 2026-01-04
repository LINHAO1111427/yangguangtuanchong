package cn.iocoder.yudao.module.member.enums;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 会员任务类型枚举
 *
 * @author xiaolvshu
 */
@AllArgsConstructor
@Getter
public enum MemberTaskTypeEnum implements ArrayValuable<Integer> {

    WATCH_VIDEO(1, "观看短视频"),
    VIEW_POST(2, "观看图文"),
    WATCH_AD(3, "观看广告");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static MemberTaskTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(MemberTaskTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}
