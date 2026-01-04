package cn.iocoder.yudao.module.member.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.task.MemberTaskConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员任务配置 Mapper
 *
 * @author xiaolvshu
 */
@Mapper
public interface MemberTaskConfigMapper extends BaseMapperX<MemberTaskConfigDO> {

    /**
     * 查询所有启用的任务配置
     *
     * @return 任务配置列表
     */
    default List<MemberTaskConfigDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<MemberTaskConfigDO>()
                .eq(MemberTaskConfigDO::getStatus, 1) // 1-启用
                .orderByAsc(MemberTaskConfigDO::getSort));
    }

    /**
     * 根据任务类型查询配置
     *
     * @param taskType 任务类型
     * @return 任务配置
     */
    default MemberTaskConfigDO selectByTaskType(Integer taskType) {
        return selectOne(new LambdaQueryWrapperX<MemberTaskConfigDO>()
                .eq(MemberTaskConfigDO::getTaskType, taskType)
                .eq(MemberTaskConfigDO::getStatus, 1));
    }

}
