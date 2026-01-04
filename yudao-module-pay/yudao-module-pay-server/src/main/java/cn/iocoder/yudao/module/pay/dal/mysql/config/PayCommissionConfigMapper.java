package cn.iocoder.yudao.module.pay.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.config.PayCommissionConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台抽成配置 Mapper
 *
 * <p>
 * 目前主要用于打赏等场景查询抽成比例，如业务尚未启用数据库配置，可返回空并由业务层兜底。
 * </p>
 */
@Mapper
public interface PayCommissionConfigMapper extends BaseMapperX<PayCommissionConfigDO> {

    /**
     * 根据业务类型获取抽成配置
     *
     * @param bizType 业务类型
     * @return 配置，可能为 {@code null}
     */
    default PayCommissionConfigDO selectByBizType(Integer bizType) {
        return selectOne(new LambdaQueryWrapperX<PayCommissionConfigDO>()
                .eq(PayCommissionConfigDO::getBizType, bizType)
                .eq(PayCommissionConfigDO::getEnabled, true)
                .last("LIMIT 1"));
    }
}
