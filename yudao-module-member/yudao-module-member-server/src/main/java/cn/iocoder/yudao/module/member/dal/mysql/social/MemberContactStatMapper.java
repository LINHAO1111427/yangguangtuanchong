package cn.iocoder.yudao.module.member.dal.mysql.social;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberContactStatDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 可能认识的人统计 mapper
 *
 * @author sun
 */
@Mapper
public interface MemberContactStatMapper extends BaseMapperX<MemberContactStatDO> {

    default List<MemberContactStatDO> selectListByUserId(Long userId, int limit) {
        return selectList(new LambdaQueryWrapperX<MemberContactStatDO>()
                .eq(MemberContactStatDO::getUserId, userId)
                .orderByDesc(MemberContactStatDO::getScore, MemberContactStatDO::getUpdateTime)
                .last("LIMIT " + Math.max(limit, 0)));
    }

}
