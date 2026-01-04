package cn.iocoder.yudao.module.member.dal.mysql.session;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.session.MemberUserSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberUserSessionMapper extends BaseMapperX<MemberUserSessionDO> {
}

