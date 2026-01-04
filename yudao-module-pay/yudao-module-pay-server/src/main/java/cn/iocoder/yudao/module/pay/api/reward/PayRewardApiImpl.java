package cn.iocoder.yudao.module.pay.api.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.service.reward.PayRewardService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 打赏 API 实现类
 *
 * @author 阳光团宠
 */
@RestController
@Validated
public class PayRewardApiImpl implements PayRewardApi {

    @Resource
    private PayRewardService payRewardService;

    @Override
    public CommonResult<Integer> getTotalRewardAmountByPost(Long postId) {
        Integer amount = payRewardService.getTotalRewardAmountByPost(postId);
        return success(amount != null ? amount : 0);
    }

    @Override
    public CommonResult<Integer> getTotalIncomeAmount(Long authorId) {
        Integer amount = payRewardService.getTotalIncomeAmount(authorId);
        return success(amount != null ? amount : 0);
    }

}
