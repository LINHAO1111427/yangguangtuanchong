package cn.iocoder.yudao.module.member.controller.app.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.controller.app.task.vo.AppMemberTaskRespVO;
import cn.iocoder.yudao.module.member.service.task.MemberTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 每日任务")
@RestController
@RequestMapping("/member/task")
@Validated
public class AppMemberTaskController {

    @Resource
    private MemberTaskService taskService;

    @GetMapping("/list")
    @Operation(summary = "获取今日任务列表（含进度）")
    public CommonResult<List<AppMemberTaskRespVO>> getTodayTaskList() {
        return success(taskService.getTodayTaskList(getLoginUserId()));
    }

    @PostMapping("/progress")
    @Operation(summary = "记录任务进度")
    @Parameter(name = "taskType", description = "任务类型 1-观看短视频 2-观看图文 3-观看广告", required = true)
    public CommonResult<Boolean> recordTaskProgress(@RequestParam("taskType") Integer taskType) {
        taskService.recordTaskProgress(getLoginUserId(), taskType);
        return success(true);
    }

    @PostMapping("/claim")
    @Operation(summary = "领取任务奖励")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    public CommonResult<Boolean> claimTaskReward(@RequestParam("taskId") Long taskId) {
        taskService.claimTaskReward(getLoginUserId(), taskId);
        return success(true);
    }

}

