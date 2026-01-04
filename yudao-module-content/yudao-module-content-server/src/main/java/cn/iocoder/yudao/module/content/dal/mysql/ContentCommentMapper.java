package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContentCommentMapper extends BaseMapperX<ContentCommentDO> {

    default List<ContentCommentDO> selectByContentId(Long contentId) {
        return selectList(ContentCommentDO::getContentId, contentId);
    }
}
