package cn.iocoder.yudao.module.message.dal.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMessageDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GroupMessageMapper extends BaseMapperX<GroupMessageDO> {

    default List<GroupMessageDO> selectRecentMessages(Long groupId, Integer limit) {
        return selectList(new QueryWrapperX<GroupMessageDO>()
                .eq("group_id", groupId)
                .orderByDesc("create_time")
                .last("LIMIT " + limit));
    }

    default void updateStatusToRecall(Long messageId, Long operatorId) {
        UpdateWrapper<GroupMessageDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId)
                .set("status", 1)
                .set("recall_time", java.time.LocalDateTime.now())
                .set("update_time", java.time.LocalDateTime.now());
        update(null, wrapper);
    }

    default List<GroupMessageDO> selectGroupMessages(Long groupId, Integer limit) {
        return selectList(new QueryWrapperX<GroupMessageDO>()
                .eq("group_id", groupId)
                .eq("status", 0)
                .orderByDesc("create_time")
                .last("LIMIT " + limit));
    }

    default void updateStatusToDeleted(Long messageId) {
        UpdateWrapper<GroupMessageDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId)
                .set("status", 2)
                .set("deleted", 1)
                .set("update_time", LocalDateTime.now());
        update(null, wrapper);
    }

}
