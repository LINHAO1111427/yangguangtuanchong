package cn.iocoder.yudao.module.content.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ContentFavoriteRecordMapper extends BaseMapperX<ContentFavoriteRecordDO> {

    default List<ContentFavoriteRecordDO> selectByGroupIds(Collection<Long> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentFavoriteRecordDO>()
                .in(ContentFavoriteRecordDO::getGroupId, groupIds)
                .eq(ContentFavoriteRecordDO::getDeleted, 0));
    }

    default ContentFavoriteRecordDO selectOne(Long userId, Long contentId) {
        if (userId == null || contentId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<ContentFavoriteRecordDO>()
                .eq(ContentFavoriteRecordDO::getUserId, userId)
                .eq(ContentFavoriteRecordDO::getContentId, contentId)
                .last("LIMIT 1"));
    }

    default List<ContentFavoriteRecordDO> selectByUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentFavoriteRecordDO>()
                .eq(ContentFavoriteRecordDO::getUserId, userId)
                .eq(ContentFavoriteRecordDO::getDeleted, 0)
                .orderByDesc(ContentFavoriteRecordDO::getCreateTime));
    }
}
