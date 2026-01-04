package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostConfigRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostRecordRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStartReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStatRespVO;
import cn.iocoder.yudao.module.content.service.boost.ContentBoostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 内容推广")
@Validated
@RestController
@RequestMapping("/content/boost")
public class AppContentBoostController {

    @Resource
    private ContentBoostService contentBoostService;

    @PostMapping("/start")
    @Operation(summary = "Start boost (placeholder)")
    public CommonResult<Long> startBoost(@Valid @RequestBody ContentBoostStartReqVO reqVO) {
        return success(contentBoostService.startBoost(reqVO));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel boost (placeholder)")
    public CommonResult<Boolean> cancelBoost(@NotNull @RequestParam("id") Long id,
                                             @NotNull @RequestParam("userId") Long userId) {
        return success(contentBoostService.cancelBoost(id, userId));
    }

    @GetMapping("/records")
    @Operation(summary = "List boost records")
    public CommonResult<PageResult<ContentBoostRecordRespVO>> getRecords(
            @NotNull @RequestParam("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return success(contentBoostService.getMyBoostRecords(userId, page, size));
    }

    @GetMapping("/record/detail")
    @Operation(summary = "Boost record detail")
    public CommonResult<ContentBoostRecordRespVO> getRecordDetail(
            @NotNull @RequestParam("id") Long id,
            @NotNull @RequestParam("userId") Long userId) {
        return success(contentBoostService.getBoostRecordDetail(id, userId));
    }

    @GetMapping("/record/stats")
    @Operation(summary = "Boost record statistics")
    public CommonResult<List<ContentBoostStatRespVO>> getRecordStats(
            @NotNull @RequestParam("id") Long id,
            @NotNull @RequestParam("userId") Long userId) {
        return success(contentBoostService.getBoostStats(id, userId));
    }

    @GetMapping("/configs")
    @Operation(summary = "Boost configurations")
    public CommonResult<List<ContentBoostConfigRespVO>> getConfigs() {
        return success(contentBoostService.getBoostConfigs());
    }
}
