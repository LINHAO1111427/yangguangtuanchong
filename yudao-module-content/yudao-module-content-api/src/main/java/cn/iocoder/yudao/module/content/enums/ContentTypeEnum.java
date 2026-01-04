package cn.iocoder.yudao.module.content.enums;

// 作者：Lin：移除对 IntArrayValuable 的依赖，保持 API 自包含
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容类型枚举
 *
 * @author xiaolvshu
 */
@Getter
@AllArgsConstructor
public enum ContentTypeEnum {

    ARTICLE(1, "图文"),
    VIDEO(2, "视频"),
    MOMENT(3, "动态");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ContentTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    // 兼容原 array() 调用
    public int[] array() {
        return ARRAYS;
    }

}
