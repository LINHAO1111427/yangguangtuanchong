package cn.iocoder.yudao.module.member.dal.mysql.social;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberBlacklistDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 黑名单 mapper
 *
 * @author sun
 */
@Mapper
public interface MemberBlacklistMapper extends BaseMapperX<MemberBlacklistDO> {

    default MemberBlacklistDO selectByUserIdAndTargetId(Long userId, Long targetId) {
        return selectOne(new LambdaQueryWrapperX<MemberBlacklistDO>()
                .eq(MemberBlacklistDO::getUserId, userId)
                .eq(MemberBlacklistDO::getTargetId, targetId));
    }

    default List<MemberBlacklistDO> selectListByUserIdAndTargetIds(Long userId, Collection<Long> targetIds) {
        if (CollUtil.isEmpty(targetIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<MemberBlacklistDO>()
                .eq(MemberBlacklistDO::getUserId, userId)
                .in(MemberBlacklistDO::getTargetId, targetIds));
    }

    default List<MemberBlacklistDO> selectListByUserIdsAndTargetId(Collection<Long> userIds, Long targetId) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<MemberBlacklistDO>()
                .in(MemberBlacklistDO::getUserId, userIds)
                .eq(MemberBlacklistDO::getTargetId, targetId));
    }

    default int deleteByUserIdAndTargetId(Long userId, Long targetId) {
        return delete(new LambdaQueryWrapperX<MemberBlacklistDO>()
                .eq(MemberBlacklistDO::getUserId, userId)
                .eq(MemberBlacklistDO::getTargetId, targetId));
    }

}
