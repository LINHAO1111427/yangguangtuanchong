package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ContentChannelUserMapper extends BaseMapperX<ContentChannelUserDO> {

    default List<ContentChannelUserDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ContentChannelUserDO>()
                .eq(ContentChannelUserDO::getUserId, userId)
                .eq(ContentChannelUserDO::getDeleted, 0)
                .orderByAsc(ContentChannelUserDO::getDisplayOrder)
                .orderByAsc(ContentChannelUserDO::getId));
    }

    default void deleteByUserIdExcluding(Long userId, Collection<Long> keepIds) {
        LambdaQueryWrapperX<ContentChannelUserDO> wrapper = new LambdaQueryWrapperX<ContentChannelUserDO>()
                .eq(ContentChannelUserDO::getUserId, userId);
        if (keepIds != null && !keepIds.isEmpty()) {
            wrapper.notIn(ContentChannelUserDO::getChannelId, keepIds);
        }
        // 逻辑删除，遵循 BaseDO 的 deleted 字段
        ContentChannelUserDO updateObj = new ContentChannelUserDO();
        updateObj.setDeleted(1);
        update(updateObj, wrapper);
    }
}
