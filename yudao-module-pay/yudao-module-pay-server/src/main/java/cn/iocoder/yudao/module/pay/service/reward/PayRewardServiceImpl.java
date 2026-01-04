package cn.iocoder.yudao.module.pay.service.reward;

import cn.iocoder.yudao.module.pay.controller.app.reward.vo.AppRewardCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.reward.PayRewardOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.mysql.reward.PayRewardOrderMapper;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 打赏服务 Service 实现类
 *
 * @author xiaolvshu
 */
@Service
@Validated
@Slf4j
public class PayRewardServiceImpl implements PayRewardService {

    @Resource
    private PayRewardOrderMapper rewardOrderMapper;
    @Resource
    private PayWalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRewardOrder(Long userId, AppRewardCreateReqVO createReqVO) {
        // 1. 校验打赏金额（0.01-9999元）
        if (createReqVO.getAmount() < 1 || createReqVO.getAmount() > 999900) {
            throw exception(REWARD_AMOUNT_INVALID);
        }

        // 2. 不能打赏自己
        if (userId.equals(createReqVO.getAuthorId())) {
            throw exception(REWARD_SELF_NOT_ALLOWED);
        }

        // 3. 获取或创建用户钱包
        PayWalletDO wallet = walletService.getOrCreateWallet(userId, 1); // 1-会员用户

        // 4. 校验余额
        if (wallet.getBalance() < createReqVO.getAmount()) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }

        // 5. 计算平台抽成比率（默认 10.5%）
        BigDecimal commissionRate = new BigDecimal("10.5");

        // 6. 计算平台抽成和作者收益
        BigDecimal amount = new BigDecimal(createReqVO.getAmount());
        Integer commissionAmount = amount.multiply(commissionRate)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                .intValue();
        Integer incomeAmount = createReqVO.getAmount() - commissionAmount;

        // 7. 创建打赏订单
        PayRewardOrderDO order = PayRewardOrderDO.builder()
                .userId(userId)
                .authorId(createReqVO.getAuthorId())
                .walletId(wallet.getId())
                .targetId(createReqVO.getTargetId())
                .rewardType(createReqVO.getRewardType())
                .amount(createReqVO.getAmount())
                .commissionAmount(commissionAmount)
                .incomeAmount(incomeAmount)
                .payStatus(0) // 待支付
                .remark(createReqVO.getRemark())
                .build();
        rewardOrderMapper.insert(order);

        // 8. 扣减打赏用户余额
        walletService.reduceWalletBalance(wallet.getId(), order.getId(),
                PayWalletBizTypeEnum.REWARD, createReqVO.getAmount());

        // 9. 增加作者收益
        PayWalletDO authorWallet = walletService.getOrCreateWallet(createReqVO.getAuthorId(), 1);
        walletService.addWalletBalance(authorWallet.getId(), String.valueOf(order.getId()),
                PayWalletBizTypeEnum.REWARD_INCOME, incomeAmount);

        // 10. 更新订单状态为已支付
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        rewardOrderMapper.updateById(order);

        log.info("[createRewardOrder][用户({}) 打赏作者({}) 金额({})分，平台抽成({})分，作者收益({})分]",
                userId, createReqVO.getAuthorId(), createReqVO.getAmount(), commissionAmount, incomeAmount);

        return order.getId();
    }

    @Override
    public List<PayRewardOrderDO> getMyRewardList(Long userId) {
        return rewardOrderMapper.selectListByUserId(userId);
    }

    @Override
    public List<PayRewardOrderDO> getReceivedRewardList(Long authorId) {
        return rewardOrderMapper.selectListByAuthorId(authorId);
    }

    @Override
    public PayRewardOrderDO getRewardOrder(Long orderId) {
        return rewardOrderMapper.selectById(orderId);
    }

    @Override
    public Integer getTotalRewardAmount(Long userId) {
        return rewardOrderMapper.selectTotalAmountByUserId(userId);
    }

    @Override
    public Integer getTotalIncomeAmount(Long authorId) {
        return rewardOrderMapper.selectTotalIncomeByAuthorId(authorId);
    }

    @Override
    public Integer getTotalRewardAmountByPost(Long postId) {
        // rewardType = 1 表示内容打赏
        return rewardOrderMapper.selectTotalAmountByTarget(postId, 1);
    }

}

