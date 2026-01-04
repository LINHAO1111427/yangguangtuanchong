package cn.iocoder.yudao.module.pay.controller.app.withdraw;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppWithdrawApplyReqVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppWithdrawOrderRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.PayWithdrawOrderDO;
import cn.iocoder.yudao.module.pay.service.withdraw.PayWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 提现管理 Controller
 *
 * @author xiaolvshu
 */
@Tag(name = "用户 App - 提现管理")
@RestController
@RequestMapping("/pay/withdraw")
@Validated
public class AppWithdrawController {

    @Resource
    private PayWithdrawService withdrawService;

    @PostMapping("/apply")
    @Operation(summary = "申请提现")
    public CommonResult<Long> applyWithdraw(@Valid @RequestBody AppWithdrawApplyReqVO applyReqVO) {
        Long orderId = withdrawService.applyWithdraw(getLoginUserId(), applyReqVO);
        return success(orderId);
    }

    @GetMapping("/list")
    @Operation(summary = "查询我的提现记录")
    public CommonResult<List<AppWithdrawOrderRespVO>> getWithdrawList() {
        List<PayWithdrawOrderDO> list = withdrawService.getWithdrawList(getLoginUserId());
        return success(convertToRespList(list));
    }

    @GetMapping("/list-by-status")
    @Operation(summary = "按状态查询提现记录")
    @Parameter(name = "status", description = "提现状态 0-待审核 1-审核通过 2-审核拒绝 3-提现中 4-提现成功 5-提现失败", required = true)
    public CommonResult<List<AppWithdrawOrderRespVO>> getWithdrawListByStatus(@RequestParam("status") Integer status) {
        List<PayWithdrawOrderDO> list = withdrawService.getWithdrawListByStatus(getLoginUserId(), status);
        return success(convertToRespList(list));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询提现订单详情")
    @Parameter(name = "orderId", description = "订单ID", required = true)
    public CommonResult<AppWithdrawOrderRespVO> getWithdrawOrder(@RequestParam("orderId") Long orderId) {
        PayWithdrawOrderDO order = withdrawService.getWithdrawOrder(orderId, getLoginUserId());
        return success(convertToResp(order));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消提现（仅待审核状态可取消）")
    @Parameter(name = "orderId", description = "订单ID", required = true)
    public CommonResult<Boolean> cancelWithdraw(@RequestParam("orderId") Long orderId) {
        withdrawService.cancelWithdraw(orderId, getLoginUserId());
        return success(true);
    }

    @GetMapping("/total-amount")
    @Operation(summary = "统计我的提现成功总金额")
    public CommonResult<Integer> getTotalWithdrawAmount() {
        return success(withdrawService.getTotalWithdrawAmount(getLoginUserId()));
    }

    private List<AppWithdrawOrderRespVO> convertToRespList(List<PayWithdrawOrderDO> list) {
        return list.stream().map(this::convertToResp).collect(Collectors.toList());
    }

    private AppWithdrawOrderRespVO convertToResp(PayWithdrawOrderDO order) {
        if (order == null) {
            return null;
        }
        AppWithdrawOrderRespVO resp = new AppWithdrawOrderRespVO();
        resp.setId(order.getId());
        resp.setUserId(order.getUserId());
        resp.setAmount(order.getAmount());
        resp.setFee(order.getFee());
        resp.setRealAmount(order.getRealAmount());
        resp.setBankCardNo(order.getBankCardNo());
        resp.setBankName(order.getBankName());
        resp.setAccountName(order.getAccountName());
        resp.setStatus(order.getStatus());
        resp.setAuditRemark(order.getAuditRemark());
        resp.setAuditTime(order.getAuditTime());
        resp.setCompleteTime(order.getCompleteTime());
        resp.setFailReason(order.getFailReason());
        resp.setRemark(order.getRemark());
        resp.setCreateTime(order.getCreateTime());
        return resp;
    }

}

