package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupInfoDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.message.constants.GroupConstants.GROUP_STATUS_ACTIVE;
import static cn.iocoder.yudao.module.message.constants.GroupConstants.GROUP_STATUS_DISSOLVED;

@Mapper
public interface GroupInfoMapper extends BaseMapperX<GroupInfoDO> {

    default List<GroupInfoDO> selectListByOwner(Long ownerUserId) {
        return selectList(new QueryWrapperX<GroupInfoDO>()
                .eq("owner_user_id", ownerUserId)
                .eq("status", GROUP_STATUS_ACTIVE)
                .orderByDesc("create_time"));
    }

    default GroupInfoDO selectActiveGroup(Long groupId) {
        return selectOne(new QueryWrapperX<GroupInfoDO>()
                .eq("id", groupId)
                .eq("status", GROUP_STATUS_ACTIVE));
    }

    default void updateMemberCount(Long groupId, Integer count) {
        UpdateWrapper<GroupInfoDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", groupId)
                .setSql("member_count = GREATEST(member_count + " + count + ", 0)");
        update(null, wrapper);
    }

    default void dissolveGroup(Long groupId) {
        UpdateWrapper<GroupInfoDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", groupId)
                .set("status", GROUP_STATUS_DISSOLVED)
                .set("dissolve_time", LocalDateTime.now());
        update(null, wrapper);
    }
}
