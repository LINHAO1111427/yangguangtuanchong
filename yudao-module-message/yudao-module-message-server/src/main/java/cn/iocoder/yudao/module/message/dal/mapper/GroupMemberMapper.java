package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMemberDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface GroupMemberMapper extends BaseMapperX<GroupMemberDO> {

    default List<GroupMemberDO> selectListByGroupId(Long groupId) {
        return selectList(new QueryWrapperX<GroupMemberDO>()
                .eq("group_id", groupId)
                .eq("status", 0)
                .orderByAsc("role")
                .orderByAsc("join_time"));
    }

    default List<Long> selectGroupIdsByUserId(Long userId) {
        return selectList(new QueryWrapperX<GroupMemberDO>()
                .eq("user_id", userId)
                .eq("status", 0))
                .stream()
                .map(GroupMemberDO::getGroupId)
                .collect(Collectors.toList());
    }

    default GroupMemberDO selectByGroupAndUser(Long groupId, Long userId) {
        return selectOne(new QueryWrapperX<GroupMemberDO>()
                .eq("group_id", groupId)
                .eq("user_id", userId)
                .eq("status", 0));
    }

    default Long selectCountByGroupId(Long groupId) {
        return selectCount(new QueryWrapperX<GroupMemberDO>()
                .eq("group_id", groupId)
                .eq("status", 0));
    }

    default void removeMember(Long groupId, Long userId, Integer status) {
        UpdateWrapper<GroupMemberDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id", groupId)
                .eq("user_id", userId)
                .set("status", status)
                .set("quit_time", LocalDateTime.now());
        update(null, wrapper);
    }

    default void updateMemberRole(Long groupId, Long userId, Integer role) {
        UpdateWrapper<GroupMemberDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id", groupId)
                .eq("user_id", userId)
                .set("role", role);
        update(null, wrapper);
    }

    default void updateMuteStatus(Long groupId, Long userId, Integer muted, LocalDateTime muteEndTime) {
        UpdateWrapper<GroupMemberDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id", groupId)
                .eq("user_id", userId)
                .set("muted", muted)
                .set("mute_end_time", muteEndTime);
        update(null, wrapper);
    }
}
