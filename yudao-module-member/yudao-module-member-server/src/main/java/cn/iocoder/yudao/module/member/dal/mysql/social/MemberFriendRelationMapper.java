package cn.iocoder.yudao.module.member.dal.mysql.social;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberFriendRelationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 社交关系 mapper
 *
 * @author sun
 */
@Mapper
public interface MemberFriendRelationMapper extends BaseMapperX<MemberFriendRelationDO> {

    default MemberFriendRelationDO selectByUserIdAndFriendId(Long userId, Long friendId) {
        return selectOne(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getUserId, userId)
                .eq(MemberFriendRelationDO::getFriendId, friendId));
    }

    default List<MemberFriendRelationDO> selectListByUserIdAndFriendIds(Long userId, Collection<Long> friendIds) {
        if (CollUtil.isEmpty(friendIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getUserId, userId)
                .in(MemberFriendRelationDO::getFriendId, friendIds));
    }

    default List<MemberFriendRelationDO> selectListByFriendIdAndUserIds(Long friendId, Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getFriendId, friendId)
                .in(MemberFriendRelationDO::getUserId, userIds));
    }

    default int deleteByUserIdAndFriendId(Long userId, Long friendId) {
        return delete(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getUserId, userId)
                .eq(MemberFriendRelationDO::getFriendId, friendId));
    }

    /**
     * 查询用户的关注列表（已同意的关系）
     */
    default List<MemberFriendRelationDO> selectFollowingList(Long userId, Integer offset, Integer limit) {
        int safeOffset = Math.max(offset != null ? offset : 0, 0);
        int safeLimit = Math.max(limit != null ? limit : 20, 0);
        return selectList(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getUserId, userId)
                .eq(MemberFriendRelationDO::getState, 1)
                .orderByDesc(MemberFriendRelationDO::getLastActionAt)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    /**
     * 查询用户的粉丝列表（已同意的关系）
     */
    default List<MemberFriendRelationDO> selectFollowersList(Long userId, Integer offset, Integer limit) {
        int safeOffset = Math.max(offset != null ? offset : 0, 0);
        int safeLimit = Math.max(limit != null ? limit : 20, 0);
        return selectList(new LambdaQueryWrapperX<MemberFriendRelationDO>()
                .eq(MemberFriendRelationDO::getFriendId, userId)
                .eq(MemberFriendRelationDO::getState, 1)
                .orderByDesc(MemberFriendRelationDO::getLastActionAt)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    /**
     * 统计互相关注（好友）数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM member_friend_relation fr1
                     JOIN member_friend_relation fr2
                          ON fr1.friend_id = fr2.user_id
                              AND fr1.user_id = fr2.friend_id
            WHERE fr1.user_id = #{userId}
              AND fr1.state = 1
              AND fr2.state = 1
            """)
    Long selectMutualFriendCount(@Param("userId") Long userId);
}
