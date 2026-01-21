package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContentChannelMapper extends BaseMapperX<ContentChannelDO> {

    default cn.iocoder.yudao.framework.common.pojo.PageResult<ContentChannelDO> selectPage(ContentChannelPageReqVO reqVO) {
        LambdaQueryWrapperX<ContentChannelDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(ContentChannelDO::getCode, reqVO.getCode())
                .likeIfPresent(ContentChannelDO::getName, reqVO.getName())
                .eqIfPresent(ContentChannelDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ContentChannelDO::getIsDefault, reqVO.getIsDefault())
                .eqIfPresent(ContentChannelDO::getIsRequired, reqVO.getIsRequired())
                .eq(ContentChannelDO::getDeleted, 0)
                .orderByAsc(ContentChannelDO::getSort)
                .orderByAsc(ContentChannelDO::getId);
        return selectPage(reqVO, wrapper);
    }

    default List<ContentChannelDO> selectEnabledChannels() {
        return selectList(new LambdaQueryWrapperX<ContentChannelDO>()
                .eq(ContentChannelDO::getStatus, 1)
                .eq(ContentChannelDO::getDeleted, 0) // PostgreSQL兼容：使用Integer 0代替Boolean.FALSE
                .orderByAsc(ContentChannelDO::getSort)
                .orderByAsc(ContentChannelDO::getId));
    }

    default List<ContentChannelDO> selectDefaultChannels() {
        return selectList(new LambdaQueryWrapperX<ContentChannelDO>()
                .eq(ContentChannelDO::getStatus, 1)
                .eq(ContentChannelDO::getDeleted, 0) // PostgreSQL兼容：使用Integer 0代替Boolean.FALSE
                .eq(ContentChannelDO::getIsDefault, 1)
                .orderByAsc(ContentChannelDO::getSort)
                .orderByAsc(ContentChannelDO::getId));
    }
}
