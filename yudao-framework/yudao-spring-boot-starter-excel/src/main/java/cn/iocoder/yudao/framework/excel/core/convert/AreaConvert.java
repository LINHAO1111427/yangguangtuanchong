package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 地区解析转换器，兼容缺失 AreaUtils 场景。
 *
 * <p>
 * 当基础设施模块提供 area 工具类时，优先使用反射调用进行解析；
 * 否则退化为从文本中提取数字部分作为地区 ID。
 * </p>
 */
@Slf4j
public class AreaConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String label = readCellData.getStringValue();
        Long areaId = parseAreaIdByReflection(label);
        if (areaId == null) {
            areaId = extractDigits(label);
        }
        if (areaId == null) {
            log.error("[convertToJavaData][label({}) 无法解析地区信息]", label);
            return null;
        }
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, areaId);
    }

    private Long parseAreaIdByReflection(String label) {
        try {
            Class<?> utilsClass = Class.forName("cn.iocoder.yudao.framework.ip.core.utils.AreaUtils");
            Object areaObj = utilsClass.getMethod("parseArea", String.class).invoke(null, label);
            if (areaObj == null) {
                return null;
            }
            Object idObj = areaObj.getClass().getMethod("getId").invoke(areaObj);
            return Convert.convert(Long.class, idObj);
        } catch (ClassNotFoundException ex) {
            // optional dependency not present
            return null;
        } catch (Throwable ex) {
            log.warn("[convertToJavaData][AreaUtils.parseArea 解析失败, label({})]", label, ex);
            return null;
        }
    }

    private Long extractDigits(String label) {
        if (label == null) {
            return null;
        }
        try {
            String digits = label.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return Long.parseLong(digits);
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }
}

