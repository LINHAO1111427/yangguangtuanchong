package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ContentAdMapper extends BaseMapperX<ContentAdDO> {

    default List<ContentAdDO> selectActiveAds(Integer scene, LocalDateTime now) {
        LambdaQueryWrapperX<ContentAdDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ContentAdDO::getStatus, 1)
                .eqIfPresent(ContentAdDO::getDisplayScene, scene)
                .eq(ContentAdDO::getDeleted, 0)
                .apply("(start_time IS NULL OR start_time <= {0})", now)
                .apply("(end_time IS NULL OR end_time >= {0})", now)
                .orderByDesc(ContentAdDO::getPriority)
                .orderByAsc(ContentAdDO::getId);
        return selectList(wrapper);
    }
}
