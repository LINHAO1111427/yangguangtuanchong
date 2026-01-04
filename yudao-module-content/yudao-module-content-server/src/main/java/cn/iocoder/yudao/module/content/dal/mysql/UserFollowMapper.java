package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.UserFollowDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapperX<UserFollowDO> {

    default UserFollowDO selectOne(Long followerId, Long targetId) {
        return selectOne(new LambdaQueryWrapperX<UserFollowDO>()
                .eq(UserFollowDO::getFollowerId, followerId)
                .eq(UserFollowDO::getTargetId, targetId)
                .last("LIMIT 1"));
    }

    default List<UserFollowDO> selectFollowing(Long userId, Integer offset, Integer limit) {
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        int safeLimit = limit == null ? 1 : Math.max(limit, 1);
        return selectList(new LambdaQueryWrapperX<UserFollowDO>()
                .eq(UserFollowDO::getFollowerId, userId)
                .eq(UserFollowDO::getStatus, (short) 0)
                .eq(UserFollowDO::getDeleted, 0)
                .orderByDesc(UserFollowDO::getCreateTime)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    default List<UserFollowDO> selectFans(Long targetId, Integer offset, Integer limit) {
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        int safeLimit = limit == null ? 1 : Math.max(limit, 1);
        return selectList(new LambdaQueryWrapperX<UserFollowDO>()
                .eq(UserFollowDO::getTargetId, targetId)
                .eq(UserFollowDO::getStatus, (short) 0)
                .eq(UserFollowDO::getDeleted, 0)
                .orderByDesc(UserFollowDO::getCreateTime)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    default Long countFollowing(Long userId) {
        return selectCount(new LambdaQueryWrapperX<UserFollowDO>()
                .eq(UserFollowDO::getFollowerId, userId)
                .eq(UserFollowDO::getStatus, (short) 0)
                .eq(UserFollowDO::getDeleted, 0));
    }

    default Long countFans(Long targetId) {
        return selectCount(new LambdaQueryWrapperX<UserFollowDO>()
                .eq(UserFollowDO::getTargetId, targetId)
                .eq(UserFollowDO::getStatus, (short) 0)
                .eq(UserFollowDO::getDeleted, 0));
    }
}
