package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.Map;

final class NotificationRelatedDataHelper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private NotificationRelatedDataHelper() {
    }

    static Map<String, Object> parse(String relatedData) {
        if (StrUtil.isBlank(relatedData)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = JsonUtils.parseObjectQuietly(relatedData, MAP_TYPE);
        return map != null ? map : Collections.emptyMap();
    }

    static Long getLong(Map<String, Object> payload, String key) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static String getString(Map<String, Object> payload, String key) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}

