package cn.iocoder.yudao.module.pay.controller.admin.withdraw;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.*;
import cn.iocoder.yudao.module.pay.service.withdraw.PayWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 提现审核 Controller
 *
 * @author xiaolvshu
 */
@Tag(name = "管理后台 - 提现审核")
@RestController
@RequestMapping("/pay/withdraw")
@Validated
@Slf4j
public class PayWithdrawController {

    @Resource
    private PayWithdrawService withdrawService;

    @GetMapping("/page")
    @Operation(summary = "获得提现订单分页")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:query')")
    public CommonResult<PageResult<PayWithdrawRespVO>> getWithdrawPage(@Valid PayWithdrawPageReqVO pageReqVO) {
        PageResult<PayWithdrawRespVO> pageResult = withdrawService.getWithdrawPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得提现订单详情")
    @Parameter(name = "id", description = "订单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:query')")
    public CommonResult<PayWithdrawRespVO> getWithdrawDetail(@RequestParam("id") Long id) {
        PayWithdrawRespVO detail = withdrawService.getWithdrawDetail(id);
        return success(detail);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核提现订单")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:audit')")
    public CommonResult<Boolean> auditWithdraw(@Valid @RequestBody PayWithdrawAuditReqVO auditReqVO) {
        // 获取当前管理员ID
        Long auditorId = SecurityFrameworkUtils.getLoginUserId();

        withdrawService.auditWithdraw(
                auditReqVO.getId(),
                auditReqVO.getApproved(),
                auditReqVO.getAuditRemark(),
                auditorId
        );

        return success(true);
    }

    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核提现订单")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:audit')")
    public CommonResult<Boolean> batchAuditWithdraw(@Valid @RequestBody PayWithdrawBatchAuditReqVO batchAuditReqVO) {
        // 获取当前管理员ID
        Long auditorId = SecurityFrameworkUtils.getLoginUserId();

        withdrawService.batchAuditWithdraw(
                batchAuditReqVO.getIds(),
                batchAuditReqVO.getApproved(),
                batchAuditReqVO.getAuditRemark(),
                auditorId
        );

        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取提现审核统计数据")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:query')")
    public CommonResult<PayWithdrawStatisticsRespVO> getAuditStatistics() {
        PayWithdrawStatisticsRespVO statistics = withdrawService.getAuditStatistics();
        return success(statistics);
    }

}

