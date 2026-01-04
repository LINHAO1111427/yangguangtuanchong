package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface ContentFavoriteGroupMapper extends BaseMapperX<ContentFavoriteGroupDO> {

    default List<ContentFavoriteGroupDO> selectByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentFavoriteGroupDO>()
                .eq(ContentFavoriteGroupDO::getUserId, userId)
                .orderByDesc(ContentFavoriteGroupDO::getIsDefault)
                .orderByAsc(ContentFavoriteGroupDO::getId));
    }

    default ContentFavoriteGroupDO selectDefaultGroup(Long userId) {
        if (userId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<ContentFavoriteGroupDO>()
                .eq(ContentFavoriteGroupDO::getUserId, userId)
                .eq(ContentFavoriteGroupDO::getIsDefault, 1)
                .last("LIMIT 1"));
    }
}
