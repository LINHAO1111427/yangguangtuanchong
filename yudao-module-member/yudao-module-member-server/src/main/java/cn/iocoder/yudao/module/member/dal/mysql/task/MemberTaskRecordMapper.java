package cn.iocoder.yudao.module.member.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.task.MemberTaskRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 会员任务完成记录 Mapper
 *
 * @author xiaolvshu
 */
@Mapper
public interface MemberTaskRecordMapper extends BaseMapperX<MemberTaskRecordDO> {

    /**
     * 查询用户今日的任务记录列表
     *
     * @param userId   用户ID
     * @param taskDate 任务日期
     * @return 任务记录列表
     */
    default List<MemberTaskRecordDO> selectListByUserIdAndDate(Long userId, LocalDate taskDate) {
        return selectList(new LambdaQueryWrapperX<MemberTaskRecordDO>()
                .eq(MemberTaskRecordDO::getUserId, userId)
                .eq(MemberTaskRecordDO::getTaskDate, taskDate)
                .orderByAsc(MemberTaskRecordDO::getTaskType));
    }

    /**
     * 查询用户指定任务的今日记录
     *
     * @param userId   用户ID
     * @param taskId   任务ID
     * @param taskDate 任务日期
     * @return 任务记录
     */
    default MemberTaskRecordDO selectByUserIdAndTaskIdAndDate(Long userId, Long taskId, LocalDate taskDate) {
        return selectOne(new LambdaQueryWrapperX<MemberTaskRecordDO>()
                .eq(MemberTaskRecordDO::getUserId, userId)
                .eq(MemberTaskRecordDO::getTaskId, taskId)
                .eq(MemberTaskRecordDO::getTaskDate, taskDate));
    }

    /**
     * 查询用户指定任务类型的今日记录
     *
     * @param userId   用户ID
     * @param taskType 任务类型
     * @param taskDate 任务日期
     * @return 任务记录
     */
    default MemberTaskRecordDO selectByUserIdAndTaskTypeAndDate(Long userId, Integer taskType, LocalDate taskDate) {
        return selectOne(new LambdaQueryWrapperX<MemberTaskRecordDO>()
                .eq(MemberTaskRecordDO::getUserId, userId)
                .eq(MemberTaskRecordDO::getTaskType, taskType)
                .eq(MemberTaskRecordDO::getTaskDate, taskDate));
    }

    /**
     * 统计用户完成任务次数
     *
     * @param userId 用户ID
     * @return 完成次数
     */
    default Long selectCompletedCountByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<MemberTaskRecordDO>()
                .eq(MemberTaskRecordDO::getUserId, userId)
                .eq(MemberTaskRecordDO::getStatus, 2)); // 2-已领取奖励
    }

}
