package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapperX<TopicDO> {

    default PageResult<TopicDO> selectPage(TopicPageReqVO reqVO) {
        LambdaQueryWrapperX<TopicDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(TopicDO::getName, reqVO.getName())
                .eqIfPresent(TopicDO::getType, reqVO.getType())
                .eqIfPresent(TopicDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TopicDO::getIsRecommend, reqVO.getIsRecommend())
                .orderByDesc(TopicDO::getId);
        return selectPage(reqVO, wrapper);
    }

    default List<TopicDO> selectRecommendedTopics(Integer limit) {
        return selectList(new LambdaQueryWrapperX<TopicDO>()
                .eq(TopicDO::getIsRecommend, 1)
                .orderByDesc(TopicDO::getId)
                .last("LIMIT " + (limit != null ? limit : 10)));
    }

    default List<TopicDO> selectHotTopics(Integer limit) {
        return selectList(new LambdaQueryWrapperX<TopicDO>()
                .orderByDesc(TopicDO::getHotScore)
                .last("LIMIT " + (limit != null ? limit : 10)));
    }

    default List<TopicDO> selectByType(Integer type) {
        return selectList(new LambdaQueryWrapperX<TopicDO>()
                .eq(TopicDO::getType, type)
                .orderByDesc(TopicDO::getId));
    }

    default List<TopicDO> searchTopics(String keyword, Integer limit) {
        return selectList(new LambdaQueryWrapperX<TopicDO>()
                .likeIfPresent(TopicDO::getName, keyword)
                .orderByDesc(TopicDO::getId)
                .last("LIMIT " + (limit != null ? limit : 10)));
    }

    default List<TopicDO> selectBasicInfo() {
        return selectList(new LambdaQueryWrapperX<TopicDO>()
                .orderByAsc(TopicDO::getId));
    }

    default List<TopicDO> selectAllEnabledTopics(Integer limit) {
        LambdaQueryWrapperX<TopicDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.orderByAsc(TopicDO::getId);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        return selectList(wrapper);
    }
}
