package cn.iocoder.yudao.module.member.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.member.controller.app.task.vo.AppMemberTaskRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.task.MemberTaskConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.task.MemberTaskRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.task.MemberTaskConfigMapper;
import cn.iocoder.yudao.module.member.dal.mysql.task.MemberTaskRecordMapper;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.member.service.point.MemberPointRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 会员任务 Service 实现类
 *
 * @author xiaolvshu
 */
@Service
@Validated
@Slf4j
public class MemberTaskServiceImpl implements MemberTaskService {

    @Resource
    private MemberTaskConfigMapper taskConfigMapper;
    @Resource
    private MemberTaskRecordMapper taskRecordMapper;
    @Resource
    private MemberPointRecordService pointRecordService;

    @Override
    public List<AppMemberTaskRespVO> getTodayTaskList(Long userId) {
        // 1. 获取所有启用的任务配置
        List<MemberTaskConfigDO> configList = taskConfigMapper.selectEnabledList();
        if (CollUtil.isEmpty(configList)) {
            return new ArrayList<>();
        }

        // 2. 获取用户今日的任务记录
        LocalDate today = LocalDate.now();
        List<MemberTaskRecordDO> recordList = taskRecordMapper.selectListByUserIdAndDate(userId, today);
        Map<Long, MemberTaskRecordDO> recordMap = recordList.stream()
                .collect(Collectors.toMap(MemberTaskRecordDO::getTaskId, record -> record));

        // 3. 组装返回数据
        List<AppMemberTaskRespVO> result = new ArrayList<>();
        for (MemberTaskConfigDO config : configList) {
            AppMemberTaskRespVO vo = new AppMemberTaskRespVO();
            vo.setTaskId(config.getId());
            vo.setTaskType(config.getTaskType());
            vo.setTaskName(config.getTaskName());
            vo.setTaskDesc(config.getTaskDesc());
            vo.setRequiredCount(config.getRequiredCount());
            vo.setRewardPoint(config.getRewardPoint());

            // 如果有任务记录，设置进度和状态
            MemberTaskRecordDO record = recordMap.get(config.getId());
            if (record != null) {
                vo.setProgress(record.getProgress());
                vo.setStatus(record.getStatus());
            } else {
                vo.setProgress(0);
                vo.setStatus(0); // 0-进行中
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordTaskProgress(Long userId, Integer taskType) {
        // 1. 根据任务类型查询任务配置
        MemberTaskConfigDO config = taskConfigMapper.selectByTaskType(taskType);
        if (config == null) {
            log.warn("[recordTaskProgress][任务类型({})未配置]", taskType);
            return;
        }

        // 2. 查询用户今日的任务记录
        LocalDate today = LocalDate.now();
        MemberTaskRecordDO record = taskRecordMapper.selectByUserIdAndTaskIdAndDate(userId, config.getId(), today);

        // 3. 如果没有记录，创建新记录
        if (record == null) {
            record = new MemberTaskRecordDO();
            record.setUserId(userId);
            record.setTaskId(config.getId());
            record.setTaskType(taskType);
            record.setProgress(1);
            record.setRequiredCount(config.getRequiredCount());
            record.setStatus(0); // 0-进行中
            record.setRewardPoint(config.getRewardPoint());
            record.setTaskDate(today);
            taskRecordMapper.insert(record);
        } else {
            // 4. 如果已有记录，更新进度
            // 如果任务已完成或已领取奖励，不再更新进度
            if (record.getStatus() >= 1) {
                return;
            }

            // 更新进度
            Integer newProgress = record.getProgress() + 1;
            record.setProgress(newProgress);

            // 如果进度达到要求，标记为已完成
            if (newProgress >= config.getRequiredCount()) {
                record.setStatus(1); // 1-已完成
            }

            taskRecordMapper.updateById(record);
        }

        log.info("[recordTaskProgress][用户({}) 任务类型({}) 进度更新: {}/{}]",
                userId, taskType, record.getProgress(), config.getRequiredCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTaskReward(Long userId, Long taskId) {
        // 1. 查询用户今日的任务记录
        LocalDate today = LocalDate.now();
        MemberTaskRecordDO record = taskRecordMapper.selectByUserIdAndTaskIdAndDate(userId, taskId, today);

        // 2. 校验任务记录
        if (record == null) {
            throw exception(TASK_RECORD_NOT_EXISTS);
        }
        if (record.getStatus() != 1) {
            throw exception(TASK_NOT_COMPLETED);
        }

        // 3. 发放积分奖励
        pointRecordService.createPointRecord(userId, record.getRewardPoint(),
                MemberPointBizTypeEnum.TASK_REWARD, String.valueOf(taskId));

        // 4. 更新任务状态为已领取
        record.setStatus(2); // 2-已领取奖励
        taskRecordMapper.updateById(record);

        log.info("[claimTaskReward][用户({}) 领取任务({}) 奖励: {} 积分]",
                userId, taskId, record.getRewardPoint());
    }

    @Override
    public void resetDailyTasks() {
        // 每日任务重置逻辑
        // 由于使用taskDate字段区分每日任务，因此无需手动重置
        // 新的一天用户操作时会自动创建新的任务记录
        log.info("[resetDailyTasks][每日任务已重置]");
    }

}

