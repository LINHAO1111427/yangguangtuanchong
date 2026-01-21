package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ContentAdMapper extends BaseMapperX<ContentAdDO> {

    default cn.iocoder.yudao.framework.common.pojo.PageResult<ContentAdDO> selectPage(ContentAdPageReqVO reqVO) {
        LambdaQueryWrapperX<ContentAdDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(ContentAdDO::getTitle, reqVO.getTitle())
                .likeIfPresent(ContentAdDO::getAdvertiserName, reqVO.getAdvertiserName())
                .eqIfPresent(ContentAdDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ContentAdDO::getDisplayScene, reqVO.getDisplayScene())
                .eq(ContentAdDO::getDeleted, 0)
                .orderByDesc(ContentAdDO::getId);
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(ContentAdDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        return selectPage(reqVO, wrapper);
    }

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
