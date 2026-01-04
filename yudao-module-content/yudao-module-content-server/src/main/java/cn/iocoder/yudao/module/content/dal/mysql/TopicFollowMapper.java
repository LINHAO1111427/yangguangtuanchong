package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicFollowDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TopicFollowMapper extends BaseMapperX<TopicFollowDO> {

    default TopicFollowDO selectOne(Long userId, Long topicId) {
        return selectOne(new LambdaQueryWrapperX<TopicFollowDO>()
                .eq(TopicFollowDO::getUserId, userId)
                .eq(TopicFollowDO::getTopicId, topicId)
                .last("LIMIT 1"));
    }

    default List<TopicFollowDO> selectByUser(Long userId, Integer offset, Integer limit) {
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        int safeLimit = limit == null ? 1 : Math.max(limit, 1);
        return selectList(new LambdaQueryWrapperX<TopicFollowDO>()
                .eq(TopicFollowDO::getUserId, userId)
                .eq(TopicFollowDO::getStatus, (short) 0)
                .eq(TopicFollowDO::getDeleted, 0)
                .orderByDesc(TopicFollowDO::getCreateTime)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    default Long countByUser(Long userId) {
        return selectCount(new LambdaQueryWrapperX<TopicFollowDO>()
                .eq(TopicFollowDO::getUserId, userId)
                .eq(TopicFollowDO::getStatus, (short) 0)
                .eq(TopicFollowDO::getDeleted, 0));
    }
}
