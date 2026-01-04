package cn.iocoder.yudao.module.member.service.task;

import cn.iocoder.yudao.module.member.controller.app.task.vo.AppMemberTaskRespVO;

import java.util.List;

/**
 * 会员任务 Service 接口
 *
 * @author xiaolvshu
 */
public interface MemberTaskService {

    /**
     * 获取用户今日任务列表（含进度）
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<AppMemberTaskRespVO> getTodayTaskList(Long userId);

    /**
     * 记录任务进度（用户观看内容时调用）
     *
     * @param userId   用户ID
     * @param taskType 任务类型 1-观看短视频 2-观看图文 3-观看广告
     */
    void recordTaskProgress(Long userId, Integer taskType);

    /**
     * 领取任务奖励
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     */
    void claimTaskReward(Long userId, Long taskId);

    /**
     * 重置每日任务（定时任务调用，每天0点执行）
     */
    void resetDailyTasks();

}
