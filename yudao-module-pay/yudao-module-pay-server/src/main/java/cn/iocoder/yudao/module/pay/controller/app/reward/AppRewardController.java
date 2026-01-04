package cn.iocoder.yudao.module.pay.controller.app.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.app.reward.vo.AppRewardCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.app.reward.vo.AppRewardOrderRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.reward.PayRewardOrderDO;
import cn.iocoder.yudao.module.pay.service.reward.PayRewardService;
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

@Tag(name = "用户 App - 打赏")
@RestController
@RequestMapping("/pay/reward")
@Validated
public class AppRewardController {

    @Resource
    private PayRewardService rewardService;

    @PostMapping("/create")
    @Operation(summary = "创建打赏订单")
    public CommonResult<Long> createRewardOrder(@Valid @RequestBody AppRewardCreateReqVO createReqVO) {
        Long orderId = rewardService.createRewardOrder(getLoginUserId(), createReqVO);
        return success(orderId);
    }

    @GetMapping("/my-rewards")
    @Operation(summary = "查询我的打赏记录")
    public CommonResult<List<AppRewardOrderRespVO>> getMyRewardList() {
        List<PayRewardOrderDO> list = rewardService.getMyRewardList(getLoginUserId());
        return success(convertToRespList(list));
    }

    @GetMapping("/received")
    @Operation(summary = "查询收到的打赏记录")
    public CommonResult<List<AppRewardOrderRespVO>> getReceivedRewardList() {
        List<PayRewardOrderDO> list = rewardService.getReceivedRewardList(getLoginUserId());
        return success(convertToRespList(list));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询打赏订单详情")
    @Parameter(name = "orderId", description = "订单ID", required = true)
    public CommonResult<AppRewardOrderRespVO> getRewardOrder(@RequestParam("orderId") Long orderId) {
        PayRewardOrderDO order = rewardService.getRewardOrder(orderId);
        return success(convertToResp(order));
    }

    @GetMapping("/total-amount")
    @Operation(summary = "统计我的打赏总金额")
    public CommonResult<Integer> getTotalRewardAmount() {
        return success(rewardService.getTotalRewardAmount(getLoginUserId()));
    }

    @GetMapping("/total-income")
    @Operation(summary = "统计我的收益总金额")
    public CommonResult<Integer> getTotalIncomeAmount() {
        return success(rewardService.getTotalIncomeAmount(getLoginUserId()));
    }

    private List<AppRewardOrderRespVO> convertToRespList(List<PayRewardOrderDO> list) {
        return list.stream().map(this::convertToResp).collect(Collectors.toList());
    }

    private AppRewardOrderRespVO convertToResp(PayRewardOrderDO order) {
        if (order == null) {
            return null;
        }
        AppRewardOrderRespVO resp = new AppRewardOrderRespVO();
        resp.setId(order.getId());
        resp.setUserId(order.getUserId());
        resp.setAuthorId(order.getAuthorId());
        resp.setTargetId(order.getTargetId());
        resp.setRewardType(order.getRewardType());
        resp.setAmount(order.getAmount());
        resp.setCommissionAmount(order.getCommissionAmount());
        resp.setIncomeAmount(order.getIncomeAmount());
        resp.setPayStatus(order.getPayStatus());
        resp.setPayTime(order.getPayTime());
        resp.setRemark(order.getRemark());
        resp.setCreateTime(order.getCreateTime());
        return resp;
    }

}

